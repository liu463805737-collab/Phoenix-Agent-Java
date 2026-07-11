package com.phoenix.data.bo.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询结果业务对象，包含结果集和显示样式配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultBO {

	private ResultSetBO resultSet;

	private DisplayStyleBO displayStyle;

}
