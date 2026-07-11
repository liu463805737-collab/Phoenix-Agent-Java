package com.phoenix.data.dto.knowledge.businessknowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Business knowledge management entity class
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBusinessKnowledgeDTO {

	@NotBlank(message = "Business term cannot be empty")
	private String businessTerm; // Business term

	@NotBlank(message = "Description cannot be empty")
	private String description; // Description

	private String synonyms; // Synonyms, comma separated

	@Builder.Default
	private Boolean isRecall = true; // Whether to recall

	@NotNull(message = "Agent ID cannot be Null")
	private Long agentId; // Associated agent ID

}
