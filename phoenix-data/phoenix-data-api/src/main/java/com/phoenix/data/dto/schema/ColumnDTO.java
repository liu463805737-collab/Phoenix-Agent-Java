package com.phoenix.data.dto.schema;

import com.phoenix.data.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.ObjectMapper;

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
        return objectMapper.writeValueAsString(this);
    }

}
