package com.phoenix.data.bo.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Schema 信息业务对象，描述数据库模式的名称和描述。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaInfoBO {

	private String name;

	private String description;

}
