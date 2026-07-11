package com.phoenix.data.controller;

import com.phoenix.data.dto.schema.SemanticModelAddDTO;
import com.phoenix.data.dto.schema.SemanticModelBatchImportDTO;
import com.phoenix.data.entity.SemanticModel;
import com.phoenix.data.service.semantic.SemanticModelService;
import com.phoenix.data.vo.ApiResponse;
import com.phoenix.data.vo.BatchImportResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Semantic Model Configuration Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/semantic-model")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class SemanticModelController {

	private static final String TEMPLATE_FILE_NAME = "semantic_model_template.xlsx";

	private final SemanticModelService semanticModelService;

	/**
	 * 获取语义模型列表
	 *
	 * @param keyword 搜索关键字（可选）
	 * @param agentId 智能体ID（可选）
	 * @return 语义模型列表
	 */
	@GetMapping
	public ApiResponse<List<SemanticModel>> list(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "agentId", required = false) Long agentId) {
		List<SemanticModel> result;
		if (keyword != null && !keyword.trim().isEmpty()) {
			result = semanticModelService.search(keyword);
		}
		else if (agentId != null) {
			result = semanticModelService.getByAgentId(agentId);
		}
		else {
			result = semanticModelService.getAll();
		}
		return ApiResponse.success("success list semanticModel", result);
	}

	/**
	 * 根据ID获取语义模型详情
	 *
	 * @param id 语义模型ID
	 * @return 语义模型
	 */
	@GetMapping("/{id}")
	public ApiResponse<SemanticModel> get(@PathVariable(value = "id") Long id) {
		SemanticModel model = semanticModelService.getById(id);
		return ApiResponse.success("success retrieve semanticModel", model);
	}

	/**
	 * 创建语义模型
	 *
	 * @param semanticModelAddDto 创建语义模型请求
	 * @return 创建结果
	 */
	@PostMapping
	public ApiResponse<Boolean> create(@RequestBody @Validated SemanticModelAddDTO semanticModelAddDto) {
		boolean success = semanticModelService.addSemanticModel(semanticModelAddDto);
		if (success) {
			return ApiResponse.success("Semantic model created successfully", true);
		}
		else {
			return ApiResponse.error("Failed to create semantic model");
		}
	}

	/**
	 * 更新语义模型
	 *
	 * @param id    语义模型ID
	 * @param model 更新内容
	 * @return 更新后的语义模型
	 */
	@PutMapping("/{id}")
	public ApiResponse<SemanticModel> update(@PathVariable(value = "id") Long id, @RequestBody SemanticModel model) {
		if (semanticModelService.getById(id) == null) {
			return ApiResponse.error("Semantic model not found");
		}
		model.setId(id);
		semanticModelService.updateSemanticModel(id, model);
		return ApiResponse.success("Semantic model updated successfully", model);
	}

	/**
	 * 删除语义模型
	 *
	 * @param id 语义模型ID
	 * @return 删除结果
	 */
	@DeleteMapping("/{id}")
	public ApiResponse<Boolean> delete(@PathVariable(value = "id") Long id) {
		if (semanticModelService.getById(id) == null) {
			return ApiResponse.error("Semantic model not found");
		}
		semanticModelService.deleteSemanticModel(id);
		return ApiResponse.success("Semantic model deleted successfully", true);
	}

	/**
	 * 批量删除语义模型
	 */
	@DeleteMapping("/batch")
	public ApiResponse<Boolean> batchDelete(@RequestBody @NotEmpty(message = "ID列表不能为空") List<Long> ids) {
		semanticModelService.deleteSemanticModels(ids);
		return ApiResponse.success("批量删除成功", true);
	}

	/**
	 * 批量启用语义模型
	 *
	 * @param ids 语义模型ID列表
	 * @return 操作结果
	 */
	@PutMapping("/enable")
	public ApiResponse<Boolean> enableFields(@RequestBody @NotEmpty(message = "ID列表不能为空") List<Long> ids) {
		semanticModelService.enableSemanticModels(ids);
		return ApiResponse.success("Semantic models enabled successfully", true);
	}

	/**
	 * 批量禁用语义模型
	 *
	 * @param ids 语义模型ID列表
	 * @return 操作结果
	 */
	@PutMapping("/disable")
	public ApiResponse<Boolean> disableFields(@RequestBody @NotEmpty(message = "ID列表不能为空") List<Long> ids) {
		ids.forEach(semanticModelService::disableSemanticModel);
		return ApiResponse.success("Semantic models disabled successfully", true);
	}

	/**
	 * 批量导入语义模型（JSON格式）
	 */
	@PostMapping("/batch-import")
	public ApiResponse<BatchImportResult> batchImport(@RequestBody @Valid SemanticModelBatchImportDTO dto) {
		log.info("开始批量导入语义模型: agentId={}, 数量={}", dto.getAgentId(), dto.getItems().size());
		BatchImportResult result = semanticModelService.batchImport(dto);
		log.info("批量导入完成: 总数={}, 成功={}, 失败={}", result.getTotal(), result.getSuccessCount(), result.getFailCount());
		return ApiResponse.success("批量导入完成", result);
	}

	/**
	 * 下载语义模型导入模板（Excel）
	 *
	 * @return 模板文件
	 */
	@GetMapping("/template/download")
	public ResponseEntity<byte[]> downloadTemplate() {
		try {
			ClassPathResource resource = new ClassPathResource("excel/" + TEMPLATE_FILE_NAME);
			if (!resource.exists()) {
				log.error("模板文件不存在: excel/{}", TEMPLATE_FILE_NAME);
				return ResponseEntity.notFound().build();
			}

			byte[] template;
			try (InputStream inputStream = resource.getInputStream()) {
				template = StreamUtils.copyToByteArray(inputStream);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", TEMPLATE_FILE_NAME);
			return ResponseEntity.ok().headers(headers).body(template);
		}
		catch (IOException e) {
			log.error("读取Excel模板失败", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	/**
	 * 通过Excel文件导入语义模型
	 *
	 * @param file    Excel文件
	 * @param agentId 智能体ID
	 * @return 导入结果
	 */
	@PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ApiResponse<BatchImportResult>> importExcel(@RequestPart("file") FilePart file,
			@RequestPart("agentId") String agentId) {
		Long agentIdLong = Long.parseLong(agentId);
		String filename = file.filename();

		return DataBufferUtils.join(file.content()).flatMap(dataBuffer -> {
			byte[] bytes = new byte[dataBuffer.readableByteCount()];
			dataBuffer.read(bytes);
			DataBufferUtils.release(dataBuffer);

			return Mono.fromCallable(() -> {
				try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
					BatchImportResult result = semanticModelService.importFromExcel(inputStream, filename, agentIdLong);
					return ApiResponse.success("Excel导入完成", result);
				}
			}).subscribeOn(Schedulers.boundedElastic());
		}).onErrorResume(IllegalArgumentException.class, e -> {
			log.error("Excel导入失败: {}", e.getMessage());
			return Mono.just(ApiResponse.error("Excel导入失败: " + e.getMessage()));
		}).onErrorResume(Exception.class, e -> {
			log.error("Excel导入失败", e);
			return Mono.just(ApiResponse.error("Excel导入失败: " + e.getMessage()));
		});
	}

}
