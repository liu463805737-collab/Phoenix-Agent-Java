package com.phoenix.data.service.graph.Context;

import com.phoenix.data.enums.TextType;
import com.phoenix.data.vo.GraphNodeResponse;
import io.opentelemetry.api.trace.Span;
import lombok.Data;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流式处理上下文，封装每个 threadId 的所有相关状态
 *
 * @author Makoto
 * @since 2025/11/28
 */
/**
 * 流式处理上下文，封装每个 threadId 的所有相关状态
 */
@Data
public class StreamContext {

	/** 可取消的订阅对象 */
	private Disposable disposable;

	/** SSE事件发射器 */
	private Sinks.Many<ServerSentEvent<GraphNodeResponse>> sink;

	/** OpenTelemetry追踪Span */
	private Span span;

	/** 当前文本类型 */
	private TextType textType;

	/**
	 * 收集流式输出内容，用于 Langfuse 上报
	 */
	private final StringBuilder outputCollector = new StringBuilder();

	/**
	 * 追加流式输出内容
	 * @param chunk 输出片段
	 */
	public void appendOutput(String chunk) {
		outputCollector.append(chunk);
	}

	/**
	 * 获取收集到的完整输出内容
	 * @return 输出内容
	 */
	public String getCollectedOutput() {
		return outputCollector.toString();
	}

	/**
	 * 标记是否已经清理，用于防止重复清理
	 */
	private final AtomicBoolean cleaned = new AtomicBoolean(false);

	/**
	 * 清理所有资源 线程安全：使用 AtomicBoolean 确保只执行一次
	 */
	public void cleanup() {
		// 使用 compareAndSet 确保只执行一次清理
		if (!cleaned.compareAndSet(false, true)) {
			return;
		}

		// 清理 Disposable
		Disposable localDisposable = disposable;
		if (localDisposable != null && !localDisposable.isDisposed()) {
			try {
				localDisposable.dispose();
			}
			catch (Exception e) {
				// 忽略清理过程中的异常
			}
		}

		// 清理 Sink
		Sinks.Many<ServerSentEvent<GraphNodeResponse>> localSink = sink;
		if (localSink != null) {
			try {
				localSink.tryEmitComplete();
			}
			catch (Exception e) {
				// 忽略清理过程中的异常
			}
		}
	}

	/**
	 * 检查是否已经清理
	 */
	public boolean isCleaned() {
		return cleaned.get();
	}

}
