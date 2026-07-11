package com.phoenix.data.dto.prompt;

/**
 * Prompt configuration request DTO
 *
 * @author Makoto
 */

public record PromptConfigDTO(String id, // Configuration ID (required for update)
		String name, // Configuration name
		String promptType, // Prompt type
		Long agentId, // Associated agent ID, null means global
		String optimizationPrompt, // User-defined system prompt content
		Boolean enabled, // Whether to enable this configuration
		String description, // Configuration description
		String creator, // Creator
		Integer priority, // Configuration priority
		Integer displayOrder // Configuration display order
) {

}
