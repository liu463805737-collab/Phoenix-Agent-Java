package com.phoenix.data.connector;

import com.phoenix.data.bo.schema.ResultSetBO;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC ResultSet 结果集构建器，用于将 ResultSet 转换为结构化结果对象。
 */
public class ResultSetBuilder {

	/**
	 * 从 ResultSet 构建结果集对象。
	 */
	public static ResultSetBO buildFrom(ResultSet rs, String schema) throws SQLException {
		ResultSetMetaData data = rs.getMetaData();
		int columnsCount = data.getColumnCount();
		ResultSetBO resultSetBO = new ResultSetBO();
		String[] rowHead = new String[columnsCount];

		for (int i = 1; i <= columnsCount; i++) {
			rowHead[i - 1] = data.getColumnLabel(i);
		}

		List<Map<String, String>> resultSetData = Lists.newArrayList();
		int count = 0;

		while (rs.next() && count < SqlExecutor.RESULT_SET_LIMIT) {
			Map<String, String> kv = new HashMap<>();
			for (String h : rowHead) {
				kv.put(h, rs.getString(h) == null ? "" : rs.getString(h));
			}
			resultSetData.add(kv);
			count++;
		}

		// Clean column names
		List<String> cleanedHead = cleanColumnNames(Arrays.asList(rowHead));
		List<Map<String, String>> cleanedData = cleanResultSet(resultSetData);

		resultSetBO.setColumn(cleanedHead);
		resultSetBO.setData(cleanedData);

		return resultSetBO;
	}

	/**
	 * 清理列名（去除反引号和双引号）。
	 */
	private static List<String> cleanColumnNames(List<String> columnNames) {
		return columnNames.stream().map(name -> StringUtils.remove(StringUtils.remove(name, "`"), "\"")).toList();
	}

	/**
	 * 清理结果集数据（去除键中的反引号和双引号）。
	 */
	private static List<Map<String, String>> cleanResultSet(List<Map<String, String>> data) {
		return data.stream().map(row -> {
			Map<String, String> cleanedRow = new HashMap<>();
			row.forEach((k, v) -> {
				String cleanedKey = StringUtils.remove(k, "`");
				cleanedKey = StringUtils.remove(cleanedKey, "\"");
				cleanedRow.put(cleanedKey, v);
			});
			return cleanedRow;
		}).toList();
	}

}
