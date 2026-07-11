package com.phoenix.data.dto.knowledge.businessknowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新业务知识 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBusinessKnowledgeDTO {

	@NotBlank(message = "Business term cannot be empty")
	private String businessTerm;

	@NotBlank(message = "Description cannot be empty")
	private String description;

	// Synonyms, comma separated
	private String synonyms;

	@NotNull(message = "Agent ID cannot be Null")
	private Long agentId;

}
