package com.phoenix.data.dto.prompt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 证据查询重写 DTO
 */
@NoArgsConstructor
@Setter
@Getter
public class EvidenceQueryRewriteDTO {

	/**
	 * 重写后的完整句子
	 */
	@JsonProperty("standalone_query")
	@JsonPropertyDescription("重写后的完整句子")
	private String standaloneQuery;

}
