package com.phoenix.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API Key response payload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {

	private String apiKey;

	private Integer apiKeyEnabled;

}
