package com.phoenix.data.service.hybrid.fusion;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 融合策略接口，定义多路检索结果融合的行为
 */
public interface FusionStrategy {

	/**
	 * 融合多路检索结果
	 * @param topK 返回结果数量
	 * @param resultLists 多路检索结果列表
	 * @return 融合后的文档列表
	 */
	@SuppressWarnings("unchecked")
	List<Document> fuseResults(int topK, List<Document>... resultLists);

}
