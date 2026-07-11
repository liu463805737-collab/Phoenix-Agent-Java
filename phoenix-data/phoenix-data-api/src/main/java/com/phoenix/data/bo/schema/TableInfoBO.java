package com.phoenix.data.bo.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据库表信息业务对象，描述表的 schema、名称、描述、列信息及外键关系。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableInfoBO {

	private String schema;

	private String name;

	private String description;

	private String type;

	private String foreignKey;

	private List<String> primaryKeys;

	private List<ColumnInfoBO> columns;

}
