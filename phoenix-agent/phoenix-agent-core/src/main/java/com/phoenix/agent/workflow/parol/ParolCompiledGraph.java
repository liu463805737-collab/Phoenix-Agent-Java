package com.phoenix.agent.workflow.parol;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.phoenix.agent.workflow.AbstractCompiledGragph;
import com.phoenix.data.entity.Agent;
import com.phoenix.data.service.agent.AgentService;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 巡逻小助手
 */
@Component
public class ParolCompiledGraph extends AbstractCompiledGragph {
    public static final String SN = "ParolCompiledGraph";
    public static final String NAME = "警务巡逻小助手";

    @Autowired
    private AgentService agentService;
    @Autowired
    private ParolSearchTool parolSearchTool;

    @Override
    public String getSn() {
        return SN;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "警务巡逻小助手";
    }

    /**
     * 创建治安巡逻工作流
     *
     * @return
     * @throws GraphStateException
     */
    @Override
    public CompiledGraph createParolCompiledGraph() throws GraphStateException {
        // 定义状态管理策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("input", new ReplaceStrategy());
            strategies.put("cleaned_input", new ReplaceStrategy());
            strategies.put("keyword_result", new ReplaceStrategy());
            strategies.put("userProfile_result", new ReplaceStrategy());
            strategies.put("result", new ReplaceStrategy());
            return strategies;
        };
        // 构建工作流
        StateGraph workflow = new StateGraph(keyStrategyFactory);
        // 添加普通Node
        workflow.addNode("preprocess", node_async(new ParolPreprocessorNode()));
        /** 第一个节点 把口语化的给我翻译成公安的术语 */
        Agent agent = agentService.findBySn(getSn());
        String sytemPrompt = null;
        if (agent != null) {
            sytemPrompt = StrUtil.blankToDefault(agent.getPrompt(), "无系统提示词");
        }

        ReactAgent keywordAgent = ReactAgent.builder()
                .name("keywordAgent")
                .model(this.createChatModel())
                .systemPrompt(sytemPrompt)
                .instruction(" 只需要把当前的这句 {cleaned_input} 变成正式书面语即可，不需要做其他的动作。")
                .outputKey("keyword_result")
                .saver(saver)
                .hooks(this.getCommonHooks())
                .interceptors(this.getCommonInterceptors())
                .enableLogging(true).build();
        workflow.addNode(keywordAgent.name(), keywordAgent.asNode(true,   // includeContents: 传递父图的消息历史
                false   // includeReasoning: 不返回推理过程
        ));
        workflow.addNode("userProfile", node_async(new ParolUserProfileNode()));
        ReactAgent argAgent = ReactAgent.builder()
                .name("argAgent")
                .model(this.createChatModel())
                .systemPrompt(sytemPrompt)
                .instruction("{userProfile_result} \n {keyword_result} ")
                .outputKey("result").saver(saver).methodTools(parolSearchTool).enableLogging(true).build();
        // 添加Agent Node（嵌套的ReactAgent）
        workflow.addNode(argAgent.name(), argAgent.asNode(true,   // includeContents: 传递父图的消息历史
                false   // includeReasoning: 不返回推理过程
        ));
        // 定义流程：预处理 -> Agent处理 -> 验证
        workflow.addEdge(StateGraph.START, "preprocess");
        workflow.addEdge("preprocess", keywordAgent.name());
        workflow.addEdge(keywordAgent.name(), "userProfile");
        workflow.addEdge("userProfile", argAgent.name());
        workflow.addEdge(argAgent.name(), StateGraph.END);
        // 编译工作流
        CompiledGraph compiledGraph = workflow
                .compile(this.buildCompileConfig());
        return compiledGraph;
    }


}
