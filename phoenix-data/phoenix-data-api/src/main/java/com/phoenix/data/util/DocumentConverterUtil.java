package com.phoenix.data.util;

import com.phoenix.data.bo.schema.ColumnInfoBO;
import com.phoenix.data.bo.schema.TableInfoBO;
import com.phoenix.data.constant.Constant;
import com.phoenix.data.constant.DocumentMetadataConstant;
import com.phoenix.data.entity.AgentKnowledge;
import com.phoenix.data.entity.BusinessKnowledge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for converting business objects to Document objects. Provides common
 * document conversion functionality for vector store operations.
 */
@Slf4j
public class DocumentConverterUtil {

	/**
	 * 将表列信息列表转换为文档列表，用于向量存储
	 * @param datasourceId 数据源ID
	 * @param tables 表信息列表
	 * @return 文档列表
	 */
	public static List<Document> convertColumnsToDocuments(Integer datasourceId,Long agentId, List<TableInfoBO> tables) {
		List<Document> documents = new ArrayList<>();
		for (TableInfoBO table : tables) {
			// 使用已经处理过的列数据，避免重复查询
			List<ColumnInfoBO> columns = table.getColumns();
			if (columns != null) {
				for (ColumnInfoBO column : columns) {
					documents.add(DocumentConverterUtil.convertColumnToDocument(datasourceId,agentId, table, column));
				}
			}
		}
		return documents;
	}

	/**
	 * Converts a column info object to a Document for vector storage.
	 * @param datasourceId the datasource ID
	 * @param tableInfoBO the table information containing schema details
	 * @param columnInfoBO the column information to convert
	 * @return Document object with column metadata
	 */
	public static Document convertColumnToDocument(Integer datasourceId,Long agentId, TableInfoBO tableInfoBO,
			ColumnInfoBO columnInfoBO) {
		String text = StringUtils.isBlank(columnInfoBO.getDescription()) ? columnInfoBO.getName()
				: columnInfoBO.getDescription();
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("agentId", agentId.toString());
		metadata.put("name", columnInfoBO.getName());
		metadata.put("tableName", tableInfoBO.getName());
		metadata.put("description", Optional.ofNullable(columnInfoBO.getDescription()).orElse(""));
		metadata.put("type", columnInfoBO.getType());
		metadata.put("primary", columnInfoBO.isPrimary());
		metadata.put("notnull", columnInfoBO.isNotnull());
		metadata.put(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.COLUMN);
		metadata.put(Constant.DATASOURCE_ID, datasourceId.toString());

		if (columnInfoBO.getSamples() != null) {
			metadata.put("samples", columnInfoBO.getSamples());
		}

		return new Document(text, metadata);
	}

	/**
	 * Converts a table info object to a Document for vector storage.
	 * @param datasourceId the datasource ID
	 * @param tableInfoBO the table information to convert
	 * @return Document object with table metadata
	 */
	public static Document convertTableToDocument(Integer datasourceId,Long agentId, TableInfoBO tableInfoBO) {
		String text = StringUtils.isBlank(tableInfoBO.getDescription()) ? tableInfoBO.getName()
				: tableInfoBO.getDescription();
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("schema", Optional.ofNullable(tableInfoBO.getSchema()).orElse(""));
		metadata.put("agentId", agentId.toString());
		metadata.put("name", tableInfoBO.getName());
		metadata.put("description", Optional.ofNullable(tableInfoBO.getDescription()).orElse(""));
		metadata.put("foreignKey", Optional.ofNullable(tableInfoBO.getForeignKey()).orElse(""));
		metadata.put("primaryKey", Optional.ofNullable(tableInfoBO.getPrimaryKeys()).orElse(new ArrayList<>()));
		metadata.put(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.TABLE);
		metadata.put(Constant.DATASOURCE_ID, datasourceId.toString());
		return new Document(text, metadata);
	}

	/**
	 * 将表信息列表转换为文档列表
	 * @param datasourceId 数据源ID
	 * @param tables 表信息列表
	 * @return 文档列表
	 */
	public static List<Document> convertTablesToDocuments(Integer datasourceId,Long agentId, List<TableInfoBO> tables) {
		return tables.stream()
			.map(table -> DocumentConverterUtil.convertTableToDocument(datasourceId, agentId, table))
			.collect(Collectors.toList());
	}

	/**
	 * 将业务知识对象转换为文档
	 * @param businessKnowledge 业务知识对象
	 * @return 文档对象
	 */
	public static Document convertBusinessKnowledgeToDocument(BusinessKnowledge businessKnowledge) {

		// 构建文档内容，包含业务名词、说明和同义词
		String businessTerm = businessKnowledge.getBusinessTerm();
		String description = Optional.ofNullable(businessKnowledge.getDescription()).orElse("无");
		String synonyms = Optional.ofNullable(businessKnowledge.getSynonyms()).orElse("无");

		String content = String.format("业务名词: %s, 说明: %s, 同义词: %s", businessTerm, description, synonyms);

		// 构建元数据
		Map<String, Object> metadata = new HashMap<>();
		metadata.put(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.BUSINESS_TERM);
		metadata.put(Constant.AGENT_ID, businessKnowledge.getAgentId().toString());
		metadata.put(DocumentMetadataConstant.DB_BUSINESS_TERM_ID, businessKnowledge.getId());

		return new Document(content, metadata);
	}

	/**
	 * 将QA/FAQ知识对象转换为文档
	 * @param knowledge 知识对象
	 * @return 文档对象
	 */
	public static Document convertQaFaqKnowledgeToDocument(AgentKnowledge knowledge) {
		// 使用question作为Document的content字段
		String content = knowledge.getQuestion();
		Map<String, Object> metadata = new HashMap<>();
		// answer和isRecall经常变更的放到关系数据库
		metadata.put(Constant.AGENT_ID, knowledge.getAgentId().toString());
		metadata.put(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.AGENT_KNOWLEDGE);
		metadata.put(DocumentMetadataConstant.DB_AGENT_KNOWLEDGE_ID, knowledge.getId());
		metadata.put(DocumentMetadataConstant.CONCRETE_AGENT_KNOWLEDGE_TYPE, knowledge.getType().getCode());

		return new Document(content, metadata);
	}

	/**
	 * 为文档列表添加元数据，用于DOCUMENT类型知识处理
	 * @param documents 原始文档列表
	 * @param knowledge 知识对象
	 * @return 添加了元数据的文档列表
	 */
	public static List<Document> convertAgentKnowledgeDocumentsWithMetadata(List<Document> documents,
			AgentKnowledge knowledge) {
		List<Document> documentsWithMetadata = new ArrayList<>();

		for (Document doc : documents) {
			// isRecall经常变更的放到关系数据库不放metadata中
			// 创建元数据
			Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
			metadata.put(Constant.AGENT_ID, knowledge.getAgentId().toString());
			metadata.put(DocumentMetadataConstant.DB_AGENT_KNOWLEDGE_ID, knowledge.getId());
			metadata.put(DocumentMetadataConstant.VECTOR_TYPE, DocumentMetadataConstant.AGENT_KNOWLEDGE);
			metadata.put(DocumentMetadataConstant.CONCRETE_AGENT_KNOWLEDGE_TYPE, knowledge.getType().getCode());

			// 创建带有元数据的新文档
			Document docWithMetadata = new Document(doc.getId(), doc.getText(), metadata);
			documentsWithMetadata.add(docWithMetadata);
		}
		return documentsWithMetadata;
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private DocumentConverterUtil() {
		throw new AssertionError("Cannot instantiate utility class");
	}

}
