package com.phoenix.data.connector;

import com.phoenix.data.bo.DbConfigBO;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

/**
 * 数据库查询参数封装类。
 */
public class DbQueryParameter {

	private String aliuid;

	private String workspaceId;

	private String region;

	private String secretArn;

	private String dbInstanceId;

	private String database;

	private String schema;

	private String table;

	private String tablePattern;

	private List<String> tables;

	private String column;

	private String sql;

	/**
	 * 默认构造函数。
	 */
	public DbQueryParameter() {
	}

	/**
	 * 全参构造函数。
	 */
	public DbQueryParameter(String aliuid, String workspaceId, String region, String secretArn, String dbInstanceId,
			String database, String schema, String table, String tablePattern, List<String> tables, String column,
			String sql) {
		this.aliuid = aliuid;
		this.workspaceId = workspaceId;
		this.region = region;
		this.secretArn = secretArn;
		this.dbInstanceId = dbInstanceId;
		this.database = database;
		this.schema = schema;
		this.table = table;
		this.tablePattern = tablePattern;
		this.tables = tables;
		this.column = column;
		this.sql = sql;
	}

	/**
	 * 获取阿里云用户ID。
	 */
	public String getAliuid() {
		return aliuid;
	}

	/**
	 * 设置阿里云用户ID。
	 */
	public DbQueryParameter setAliuid(String aliuid) {
		this.aliuid = aliuid;
		return this;
	}

	/**
	 * 获取工作空间ID。
	 */
	public String getWorkspaceId() {
		return workspaceId;
	}

	/**
	 * 设置工作空间ID。
	 */
	public DbQueryParameter setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
		return this;
	}

	/**
	 * 获取地域信息。
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * 设置地域信息。
	 */
	public DbQueryParameter setRegion(String region) {
		this.region = region;
		return this;
	}

	/**
	 * 获取密钥ARN。
	 */
	public String getSecretArn() {
		return secretArn;
	}

	/**
	 * 设置密钥ARN。
	 */
	public DbQueryParameter setSecretArn(String secretArn) {
		this.secretArn = secretArn;
		return this;
	}

	/**
	 * 获取数据库实例ID。
	 */
	public String getDbInstanceId() {
		return dbInstanceId;
	}

	/**
	 * 设置数据库实例ID。
	 */
	public DbQueryParameter setDbInstanceId(String dbInstanceId) {
		this.dbInstanceId = dbInstanceId;
		return this;
	}

	/**
	 * 获取数据库名称。
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * 设置数据库名称。
	 */
	public DbQueryParameter setDatabase(String database) {
		this.database = database;
		return this;
	}

	/**
	 * 获取模式名称。
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * 设置模式名称。
	 */
	public DbQueryParameter setSchema(String schema) {
		this.schema = schema;
		return this;
	}

	/**
	 * 获取表名。
	 */
	public String getTable() {
		return table;
	}

	/**
	 * 设置表名。
	 */
	public DbQueryParameter setTable(String table) {
		this.table = table;
		return this;
	}

	/**
	 * 获取表名匹配模式。
	 */
	public String getTablePattern() {
		return tablePattern;
	}

	/**
	 * 设置表名匹配模式。
	 */
	public DbQueryParameter setTablePattern(String tablePattern) {
		this.tablePattern = tablePattern;
		return this;
	}

	/**
	 * 获取表名列表。
	 */
	public List<String> getTables() {
		return tables;
	}

	/**
	 * 设置表名列表。
	 */
	public DbQueryParameter setTables(List<String> tables) {
		this.tables = tables;
		return this;
	}

	/**
	 * 获取列名。
	 */
	public String getColumn() {
		return column;
	}

	/**
	 * 设置列名。
	 */
	public DbQueryParameter setColumn(String column) {
		this.column = column;
		return this;
	}

	/**
	 * 获取SQL语句。
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * 设置SQL语句。
	 */
	public DbQueryParameter setSql(String sql) {
		this.sql = sql;
		return this;
	}

	/**
	 * 从数据库配置对象创建查询参数。
	 */
	public static DbQueryParameter from(DbConfigBO config) {
		DbQueryParameter param = new DbQueryParameter();
		BeanUtils.copyProperties(config, param);
		return param;
	}

	/**
	 * 返回对象的字符串表示形式。
	 */
	@Override
	public String toString() {
		return "DbQueryParameter{" + "aliuid='" + aliuid + '\'' + ", workspaceId='" + workspaceId + '\'' + ", region='"
				+ region + '\'' + ", secretArn='" + secretArn + '\'' + ", dbInstanceId='" + dbInstanceId + '\''
				+ ", database='" + database + '\'' + ", schema='" + schema + '\'' + ", table='" + table + '\''
				+ ", tablePattern='" + tablePattern + '\'' + ", tables=" + tables + ", column='" + column + '\''
				+ ", sql='" + sql + '\'' + '}';
	}

	/**
	 * 判断两个对象是否相等。
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DbQueryParameter that = (DbQueryParameter) o;
		return Objects.equals(aliuid, that.aliuid) && Objects.equals(workspaceId, that.workspaceId)
				&& Objects.equals(region, that.region) && Objects.equals(secretArn, that.secretArn)
				&& Objects.equals(dbInstanceId, that.dbInstanceId) && Objects.equals(database, that.database)
				&& Objects.equals(schema, that.schema) && Objects.equals(table, that.table)
				&& Objects.equals(tablePattern, that.tablePattern) && Objects.equals(tables, that.tables)
				&& Objects.equals(column, that.column) && Objects.equals(sql, that.sql);
	}

	/**
	 * 返回对象的哈希码值。
	 */
	@Override
	public int hashCode() {
		return Objects.hash(aliuid, workspaceId, region, secretArn, dbInstanceId, database, schema, table, tablePattern,
				tables, column, sql);
	}

}
