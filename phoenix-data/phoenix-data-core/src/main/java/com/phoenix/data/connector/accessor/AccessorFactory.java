package com.phoenix.data.connector.accessor;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库访问器工厂，负责管理和获取数据源访问器实例。
 *
 * @author vlsmb
 * @since 2025/9/27
 */
@Component
public class AccessorFactory {

	/**
	 * 构造函数，注入所有 Accessor 并注册。
	 */
	public AccessorFactory(List<Accessor> accessors) {
		accessors.forEach(this::register);
	}

	private final Map<String, Accessor> accessorMap = new ConcurrentHashMap<>();

	/**
	 * 注册访问器。
	 */
	public void register(Accessor accessor) {
		accessorMap.put(accessor.getAccessorType(), accessor);
	}

	/**
	 * 判断指定类型的访问器是否已注册。
	 */
	public boolean isRegistered(String type) {
		return accessorMap.containsKey(type);
	}

	/**
	 * 根据数据库配置获取对应的访问器。
	 */
	public Accessor getAccessorByDbConfig(DbConfigBO dbConfig) {
		if (dbConfig == null) {
			throw new IllegalArgumentException("dbConfig cannot be null");
		}
		BizDataSourceTypeEnum typeEnum = Arrays.stream(BizDataSourceTypeEnum.values())
			.filter(e -> e.getDialect().equalsIgnoreCase(dbConfig.getDialectType()))
			.filter(e -> e.getProtocol().equalsIgnoreCase(dbConfig.getConnectionType()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException(
					"no accessor registered for dialect: " + dbConfig.getDialectType()));
		return getAccessorByDbTypeEnum(typeEnum);
	}

	// todo: 写一层缓存
	/**
	 * 根据数据源类型枚举获取对应的访问器。
	 */
	public Accessor getAccessorByDbTypeEnum(BizDataSourceTypeEnum typeEnum) {
		return accessorMap.values()
			.stream()
			.filter(a -> a.supportedDataSourceType(typeEnum.getTypeName()))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("no accessor registered for dialect: " + typeEnum));
	}

	/**
	 * 根据类型标识获取访问器。
	 */
	public Accessor getAccessorByType(String type) {
		return accessorMap.get(type);
	}

}
