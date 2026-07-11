package com.phoenix.data.bo.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库列信息业务对象，描述表中的单个字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnInfoBO {

	private String name;

	private String tableName;

	private String description;

	private String type;

	private boolean primary;

	private boolean notnull;

	private String samples;

}
