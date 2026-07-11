package com.phoenix.data.controller;

import com.phoenix.data.dto.knowledge.businessknowledge.CreateBusinessKnowledgeDTO;
import com.phoenix.data.dto.knowledge.businessknowledge.UpdateBusinessKnowledgeDTO;
import com.phoenix.data.service.business.BusinessKnowledgeService;
import com.phoenix.data.vo.ApiResponse;
import com.phoenix.data.vo.BusinessKnowledgeVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业务知识控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/business-knowledge")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class BusinessKnowledgeController {

	private final BusinessKnowledgeService businessKnowledgeService;

	/**
	 * 获取业务知识列表
	 *
	 * @param agentIdStr 智能体ID
	 * @param keyword    搜索关键字（可选）
	 * @return 业务知识列表
	 */
	@GetMapping
	public ApiResponse<List<BusinessKnowledgeVO>> list(@RequestParam(value = "agentId") String agentIdStr,
			@RequestParam(value = "keyword", required = false) String keyword) {
		List<BusinessKnowledgeVO> result;
		Long agentId = Long.parseLong(agentIdStr);

		if (StringUtils.hasText(keyword)) {
			result = businessKnowledgeService.searchKnowledge(agentId, keyword);
		}
		else {
			result = businessKnowledgeService.getKnowledge(agentId);
		}
		return ApiResponse.success("success list businessKnowledge", result);
	}

	/**
	 * 根据ID获取业务知识详情
	 *
	 * @param id 业务知识ID
	 * @return 业务知识详情
	 */
	@GetMapping("/{id}")
	public ApiResponse<BusinessKnowledgeVO> get(@PathVariable(value = "id") Long id) {
		BusinessKnowledgeVO vo = businessKnowledgeService.getKnowledgeById(id);
		if (vo == null) {
			return ApiResponse.error("businessKnowledge not found");
		}
		return ApiResponse.success("success get businessKnowledge", vo);
	}

	/**
	 * 创建业务知识
	 *
	 * @param knowledge 创建业务知识请求
	 * @return 创建后的业务知识
	 */
	@PostMapping
	public ApiResponse<BusinessKnowledgeVO> create(@RequestBody @Validated CreateBusinessKnowledgeDTO knowledge) {
		return ApiResponse.success("success create businessKnowledge",
				businessKnowledgeService.addKnowledge(knowledge));
	}

	/**
	 * 更新业务知识
	 *
	 * @param id        业务知识ID
	 * @param knowledge 更新请求
	 * @return 更新后的业务知识
	 */
	@PutMapping("/{id}")
	public ApiResponse<BusinessKnowledgeVO> update(@PathVariable(value = "id") Long id,
			@RequestBody UpdateBusinessKnowledgeDTO knowledge) {

		return ApiResponse.success("success update businessKnowledge",
				businessKnowledgeService.updateKnowledge(id, knowledge));
	}

	/**
	 * 删除业务知识
	 *
	 * @param id 业务知识ID
	 * @return 删除结果
	 */
	@DeleteMapping("/{id}")
	public ApiResponse<Boolean> delete(@PathVariable(value = "id") Long id) {
		if (businessKnowledgeService.getKnowledgeById(id) == null) {
			return ApiResponse.error("businessKnowledge not found");
		}
		businessKnowledgeService.deleteKnowledge(id);
		return ApiResponse.success("success delete businessKnowledge");
	}

	/**
	 * 更新业务知识的召回状态
	 *
	 * @param id       业务知识ID
	 * @param isRecall 是否召回
	 * @return 操作结果
	 */
	@PostMapping("/recall/{id}")
	public ApiResponse<Boolean> recallKnowledge(@PathVariable(value = "id") Long id,
			@RequestParam(value = "isRecall") Boolean isRecall) {
		businessKnowledgeService.recallKnowledge(id, isRecall);
		return ApiResponse.success("success update recall businessKnowledge");
	}

	/**
	 * 将所有业务知识刷新到向量存储
	 *
	 * @param agentId 智能体ID
	 * @return 刷新结果
	 */
	@PostMapping("/refresh-vector-store")
	public ApiResponse<Boolean> refreshAllKnowledgeToVectorStore(@RequestParam(value = "agentId") String agentId) {
		// 校验 agentId 不为空和空字符串
		if (!StringUtils.hasText(agentId)) {
			return ApiResponse.error("agentId cannot be empty");
		}

		try {
			businessKnowledgeService.refreshAllKnowledgeToVectorStore(agentId);
			return ApiResponse.success("success refresh vector store");
		}
		catch (Exception e) {
			log.error("Failed to refresh vector store for agentId: {}", agentId, e);
			return ApiResponse.error("Failed to refresh vector store");
		}
	}

	/**
	 * 重试业务知识的向量化操作
	 *
	 * @param id 业务知识ID
	 * @return 操作结果
	 */
	@PostMapping("/retry-embedding/{id}")
	public ApiResponse<Boolean> retryEmbedding(@PathVariable(value = "id") Long id) {
		businessKnowledgeService.retryEmbedding(id);
		return ApiResponse.success("success retry embedding");
	}

}
