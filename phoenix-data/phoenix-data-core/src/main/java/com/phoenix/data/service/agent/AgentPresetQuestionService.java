package com.phoenix.data.service.agent;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.entity.AgentPresetQuestion;

import java.util.List;

/**
 * 预设问题服务接口，提供对 Agent 预设问题的增删改查操作。
 */
public interface AgentPresetQuestionService extends IService<AgentPresetQuestion> {

	/**
	 * 根据 Agent ID 获取预设问题列表（仅活跃的，按 sort_order 和 id 排序）
	 */
	List<AgentPresetQuestion> findByAgentId(Long agentId);

	/**
	 * 根据 Agent ID 获取所有预设问题列表（包含非活跃的，按 sort_order 和 id 排序）
	 */
	List<AgentPresetQuestion> findAllByAgentId(Long agentId);
	List<AgentPresetQuestion> findAllByAgentIdAndAccountId(Long agentId, String accountId);

	/**
	 * 创建新的预设问题
	 */
	AgentPresetQuestion create(AgentPresetQuestion question);

	/**
	 * 更新指定的预设问题
	 */
	void update(Long id, AgentPresetQuestion question);

	/**
	 * 根据 ID 删除预设问题
	 */
	void deleteById(Long id);

	/**
	 * 根据 Agent ID 删除所有预设问题
	 */
	void deleteByAgentId(Long agentId);
	void deleteByAgentIdAndAccountId(Long agentId, String accountId);
	/**
	 * 批量保存预设问题：先删除该 Agent 的所有预设问题，再插入新列表
	 */
	void batchSave(Long agentId, List<AgentPresetQuestion> questions);
	void batchSave(Long agentId,String accountId, List<AgentPresetQuestion> questions);

}
