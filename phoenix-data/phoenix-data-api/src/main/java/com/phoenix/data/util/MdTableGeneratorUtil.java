package com.phoenix.data.util;

import com.phoenix.data.bo.schema.ResultSetBO;

import java.util.List;
import java.util.Map;

/**
 * Markdown表格生成工具类
 */
public class MdTableGeneratorUtil {

	/**
	 * Convert two-dimensional array to Markdown table
	 * @param resultArr two-dimensional array
	 * @return Markdown table string
	 */
	public static String generateTable(String[][] resultArr) {
		if (resultArr == null || resultArr.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		// Header
		sb.append("| ");
		for (String col : resultArr[0]) {
			sb.append(col).append(" | ");
		}
		sb.append("\n");

		// Separator line
		sb.append("|---".repeat(resultArr[0].length)).append("|\n");

		// Data rows
		for (int i = 1; i < resultArr.length; i++) {
			sb.append("| ");
			for (String cell : resultArr[i]) {
				sb.append(cell).append(" | ");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Convert ResultSetBO to Markdown table
	 * @param resultSetBO structured data
	 * @return Markdown table string
	 */
	/**
	 * 将结构化查询结果转换为Markdown表格
	 * @param resultSetBO 结构化查询结果
	 * @return Markdown表格字符串
	 */
	public static String generateTable(ResultSetBO resultSetBO) {
		List<String> column = resultSetBO.getColumn();
		List<Map<String, String>> data = resultSetBO.getData();

		String[][] resultArr = new String[data.size() + 1][column.size()];
		int idxR = 0;

		resultArr[idxR++] = column.toArray(new String[0]);

		for (Map<String, String> kv : data) {
			String[] row = new String[column.size()];
			int idxC = 0;
			for (String c : column) {
				row[idxC++] = kv.get(c);
			}
			resultArr[idxR++] = row;
		}

		return generateTable(resultArr);
	}

}
