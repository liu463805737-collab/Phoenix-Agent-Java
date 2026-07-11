package com.phoenix.data.strategy;

import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * 增强的批处理策略，同时考虑token数量和文本数量限制 适用于DashScope等有文本数量限制的嵌入模型
 */
public class EnhancedTokenCountBatchingStrategy implements BatchingStrategy {

	private final TokenCountBatchingStrategy tokenCountBatchingStrategy;

	private final int maxTextCount;

	/**
	 * 创建一个新的增强批处理策略
	 * @param encodingType 编码类型
	 * @param maxTokenCount 每批次最大token数
	 * @param reservePercentage 预留百分比
	 * @param maxTextCount 每批次最大文本数（例如DashScope限制为10）
	 */
	public EnhancedTokenCountBatchingStrategy(EncodingType encodingType, int maxTokenCount, double reservePercentage,
			int maxTextCount) {
		this.tokenCountBatchingStrategy = new TokenCountBatchingStrategy(encodingType, maxTokenCount,
				reservePercentage);
		this.maxTextCount = maxTextCount;
	}

	/**
	 * 将文档列表分批处理，同时考虑token数量和文本数量限制
	 * @param documents 待分批的文档列表
	 * @return 分批后的文档列表
	 */
	@Override
	public List<List<Document>> batch(List<Document> documents) {
		// 首先使用原始的TokenCountBatchingStrategy进行批处理
		List<List<Document>> tokenBasedBatches = tokenCountBatchingStrategy.batch(documents);

		// 然后对每个批次检查是否超过文本数量限制，如果超过则进一步分割
		List<List<Document>> finalBatches = new ArrayList<>();

		for (List<Document> batch : tokenBasedBatches) {
			if (batch.size() <= maxTextCount) {
				// 如果批次大小在限制内，直接添加
				finalBatches.add(batch);
			}
			else {
				// 如果批次大小超过限制，按文本数量进一步分割
				for (int i = 0; i < batch.size(); i += maxTextCount) {
					int endIndex = Math.min(i + maxTextCount, batch.size());
					finalBatches.add(batch.subList(i, endIndex));
				}
			}
		}

		return finalBatches;
	}

}
