package com.phoenix.data.bo.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库基本信息业务对象，描述数据库的标识和描述信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseInfoBO {

	private String name;

	private String description;

}
