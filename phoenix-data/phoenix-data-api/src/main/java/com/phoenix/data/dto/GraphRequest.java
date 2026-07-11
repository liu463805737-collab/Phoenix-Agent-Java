package com.phoenix.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图执行请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GraphRequest {

	private String agentId;

	private String threadId;

	private String query;

	private boolean humanFeedback;

	private String humanFeedbackContent;

	private boolean rejectedPlan;

	private boolean nl2sqlOnly;

}
