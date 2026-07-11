package com.phoenix.data.dto.datasource;

import com.phoenix.data.bo.DbConfigBO;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 模式初始化请求 DTO
 */
public class SchemaInitRequest implements Serializable {

	private DbConfigBO dbConfig;

	private List<String> tables;

	/**
	 * 获取数据库配置
	 */
	public DbConfigBO getDbConfig() {
		return dbConfig;
	}

	/**
	 * 设置数据库配置
	 */
	public void setDbConfig(DbConfigBO dbConfig) {
		this.dbConfig = dbConfig;
	}

	/**
	 * 获取表名列表
	 */
	public List<String> getTables() {
		return tables;
	}

	/**
	 * 设置表名列表
	 */
	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	@Override
	/**
	 * 返回对象的字符串表示
	 */
	public String toString() {
		return "SchemaInitRequest{" + "dbConfig=" + dbConfig + ", tables=" + tables + '}';
	}

	/**
	 * 判断两个对象是否相等
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SchemaInitRequest that = (SchemaInitRequest) o;
		return Objects.equals(dbConfig, that.dbConfig) && Objects.equals(tables, that.tables);
	}

	/**
	 * 返回对象的哈希码
	 */
	@Override
	public int hashCode() {
		return Objects.hash(dbConfig, tables);
	}

}
