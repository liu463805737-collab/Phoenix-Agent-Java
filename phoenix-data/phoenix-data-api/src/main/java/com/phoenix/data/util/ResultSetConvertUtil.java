package com.phoenix.data.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Convert JDBC ResultSet to structured data
 */
public class ResultSetConvertUtil {

	/**
	 * 将JDBC ResultSet转换为字符串数组列表
	 * @param rs JDBC结果集
	 * @return 字符串数组列表（第一行为列标题）
	 * @throws SQLException 数据库访问异常
	 */
	public static List<String[]> convert(ResultSet rs) throws SQLException {
		ResultSetMetaData data = rs.getMetaData();
		int columnsCount = data.getColumnCount();
		List<String[]> list = new ArrayList<>();
		String[] rowHead = new String[columnsCount];

		for (int i = 1; i <= columnsCount; i++) {
			rowHead[i - 1] = data.getColumnLabel(i);
		}

		list.add(rowHead);

		while (rs.next()) {
			String[] rowData = new String[columnsCount];
			int idx = 0;
			for (String head : rowHead) {
				rowData[idx++] = rs.getString(head) == null ? "" : rs.getString(head);
			}
			list.add(rowData);
		}

		return list;
	}

}
