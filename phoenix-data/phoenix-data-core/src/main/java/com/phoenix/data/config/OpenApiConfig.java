//package com.phoenix.data.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Info;
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * OpenAPI/Swagger 配置。
// */
//@Configuration
//public class OpenApiConfig {
//
//	@Bean
//	public OpenAPI dataAgentOpenApi() {
//		return new OpenAPI()
//			.info(new Info().title("DataAgent Backend API").description("DataAgent 后端接口文档").version("v1"));
//	}
//
//	@Bean
//	public GroupedOpenApi dataAgentApiGroup() {
//		return GroupedOpenApi.builder()
//			.group("data-agent")
//			.packagesToScan("com.phoenix.data.controller")
//			.pathsToMatch("/api/**")
//			.build();
//	}
//
//}
