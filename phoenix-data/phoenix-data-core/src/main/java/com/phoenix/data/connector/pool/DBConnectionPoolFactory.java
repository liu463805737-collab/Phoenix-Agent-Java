package com.phoenix.data.connector.pool;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库连接池工厂，负责管理和获取各类型的数据源连接池实例。
 */
@Component
public class DBConnectionPoolFactory {

	private final Map<String, DBConnectionPool> poolMap = new ConcurrentHashMap<>();

	/**
	 * 构造函数，注入所有连接池并注册。
	 */
	public DBConnectionPoolFactory(List<DBConnectionPool> pools) {
		pools.forEach(this::register);
	}

	/**
	 * 注册连接池。
	 */
	public void register(DBConnectionPool pool) {
		poolMap.put(pool.getConnectionPoolType(), pool);
	}

	/**
	 * 判断指定类型的连接池是否已注册。
	 */
	public boolean isRegistered(String type) {
		return poolMap.containsKey(type);
	}

	/**
	 * 根据类型标识获取对应的连接池。
	 */
	public DBConnectionPool getPoolByType(String type) {
		DBConnectionPool direct = poolMap.get(type);
		if (direct != null) {
			return direct;
		}
		return poolMap.values().stream().filter(p -> p.supportedDataSourceType(type)).findFirst().orElse(null);
	}

	// todo: 写一层缓存
	/**
	 * 根据数据库类型获取对应的连接池。
	 */
	public DBConnectionPool getPoolByDbType(String type) {
		return poolMap.values()
			.stream()
			.filter(p -> p.supportedDataSourceType(type))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No DB connection pool found for type: " + type));
	}

}
