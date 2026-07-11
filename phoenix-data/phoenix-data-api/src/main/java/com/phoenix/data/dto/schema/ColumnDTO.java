package com.phoenix.data.dto.schema;

import com.phoenix.data.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 列信息 DTO
 */
@Data
@NoArgsConstructor
public class ColumnDTO {

	private String name;

	private String description;

	private int enumeration;

	private String range;

	private String type;

	private List<String> data;

	private Map<String, String> mapping;

	/**
	 * 返回列的 JSON 字符串表示
	 */
	@Override
	public String toString() {
		ObjectMapper objectMapper = JsonUtil.getObjectMapper();
		try {
			return objectMapper.writeValueAsString(this);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to convert object to JSON string", e);
		}
	}

}
