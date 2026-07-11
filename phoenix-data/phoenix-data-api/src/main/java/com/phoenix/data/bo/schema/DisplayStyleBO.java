package com.phoenix.data.bo.schema;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 结果集显示样式配置类，用于定义数据集的图表显示方式。
 *
 * @author fudawei
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisplayStyleBO {

	/**
	 * 图表类型，如：table, bar, line, pie等
	 */
	@JsonPropertyDescription("图表类型，取值范围：table、 bar、 line、 pie等")
	private String type;

	/**
	 * 图表标题
	 */
	@JsonPropertyDescription("图表标题")
	private String title;

	/**
	 * X轴字段名
	 */
	@JsonPropertyDescription("X轴字段名")
	private String x;

	/**
	 * Y轴字段名列表
	 */
	@JsonPropertyDescription("Y轴字段名列表")
	private List<String> y;

}
