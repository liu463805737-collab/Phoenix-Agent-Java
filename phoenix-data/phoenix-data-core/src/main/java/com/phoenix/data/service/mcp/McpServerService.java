package com.phoenix.data.service.mcp;

import com.phoenix.data.entity.Agent;
import com.phoenix.data.mapper.AgentMapper;
import com.phoenix.data.service.graph.GraphService;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * MCP 服务端服务，封装智能体查询和 NL2SQL 转换的 MCP 工具调用。
 */
@Service
@Transactional(rollbackFor = Exception.class)
@AllArgsConstructor
public class McpServerService {

	private final AgentMapper agentMapper;

	private GraphService graphService;

	/**
	 * 智能体列表查询请求参数
	 */
	public record AgentListRequest(
			@JsonPropertyDescription("按状态过滤，例如 '状态：draft-待发布，published-已发布，offline-已下线") String status,

			@JsonPropertyDescription("按关键词搜索智能体名称或描述") String keyword) {
	}

	/**
	 * 查询智能体列表，支持按状态和关键词过滤
	 * @param agentListRequest 查询请求参数
	 * @return 智能体列表
	 */
	@Tool(description = "查询智能体列表，支持按状态和关键词过滤。可以根据智能体的状态（如已发布PUBLISHED、草稿DRAFT等）进行过滤，也可以通过关键词搜索智能体的名称、描述或标签。返回按创建时间降序排列的智能体列表。")
	@Transactional(readOnly = true)
	public List<Agent> listAgentsToolCallback(AgentListRequest agentListRequest) {
		return agentMapper.findByConditions(agentListRequest.status(), agentListRequest.keyword());
	}

	/**
	 * NL2SQL 请求参数
	 */
	public record Nl2SqlRequest(@JsonPropertyDescription("自然语言查询描述，例如：'查询销售额最高的10个产品'") String naturalQuery,
			@JsonPropertyDescription("智能体ID，用于指定使用哪个智能体进行NL2SQL转换") String agentId) {
	}

	/**
	 * 将自然语言查询转换为 SQL 语句
	 * @param nl2SqlRequest NL2SQL 请求参数
	 * @return SQL 语句
	 * @throws GraphRunnerException 转换失败时抛出异常
	 */
	@Tool(description = "将自然语言查询转换为SQL语句。使用指定的智能体将用户的自然语言查询描述转换为可执行的SQL语句，支持复杂的数据查询需求。")
	@Transactional(readOnly = true)
	public String nl2SqlToolCallback(Nl2SqlRequest nl2SqlRequest) throws GraphRunnerException {
		Assert.hasText(nl2SqlRequest.agentId(), "AgentId cannot be empty");
		Assert.hasText(nl2SqlRequest.naturalQuery(), "Natural query cannot be empty");
		return graphService.nl2sql(nl2SqlRequest.naturalQuery(), nl2SqlRequest.agentId());
	}

}
