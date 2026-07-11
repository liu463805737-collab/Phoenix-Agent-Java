package com.phoenix.data.dto.schema;

import com.phoenix.data.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息 DTO
 */
@Data
@NoArgsConstructor
public class TableDTO {

	private String name;

	private String description;

	private List<ColumnDTO> column = new ArrayList<ColumnDTO>();

	private List<String> primaryKeys;

	/**
	 * 返回表的 JSON 字符串表示
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
