package com.phoenix.data.controller;

import com.phoenix.data.dto.knowledge.agentknowledge.AgentKnowledgeQueryDTO;
import com.phoenix.data.dto.knowledge.agentknowledge.CreateKnowledgeDTO;
import com.phoenix.data.dto.knowledge.agentknowledge.UpdateKnowledgeDTO;
import com.phoenix.data.service.file.ByteArrayMultipartFile;
import com.phoenix.data.service.knowledge.AgentKnowledgeService;
import com.phoenix.data.vo.AgentKnowledgeVO;
import com.phoenix.data.vo.ApiResponse;
import com.phoenix.data.vo.PageResponse;
import com.phoenix.data.vo.PageResult;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Agent Knowledge Management Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/agent-knowledge")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AgentKnowledgeController {

	private final AgentKnowledgeService agentKnowledgeService;

	/**
	 * Query knowledge details by ID
	 */
	@GetMapping("/{id}")
	public ApiResponse<AgentKnowledgeVO> getKnowledgeById(@PathVariable("id") Integer id) {
		try {
			AgentKnowledgeVO knowledge = agentKnowledgeService.getKnowledgeById(id);
			if (knowledge != null) {
				return ApiResponse.success("查询成功", knowledge);
			}
			else {
				return ApiResponse.error("知识不存在");
			}
		}
		catch (Exception e) {
			log.error("查询知识详情失败：{}", e.getMessage());
			return ApiResponse.error("查询知识详情失败：" + e.getMessage());
		}
	}

	/**
	 * Create knowledge,supporting file upload
	 */
	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ApiResponse<AgentKnowledgeVO>> createKnowledge(@RequestPart("agentId") String agentId,
			@RequestPart("title") String title, @RequestPart("type") String type,
			@RequestPart(value = "question", required = false) String question,
			@RequestPart(value = "content", required = false) String content,
			@RequestPart(value = "file", required = false) FilePart filePart,
			@RequestPart(value = "splitterType", required = false) String splitterType) {

		// 如果没有文件，直接同步处理
		if (filePart == null) {
			return Mono.fromCallable(() -> {
				CreateKnowledgeDTO dto = buildCreateKnowledgeDTO(agentId, title, type, question, content, null,
						splitterType);
				AgentKnowledgeVO knowledge = agentKnowledgeService.createKnowledge(dto);
				return ApiResponse.success("创建知识成功，后台向量存储开始更新，请耐心等待...", knowledge);
			}).subscribeOn(Schedulers.boundedElastic());
		}

		// 有文件时，先读取文件内容再处理
		String filename = filePart.filename();
		String fileContentType = filePart.headers().getContentType() != null
				? filePart.headers().getContentType().toString() : "application/octet-stream";

		return DataBufferUtils.join(filePart.content()).flatMap(dataBuffer -> {
			byte[] bytes = new byte[dataBuffer.readableByteCount()];
			dataBuffer.read(bytes);
			DataBufferUtils.release(dataBuffer);

			return Mono.fromCallable(() -> {
				MultipartFile multipartFile = new ByteArrayMultipartFile(bytes, filename, fileContentType);
				CreateKnowledgeDTO dto = buildCreateKnowledgeDTO(agentId, title, type, question, content, multipartFile,
						splitterType);
				AgentKnowledgeVO knowledge = agentKnowledgeService.createKnowledge(dto);
				return ApiResponse.success("创建知识成功，后台向量存储开始更新，请耐心等待...", knowledge);
			}).subscribeOn(Schedulers.boundedElastic());
		});
	}

	/**
	 * 构建创建知识的DTO对象
	 *
	 * @param agentId     智能体ID
	 * @param title       知识标题
	 * @param type        知识类型
	 * @param question    问题
	 * @param content     内容
	 * @param file        上传文件
	 * @param splitterType 分块策略类型
	 * @return 创建知识DTO
	 */
	private CreateKnowledgeDTO buildCreateKnowledgeDTO(String agentId, String title, String type, String question,
			String content, MultipartFile file, String splitterType) {
		CreateKnowledgeDTO dto = new CreateKnowledgeDTO();
		dto.setAgentId(Integer.parseInt(agentId));
		dto.setTitle(title);
		dto.setType(type);
		dto.setQuestion(question);
		dto.setContent(content);
		dto.setFile(file);
		dto.setSplitterType(splitterType);
		return dto;
	}

	/**
	 * Update knowledge
	 */
	@PutMapping("/{id}")
	public ApiResponse<AgentKnowledgeVO> updateKnowledge(@PathVariable("id") Integer id,
			@RequestBody UpdateKnowledgeDTO updateKnowledgeDto) {
		AgentKnowledgeVO knowledge = agentKnowledgeService.updateKnowledge(id, updateKnowledgeDto);
		return ApiResponse.success("更新成功", knowledge);
	}

	/**
	 * 更新知识的召回状态
	 *
	 * @param id       知识ID
	 * @param isRecall 是否召回
	 * @return 更新后的知识信息
	 */
	@PutMapping("/recall/{id}")
	public ApiResponse<AgentKnowledgeVO> updateRecallStatus(@PathVariable Integer id,
			@RequestParam(value = "isRecall") Boolean isRecall) {
		AgentKnowledgeVO agentKnowledgeVO = agentKnowledgeService.updateKnowledgeRecallStatus(id, isRecall);
		return ApiResponse.success("更新成功", agentKnowledgeVO);
	}

	/**
	 * Delete knowledge
	 */
	@DeleteMapping("/{id}")
	public ApiResponse<Boolean> deleteKnowledge(@PathVariable("id") Integer id) {
		return agentKnowledgeService.deleteKnowledge(id) ? ApiResponse.success("删除操作已接收，等待后台删除相关资源...")
				: ApiResponse.error("删除失败");
	}

	/**
	 * 分页查询知识列表
	 *
	 * @param queryDTO 查询条件
	 * @return 分页结果
	 */
	@PostMapping("/query/page")
	public PageResponse<List<AgentKnowledgeVO>> queryByPage(@Valid @RequestBody AgentKnowledgeQueryDTO queryDTO) {
		try {
			PageResult<AgentKnowledgeVO> pageResult = agentKnowledgeService.queryByConditionsWithPage(queryDTO);
			return PageResponse.success(pageResult.getData(), pageResult.getTotal(), pageResult.getPageNum(),
					pageResult.getPageSize(), pageResult.getTotalPages());
		}
		catch (Exception e) {
			log.error("分页查询知识列表失败：{}", e.getMessage());
			return PageResponse.pageError("分页查询失败：" + e.getMessage());
		}
	}

	/**
	 * 重试向量化操作
	 *
	 * @param id 知识ID
	 * @return 操作结果
	 */
	@PostMapping("/retry-embedding/{id}")
	public ApiResponse<AgentKnowledgeVO> retryEmbedding(@PathVariable Integer id) {
		agentKnowledgeService.retryEmbedding(id);
		return ApiResponse.success("重试向量化操作成功，如果是文件解析需要花费点时间，请耐心等待...");
	}

}
