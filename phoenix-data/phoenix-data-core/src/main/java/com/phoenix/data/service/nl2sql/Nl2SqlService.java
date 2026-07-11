package com.phoenix.data.service.nl2sql;

import com.phoenix.data.bo.DbConfigBO;
import com.phoenix.data.dto.prompt.SemanticConsistencyDTO;
import com.phoenix.data.dto.prompt.SqlGenerationDTO;
import com.phoenix.data.dto.schema.SchemaDTO;
import com.phoenix.data.util.MarkdownParserUtil;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

/**
 * NL2SQL 服务接口，定义自然语言转 SQL 的核心数据流方法。
 */
public interface Nl2SqlService {

	/**
	 * 执行语义一致性校验
	 * @param semanticConsistencyDTO 语义一致性校验参数
	 * @return ChatResponse 响应流
	 */
	Flux<ChatResponse> performSemanticConsistency(SemanticConsistencyDTO semanticConsistencyDTO);

	/**
	 * 生成或修复 SQL 语句
	 * @param sqlGenerationDTO SQL 生成参数
	 * @return SQL 文本流
	 */
	Flux<String> generateSql(SqlGenerationDTO sqlGenerationDTO);

	/**
	 * 精细化选择数据库表结构
	 * @param schemaDTO 数据库模式信息
	 * @param query 用户查询
	 * @param evidence 证据信息
	 * @param sqlGenerateSchemaMissingAdvice 缺失表建议
	 * @param specificDbConfig 指定数据库配置
	 * @param dtoConsumer 结果回调
	 * @return ChatResponse 响应流
	 */
	Flux<ChatResponse> fineSelect(SchemaDTO schemaDTO, String query, String evidence,
			String sqlGenerateSchemaMissingAdvice, DbConfigBO specificDbConfig, Consumer<SchemaDTO> dtoConsumer);

	/**
	 * 清理 SQL 字符串中的 Markdown 标记
	 * @param sql 原始 SQL 文本
	 * @return 清理后的 SQL
	 */
	default String sqlTrim(String sql) {
		return MarkdownParserUtil.extractRawText(sql).trim();
	}

}
