package com.phoenix.data.bo.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结果集业务对象，包含列名列表和数据行集合，支持克隆。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class ResultSetBO implements Cloneable {

	private List<String> column;

	private List<Map<String, String>> data;

	private String errorMsg;

	/**
	 * 克隆结果集，对列名和数据行进行深拷贝。
	 * @return 克隆后的 ResultSetBO 实例
	 */
	@Override
	public ResultSetBO clone() {
		return ResultSetBO.builder()
			.column(new ArrayList<>(this.column))
			.data(this.data.stream().map(HashMap::new).collect(Collectors.toList()))
			.build();
	}

}
