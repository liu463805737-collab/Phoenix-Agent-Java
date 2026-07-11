package com.phoenix.data.dto.datasource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据源类型 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasourceTypeDTO {

	/**
	 * 数据源类型代码
	 */
	private Integer code;

	/**
	 * 数据源类型名称（用于后端识别）
	 */
	private String typeName;

	/**
	 * 数据库方言类型
	 */
	private String dialect;

	/**
	 * 连接协议类型
	 */
	private String protocol;

	/**
	 * 显示名称（用于前端展示）
	 */
	private String displayName;

}
