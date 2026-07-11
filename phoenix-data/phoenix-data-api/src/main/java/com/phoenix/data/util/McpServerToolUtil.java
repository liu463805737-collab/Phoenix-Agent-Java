package com.phoenix.data.util;

import com.phoenix.data.annotation.McpServerTool;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * MCP Server 工具工具类，用于排除带有@McpServerTool注解的Bean
 */
public final class McpServerToolUtil {

	/**
	 * 从Spring容器中排除带有@McpServerTool注解的指定类型Bean
	 * @param context Spring应用上下文
	 * @param type Bean类型
	 * @return 排除后的Bean列表
	 */
	public static <T> List<T> excludeMcpServerTool(GenericApplicationContext context, Class<T> type) {
		String[] namesForType = context.getBeanNamesForType(type);
		Set<String> namesForAnnotation = Set.of(context.getBeanNamesForAnnotation(McpServerTool.class));
		return Arrays.stream(namesForType)
			.filter(name -> !namesForAnnotation.contains(name))
			.map(name -> context.getBean(name, type))
			.toList();
	}

}
