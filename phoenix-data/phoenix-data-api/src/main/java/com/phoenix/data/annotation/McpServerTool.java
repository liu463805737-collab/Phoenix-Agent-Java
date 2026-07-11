package com.phoenix.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 MCP Server 工具的注解，用于解决 ChatClient 初始化时 Tool 与 ChatModel 的循环依赖问题。
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface McpServerTool {

}
