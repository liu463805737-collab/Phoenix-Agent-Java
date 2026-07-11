package com.phoenix.data.connector.ddl;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DDL 执行器工厂，负责管理和获取各数据库类型的 DDL 执行器实例。
 */
@Component
public class DdlFactory {

	private final Map<String, Ddl> ddlExecutorSet = new ConcurrentHashMap<>();

	/**
	 * 构造函数，注入所有 DDL 执行器并注册。
	 */
	public DdlFactory(List<Ddl> ddls) {
		ddls.forEach(this::registry);
	}

	/**
	 * 注册 DDL 执行器。
	 */
	public void registry(Ddl ddlExecutor) {
		ddlExecutorSet.put(ddlExecutor.getDdlType(), ddlExecutor);
	}

	/**
	 * 判断指定类型的 DDL 执行器是否已注册。
	 */
	public boolean isRegistered(String type) {
		return ddlExecutorSet.containsKey(type);
	}

	/**
	 * 根据数据库配置获取对应的 DDL 执行器。
	 */
	public Ddl getDdlExecutorByDbConfig(DbConfigBO dbConfig) {
		BizDataSourceTypeEnum type = BizDataSourceTypeEnum.fromTypeName(dbConfig.getDialectType());
		if (type == null) {
			throw new RuntimeException("unknown db type");
		}
		return getDdlExecutorByDbType(type);
	}

	// todo: 写一层缓存
	/**
	 * 根据数据源类型枚举获取对应的 DDL 执行器。
	 */
	public Ddl getDdlExecutorByDbType(BizDataSourceTypeEnum type) {
		return ddlExecutorSet.values()
			.stream()
			.filter(d -> d.supportedDataSourceType(type))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("no ddl executor found for " + type));
	}

	/**
	 * 根据类型标识获取 DDL 执行器。
	 */
	public Ddl getDdlExecutorByType(String type) {
		return ddlExecutorSet.get(type);
	}

}
