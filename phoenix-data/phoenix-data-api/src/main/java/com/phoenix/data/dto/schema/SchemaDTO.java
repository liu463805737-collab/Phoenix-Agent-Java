package com.phoenix.data.dto.schema;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模式信息 DTO
 */
@Data
@NoArgsConstructor
public class SchemaDTO {

	private String name;

	private String description;

	private Integer tableCount;

	private List<TableDTO> table;

	private List<String> foreignKeys;

	/**
	 * 返回模式的字符串表示
	 */
	@Override
	public String toString() {
		return "SchemaDTO{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", tableCount="
				+ tableCount + ", table=" + table + ", foreignKeys=" + foreignKeys + '}';
	}

}
