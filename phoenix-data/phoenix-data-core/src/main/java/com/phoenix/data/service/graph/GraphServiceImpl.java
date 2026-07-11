package com.phoenix.data.service.graph;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.phoenix.common.vo.front.LoginVO;
import com.phoenix.data.dto.GraphRequest;
import com.phoenix.data.enums.TextType;
import com.phoenix.data.service.graph.Context.MultiTurnContextManager;
import com.phoenix.data.service.graph.Context.StreamContext;
import com.phoenix.data.service.langfuse.LangfuseService;
import com.phoenix.data.vo.GraphNodeResponse;
import com.phoenix.data.workflow.node.PlannerNode;
import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.phoenix.common.constant.CommonConstant.ACCOUNT_LOGIN;
import static com.phoenix.data.constant.Constant.*;

/**
 * 图服务实现类，管理NL2SQL状态图的编译、流式处理及生命周期
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class GraphServiceImpl implements GraphService {

    /**
     * 编译后的状态图
     */
    private final CompiledGraph compiledGraph;

    /**
     * 异步执行器
     */
    private final ExecutorService executor;

    /**
     * 流式处理上下文映射表
     */
    private final ConcurrentHashMap<String, StreamContext> streamContextMap = new ConcurrentHashMap<>();

    /**
     * 多轮对话上下文管理器
     */
    private final MultiTurnContextManager multiTurnContextManager;

    /**
     * Langfuse链路追踪服务
     */
    private final LangfuseService langfuseReporter;
    /**
     * 状态检查点配置
     */
    private final SaverConfig saverConfig;

    /**
     * 构造函数，编译状态图并初始化服务
     *
     * @param stateGraph              状态图
     * @param executorService         执行器服务
     * @param multiTurnContextManager 多轮上下文管理器
     * @param langfuseReporter        Langfuse上报服务
     * @param saverConfig             检查点配置
     * @throws GraphStateException 图状态异常
     */
    public GraphServiceImpl(StateGraph stateGraph, ExecutorService executorService,
                            MultiTurnContextManager multiTurnContextManager, LangfuseService langfuseReporter,
                            SaverConfig saverConfig)
            throws GraphStateException {
        this.saverConfig = saverConfig;
        this.compiledGraph = stateGraph.compile(CompileConfig.builder().saverConfig(saverConfig).interruptBefore(HUMAN_FEEDBACK_NODE).build());
        this.executor = executorService;
        this.multiTurnContextManager = multiTurnContextManager;
        this.langfuseReporter = langfuseReporter;
    }

    /**
     * 自然语言转SQL，仅返回SQL代码结果
     *
     * @param naturalQuery 自然语言查询
     * @param agentId      智能体ID
     * @return SQL结果
     * @throws GraphRunnerException 图运行异常
     */
    @Override
    @Transactional(readOnly = true)
    public String nl2sql(String naturalQuery, String agentId) throws GraphRunnerException {
        OverAllState state = compiledGraph
                .invoke(Map.of(IS_ONLY_NL2SQL, true, INPUT_KEY, naturalQuery, AGENT_ID, agentId),
                        RunnableConfig.builder().build())
                .orElseThrow();
        return state.value(SQL_GENERATE_OUTPUT, "");
    }

    /**
     * 流式处理NL2SQL或DataAgent请求
     *
     * @param sink         SSE事件发射器
     * @param graphRequest 请求体
     */
    @Override
    public void graphStreamProcess(Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink, GraphRequest graphRequest) {
        if (!StringUtils.hasText(graphRequest.getThreadId())) {
            graphRequest.setThreadId(UUID.randomUUID().toString());
        }
        String threadId = graphRequest.getThreadId();
        // 创建或获取 StreamContext
        StreamContext context = streamContextMap.computeIfAbsent(threadId, k -> new StreamContext());
        context.setSink(sink);
        if (StringUtils.hasText(graphRequest.getHumanFeedbackContent())) {
            handleHumanFeedback(graphRequest);
        } else {
            handleNewProcess(graphRequest);
        }
    }

    /**
     * 停止指定 threadId 的流式处理 线程安全：使用 remove 操作确保只有一个线程能获取到 context
     *
     * @param threadId 线程ID
     */
    @Override
    public void stopStreamProcessing(String threadId) {
        if (!StringUtils.hasText(threadId)) {
            return;
        }
        log.info("Stopping stream processing for threadId: {}", threadId);
        multiTurnContextManager.discardPending(threadId);
        StreamContext context = streamContextMap.remove(threadId);
        if (context != null) {
            // 客户端断开，结束 Langfuse span
            if (context.getSpan() != null && context.getSpan().isRecording()) {
                langfuseReporter.endSpanSuccess(context.getSpan(), threadId, context.getCollectedOutput());
            }
            context.cleanup();
            log.info("Cleaned up stream context for threadId: {}", threadId);
        }
    }

    /**
     * 处理新的流式请求
     *
     * @param graphRequest 图请求
     */
    private void handleNewProcess(GraphRequest graphRequest) {
        String query = graphRequest.getQuery();
        String agentId = graphRequest.getAgentId();
        String threadId = graphRequest.getThreadId();
        boolean nl2sqlOnly = graphRequest.isNl2sqlOnly();
        boolean humanReviewEnabled = graphRequest.isHumanFeedback() & !(nl2sqlOnly);
        if (!StringUtils.hasText(threadId) || !StringUtils.hasText(agentId) || !StringUtils.hasText(query)) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        StreamContext context = streamContextMap.get(threadId);
        if (context == null || context.getSink() == null) {
            throw new IllegalStateException("StreamContext not found for threadId: " + threadId);
        }
        // 检查是否已经清理，如果已清理则不再启动新的流
        if (context.isCleaned()) {
            log.warn("StreamContext already cleaned for threadId: {}, skipping stream start", threadId);
            return;
        }
        // 开始 Langfuse 追踪
        Span span = langfuseReporter.startLLMSpan("graph-stream", graphRequest);
        context.setSpan(span);

        String multiTurnContext = multiTurnContextManager.buildContext(threadId);
        multiTurnContextManager.beginTurn(threadId, query);
        Flux<NodeOutput> nodeOutputFlux = compiledGraph.stream(
                Map.of(IS_ONLY_NL2SQL, nl2sqlOnly, INPUT_KEY, query, AGENT_ID, agentId, HUMAN_REVIEW_ENABLED,
                        humanReviewEnabled, MULTI_TURN_CONTEXT, multiTurnContext, TRACE_THREAD_ID, threadId,
                        FRONT_LOGIN_INFO, builerLoginVo()
                ),
                RunnableConfig.builder().threadId(threadId).build());
        subscribeToFlux(context, nodeOutputFlux, graphRequest, agentId, threadId);
    }

    /**
     * 构建RunnableConfig
     *
     * @return
     */
    private String builerLoginVo() {
        LoginVO loginVO = StpUtil.getSession().getModel(ACCOUNT_LOGIN, LoginVO.class);
        String loginVo = "";
        if (loginVO != null) {
            loginVo = """
                    登录人相关信息：姓名：%s, 工号：%s, 账号：%s,  邮箱：%s \n
                    """.formatted(loginVO.getRealName(), loginVO.getUserCode(), loginVO.getUsername(), loginVO.getEmail());
        }
        return loginVo;
    }

    /**
     * 处理人工反馈
     *
     * @param graphRequest 图请求
     */
    private void handleHumanFeedback(GraphRequest graphRequest) {
        String agentId = graphRequest.getAgentId();
        String threadId = graphRequest.getThreadId();
        String feedbackContent = graphRequest.getHumanFeedbackContent();
        if (!StringUtils.hasText(threadId) || !StringUtils.hasText(agentId) || !StringUtils.hasText(feedbackContent)) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        StreamContext context = streamContextMap.get(threadId);
        if (context == null || context.getSink() == null) {
            throw new IllegalStateException("StreamContext not found for threadId: " + threadId);
        }
        if (context.isCleaned()) {
            log.warn("StreamContext already cleaned for threadId: {}, skipping stream start", threadId);
            return;
        }
        // 开始 Langfuse 追踪
        Span span = langfuseReporter.startLLMSpan("graph-feedback", graphRequest);
        context.setSpan(span);

        Map<String, Object> feedbackData = Map.of("feedback", !graphRequest.isRejectedPlan(), "feedback_content",
                feedbackContent);
        if (graphRequest.isRejectedPlan()) {
            multiTurnContextManager.restartLastTurn(threadId);
        }
        Map<String, Object> stateUpdate = new HashMap<>();
        stateUpdate.put(HUMAN_FEEDBACK_DATA, feedbackData);
        stateUpdate.put(MULTI_TURN_CONTEXT, multiTurnContextManager.buildContext(threadId));

        RunnableConfig baseConfig = RunnableConfig.builder().threadId(threadId).build();
        RunnableConfig updatedConfig;
        try {
            updatedConfig = compiledGraph.updateState(baseConfig, stateUpdate);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update graph state for human feedback", e);
        }
        RunnableConfig resumeConfig = RunnableConfig.builder(updatedConfig)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, feedbackData)
                .build();

        Flux<NodeOutput> nodeOutputFlux = compiledGraph.stream(null, resumeConfig);
        subscribeToFlux(context, nodeOutputFlux, graphRequest, agentId, threadId);
    }

    /**
     * 订阅 Flux 并原子性地设置 Disposable 线程安全：使用 synchronized 确保 Disposable 设置的原子性
     *
     * @param context        流式处理上下文
     * @param nodeOutputFlux 节点输出流
     * @param graphRequest   图请求
     * @param agentId        代理ID
     * @param threadId       线程ID
     */
    private void subscribeToFlux(StreamContext context, Flux<NodeOutput> nodeOutputFlux, GraphRequest graphRequest,
                                 String agentId, String threadId) {
        CompletableFuture.runAsync(() -> {
            // 在订阅之前检查上下文是否仍然有效
            if (context.isCleaned()) {
                log.debug("StreamContext cleaned before subscription for threadId: {}", threadId);
                return;
            }
            Disposable disposable = nodeOutputFlux.subscribe(output -> handleNodeOutput(graphRequest, output),
                    error -> handleStreamError(agentId, threadId, error),
                    () -> handleStreamComplete(agentId, threadId));
            // 原子性地设置 Disposable，如果已经清理则立即释放
            synchronized (context) {
                if (context.isCleaned()) {
                    // 如果已经清理，立即释放刚创建的 Disposable
                    if (disposable != null && !disposable.isDisposed()) {
                        disposable.dispose();
                    }
                } else {
                    // 只有在未清理的情况下才设置 Disposable
                    context.setDisposable(disposable);
                }
            }
        }, executor);
    }

    /**
     * 处理流式错误 线程安全：使用 remove 操作确保只有一个线程能获取到 context
     */
    private void handleStreamError(String agentId, String threadId, Throwable error) {
        log.error("Error in stream processing for threadId: {}: ", threadId, error);
        StreamContext context = streamContextMap.remove(threadId);
        if (context != null && !context.isCleaned()) {
            // 结束 Langfuse span（失败）
            if (context.getSpan() != null) {
                langfuseReporter.endSpanError(context.getSpan(), threadId,
                        error instanceof Exception ? (Exception) error : new RuntimeException(error));
            }
            if (context.getSink() != null && context.getSink().currentSubscriberCount() > 0) {
                context.getSink()
                        .tryEmitNext(ServerSentEvent
                                .builder(GraphNodeResponse.error(agentId, threadId,
                                        "Error in stream processing: " + error.getMessage()))
                                .event(STREAM_EVENT_ERROR)
                                .build());
                context.getSink().tryEmitComplete();
            }
            // 清理资源（cleanup 内部已经保证只执行一次）
            context.cleanup();
        }
    }

    /**
     * 处理流式完成 线程安全：使用 remove 操作确保只有一个线程能获取到 context
     */
    private void handleStreamComplete(String agentId, String threadId) {
        log.info("Stream processing completed successfully for threadId: {}", threadId);
        multiTurnContextManager.finishTurn(threadId);
        StreamContext context = streamContextMap.remove(threadId);
        if (context != null && !context.isCleaned()) {
            // 结束 Langfuse span（成功）
            if (context.getSpan() != null) {
                langfuseReporter.endSpanSuccess(context.getSpan(), threadId, context.getCollectedOutput());
            }
            if (context.getSink() != null && context.getSink().currentSubscriberCount() > 0) {
                context.getSink()
                        .tryEmitNext(ServerSentEvent.builder(GraphNodeResponse.complete(agentId, threadId))
                                .event(STREAM_EVENT_COMPLETE)
                                .build());
                context.getSink().tryEmitComplete();
            }
            context.cleanup();
        }
    }

    /**
     * 处理节点输出
     */
    private void handleNodeOutput(GraphRequest request, NodeOutput output) {
        log.debug("Received output: {}", output.getClass().getSimpleName());
        if (output instanceof StreamingOutput streamingOutput) {
            handleStreamNodeOutput(request, streamingOutput);
        }
    }

    /**
     * 处理流式节点输出
     *
     * @param request 图请求
     * @param output  流式输出
     */
    private void handleStreamNodeOutput(GraphRequest request, StreamingOutput output) {
        String threadId = request.getThreadId();
        StreamContext context = streamContextMap.get(threadId);
        // 检查是否已经停止处理
        if (context == null || context.getSink() == null) {
            log.debug("Stream processing already stopped for threadId: {}, skipping output", threadId);
            return;
        }
        String node = output.node();
        String chunk = output.chunk();
        log.debug("Received Stream output: {}", chunk);

        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        // 如果是文本标记符号，则更新文本类型
        TextType originType = context.getTextType();
        TextType textType;
        boolean isTypeSign = false;
        if (originType == null) {
            textType = TextType.getTypeByStratSign(chunk);
            if (textType != TextType.TEXT) {
                isTypeSign = true;
            }
            context.setTextType(textType);
        } else {
            textType = TextType.getType(originType, chunk);
            if (textType != originType) {
                isTypeSign = true;
            }
            context.setTextType(textType);
        }
        // 文本标记符号不返回给前端
        if (!isTypeSign) {
            context.appendOutput(chunk);
            if (PlannerNode.class.getSimpleName().equals(node)) {
                multiTurnContextManager.appendPlannerChunk(threadId, chunk);
            }
            GraphNodeResponse response = GraphNodeResponse.builder()
                    .agentId(request.getAgentId())
                    .threadId(threadId)
                    .nodeName(node)
                    .text(chunk)
                    .textType(textType)
                    .build();
            // 检查发送是否成功，如果失败说明客户端已断开
            Sinks.EmitResult result = context.getSink().tryEmitNext(ServerSentEvent.builder(response).build());
            if (result.isFailure()) {
                log.warn("Failed to emit data to sink for threadId: {}, result: {}. Stopping stream processing.",
                        threadId, result);
                // 如果发送失败，停止处理
                stopStreamProcessing(threadId);
            }
        }
    }

}
