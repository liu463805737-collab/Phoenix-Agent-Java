package com.phoenix.data.dto.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.phoenix.data.util.JsonUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.ObjectMapper;

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
        return objectMapper.writeValueAsString(this);
    }

}
