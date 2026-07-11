package com.phoenix.data.service.datasource.handler.registry;

import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源类型处理器注册表，管理和提供各类数据源类型对应的处理器
 */
@Component
public class DatasourceTypeHandlerRegistry {

	private final Map<String, DatasourceTypeHandler> handlerMap = new ConcurrentHashMap<>();

	/**
	 * 构造函数，自动注册所有数据源类型处理器
	 * @param handlers 处理器列表
	 */
	public DatasourceTypeHandlerRegistry(List<DatasourceTypeHandler> handlers) {
		handlers.forEach(this::register);
	}

	/**
	 * 注册数据源类型处理器
	 * @param handler 处理器
	 */
	public void register(DatasourceTypeHandler handler) {
		handlerMap.put(normalizeType(handler.typeName()), handler);
	}

	/**
	 * 检查指定类型是否已注册处理器
	 * @param type 数据源类型
	 * @return 是否已注册
	 */
	public boolean isRegistered(String type) {
		return handlerMap.containsKey(normalizeType(type));
	}

	/**
	 * 获取指定类型的处理器，如果不存在则抛出异常
	 * @param type 数据源类型
	 * @return 处理器
	 * @throws IllegalArgumentException 类型为空
	 * @throws IllegalStateException 不支持的数据库类型
	 */
	public DatasourceTypeHandler getRequired(String type) {
		if (!StringUtils.hasText(type)) {
			throw new IllegalArgumentException("Datasource type cannot be blank");
		}
		DatasourceTypeHandler handler = handlerMap.get(normalizeType(type));
		if (handler == null) {
			throw new IllegalStateException("Unsupported datasource type: " + type);
		}
		return handler;
	}

	/**
	 * 规范化类型名称（去空格、转小写）
	 * @param type 原始类型
	 * @return 规范化后的类型
	 */
	private String normalizeType(String type) {
		if (!StringUtils.hasText(type)) {
			return "";
		}
		return type.trim().toLowerCase(Locale.ROOT);
	}

}
