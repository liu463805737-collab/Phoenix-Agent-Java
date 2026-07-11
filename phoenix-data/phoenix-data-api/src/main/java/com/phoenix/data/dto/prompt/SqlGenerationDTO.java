package com.phoenix.data.dto.prompt;

import com.phoenix.data.dto.schema.SchemaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * SQL 生成 DTO
 */
@AllArgsConstructor
@Builder
@Data
public class SqlGenerationDTO {

	private String evidence;

	private String query;

	private SchemaDTO schemaDTO;

	private String sql;

	private String exceptionMessage;

	private String executionDescription;

	private String dialect;

}
