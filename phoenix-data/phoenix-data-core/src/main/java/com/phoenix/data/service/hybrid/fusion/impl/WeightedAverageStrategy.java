package com.phoenix.data.service.hybrid.fusion.impl;

import com.phoenix.data.service.hybrid.fusion.FusionStrategy;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 加权平均融合策略（尚未实现）
 */
public class WeightedAverageStrategy implements FusionStrategy {

	/**
	 * 加权平均融合（尚未实现）
	 * @param topK 返回结果数量
	 * @param resultLists 多路检索结果列表
	 * @return 融合后的文档列表
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Document> fuseResults(int topK, List<Document>... resultLists) {
		throw new UnsupportedOperationException("Not implemented");
	}

}
