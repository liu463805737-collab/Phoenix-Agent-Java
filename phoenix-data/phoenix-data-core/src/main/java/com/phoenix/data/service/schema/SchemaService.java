package com.phoenix.data.service.schema;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.dto.datasource.SchemaInitRequest;
import com.phoenix.data.dto.schema.SchemaDTO;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 数据库模式服务接口，定义了模式初始化、文档检索和模式构建等核心方法。
 */
public interface SchemaService {

	/**
	 * 初始化指定数据源的数据库模式
	 * @param datasourceId 数据源 ID
	 * @param agentId 智能体 ID
	 * @param schemaInitRequest 模式初始化请求
	 * @return 是否初始化成功
	 * @throws Exception 初始化失败时抛出异常
	 */
	Boolean schema(Integer datasourceId,Long agentId, SchemaInitRequest schemaInitRequest) throws Exception;

	/**
	 * 根据数据源和查询条件获取表文档
	 * @param datasourceId 数据源 ID
	 * @param agentId 智能体 ID
	 * @param query 查询条件
	 * @return 表文档列表
	 */
	List<Document> getTableDocumentsByDatasource(Integer datasourceId,Long agentId, String query);

	/**
	 * 从数据库配置中提取数据库名称
	 * @param schemaDTO 模式 DTO
	 * @param dbConfig 数据库配置
	 */
	void extractDatabaseName(SchemaDTO schemaDTO, DbConfigBO dbConfig);

	/**
	 * 从文档列表构建数据库模式
	 * @param agentId 智能体 ID
	 * @param columnDocumentList 列文档列表
	 * @param tableDocuments 表文档列表
	 * @param schemaDTO 模式 DTO 输出
	 */
	void buildSchemaFromDocuments(String agentId, List<Document> columnDocumentList, List<Document> tableDocuments,
			SchemaDTO schemaDTO);

	/**
	 * 根据数据源和表名列表获取表文档
	 * @param datasourceId 数据源 ID
	 * @param tableNames 表名列表
	 * @return 表文档列表
	 */
	List<Document> getTableDocuments(Integer datasourceId, Long agentId, List<String> tableNames);

	/**
	 * 删除文档
	 * @param agentId 智能体ID
	 */
	void deletTableDocuments(Long agentId) throws Exception;

	/**
	 * 根据数据源和表名列表获取列文档
	 * @param datasourceId 数据源 ID
	 * @param tableNames 表名列表
	 * @return 列文档列表
	 */
	List<Document> getColumnDocumentsByTableName(Integer datasourceId,Long agentId, List<String> tableNames);

}
