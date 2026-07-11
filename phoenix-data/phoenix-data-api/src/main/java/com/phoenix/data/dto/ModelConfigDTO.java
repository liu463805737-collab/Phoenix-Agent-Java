package com.phoenix.data.dto;

import com.phoenix.common.annotation.InEnum;
import com.phoenix.data.enums.ModelType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigDTO {

	private Integer id;

	@NotBlank(message = "provider must not be empty")
	private String provider; // e.g. "openai", "deepseek"

	private String apiKey; // e.g. "https://api.openai.com"

	@NotBlank(message = "baseUrl must not be empty")
	private String baseUrl;

	@NotBlank(message = "modelName must not be empty")
	private String modelName;

	@NotBlank(message = "modelType must not be empty")
	@InEnum(value = ModelType.class, message = "CHAT/EMBEDDING 之一")
	private String modelType;

	// 仅当厂商路径非标准时填写，例如 "/custom/chat"
	private String completionsPath;

	// 仅当厂商路径非标准时填写
	private String embeddingsPath;

	private Double temperature = 0.0;

	private Integer maxTokens = 2000;
	/**
	 * 1 true; 0 false
	 */
	private Boolean isActive = true;

	// 模型代理配置，默认关闭（使用直连）
	private Boolean proxyEnabled = false;

	private String proxyHost;

	private Integer proxyPort;

	private String proxyUsername;

	private String proxyPassword;

}
