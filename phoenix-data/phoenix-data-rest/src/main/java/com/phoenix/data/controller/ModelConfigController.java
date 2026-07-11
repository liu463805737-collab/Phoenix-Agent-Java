package com.phoenix.data.controller;

import com.phoenix.data.dto.ModelConfigDTO;
import com.phoenix.data.enums.ModelType;
import com.phoenix.data.service.aimodelconfig.ModelConfigDataService;
import com.phoenix.data.service.aimodelconfig.ModelConfigOpsService;
import com.phoenix.data.vo.ApiResponse;
import com.phoenix.data.vo.ModelCheckVo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型配置控制器
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/model-config")
public class ModelConfigController {

	private final ModelConfigDataService modelConfigDataService;

	private final ModelConfigOpsService modelConfigOpsService;

	/**
	 * 获取模型配置列表
	 *
	 * @return 模型配置列表
	 */
	@GetMapping("/list")
	public ApiResponse<List<ModelConfigDTO>> list() {
		try {
			return ApiResponse.success("获取模型配置列表成功", modelConfigDataService.listConfigs());
		}
		catch (Exception e) {
			return ApiResponse.error("获取模型配置列表失败: " + e.getMessage());
		}
	}

	/**
	 * 新增模型配置
	 *
	 * @param config 模型配置
	 * @return 操作结果
	 */
	@PostMapping("/add")
	public ApiResponse<String> add(@Valid @RequestBody ModelConfigDTO config) {
		try {
			modelConfigDataService.addConfig(config);
			return ApiResponse.success("配置已保存");
		}
		catch (Exception e) {
			return ApiResponse.error("保存失败: " + e.getMessage());
		}
	}

	/**
	 * 修改模型配置
	 *
	 * @param config 模型配置
	 * @return 操作结果
	 */
	@PutMapping("/update")
	public ApiResponse<String> update(@Valid @RequestBody ModelConfigDTO config) {
		try {
			modelConfigOpsService.updateAndRefresh(config);
			return ApiResponse.success("配置已更新");
		}
		catch (Exception e) {
			return ApiResponse.error("更新失败: " + e.getMessage());
		}
	}

	/**
	 * 删除模型配置
	 *
	 * @param id 配置ID
	 * @return 操作结果
	 */
	@DeleteMapping("/{id}")
	public ApiResponse<String> delete(@PathVariable Integer id) {
		try {
			modelConfigDataService.deleteConfig(id);
			return ApiResponse.success("配置已删除");
		}
		catch (Exception e) {
			return ApiResponse.error("删除失败: " + e.getMessage());
		}
	}

	/**
	 * 启用/切换模型配置
	 *
	 * @param id 配置ID
	 * @return 操作结果
	 */
	@PostMapping("/activate/{id}")
	public ApiResponse<String> activate(@PathVariable Integer id) {
		try {
			modelConfigOpsService.activateConfig(id);
			return ApiResponse.success("模型切换成功！");
		}
		catch (Exception e) {
			return ApiResponse.error("切换失败，请检查配置是否正确: " + e.getMessage());
		}
	}

	/**
	 * 6. 连通性测试 接收前端表单里的配置参数，尝试发起一次真实调用
	 */
	@PostMapping("/test")
	public ApiResponse<String> testConnection(@Valid @RequestBody ModelConfigDTO config) {
		try {
			modelConfigOpsService.testConnection(config);
			return ApiResponse.success("连接测试成功！模型可用。");
		}
		catch (Exception e) {
			// 捕获具体的错误信息（如 401 Invalid Key, 404 Not Found 等）返回给前端
			return ApiResponse.error("连接测试失败: " + e.getMessage());
		}
	}

	/**
	 * 7. 检查模型配置是否就绪（聊天模型和嵌入模型都需要配置）
	 */
	@GetMapping("/check-ready")
	public ApiResponse<ModelCheckVo> checkReady() {
		// 检查聊天模型是否已配置且启用
		ModelConfigDTO chatModel = modelConfigDataService.getActiveConfigByType(ModelType.CHAT);
		// 检查嵌入模型是否已配置且启用
		ModelConfigDTO embeddingModel = modelConfigDataService.getActiveConfigByType(ModelType.EMBEDDING);

		boolean chatModelReady = chatModel != null;
		boolean embeddingModelReady = embeddingModel != null;
		boolean ready = chatModelReady && embeddingModelReady;

		return ApiResponse.success("模型配置检查完成",
				ModelCheckVo.builder()
					.chatModelReady(chatModelReady)
					.embeddingModelReady(embeddingModelReady)
					.ready(ready)
					.build());
	}

}
