package com.phoenix.data.dto.prompt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 语义一致性检查 DTO
 */
@AllArgsConstructor
@Builder
@Data
public class SemanticConsistencyDTO {

	private String dialect;

	private String sql;

	private String executionDescription;

	private String schemaInfo;

	private String userQuery;

	private String evidence;

}
