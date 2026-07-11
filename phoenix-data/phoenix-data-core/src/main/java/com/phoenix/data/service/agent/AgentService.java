package com.phoenix.data.service.agent;

import com.mybatisflex.core.service.IService;
import com.phoenix.data.entity.Agent;

import java.util.List;

/**
 * Agent 服务接口，提供 Agent 的增删改查及 API Key 管理功能。
 */
public interface AgentService extends IService<Agent> {

	List<Agent> findAll();

	Agent findById(Long id);

	List<Agent> findByIds(List<Long> ids, String status);

	/**
	 * 根据唯一标识 sn 查找 Agent
	 */
	Agent findBySn(String sn);

	/**
	 * 根据状态查找 Agent 列表
	 */
	List<Agent> findByStatus(String status);

	/**
	 * 根据关键字搜索 Agent
	 */
	List<Agent> search(String keyword);

	/**
	 * 保存 Agent（新增或更新）
	 */
	Agent saveAgent(Agent agent);

	/**
	 * 根据 sn 保存 Agent，若已存在则跳过
	 */
	void saveBySn(Agent agent);

	/**
	 * 生成 API Key
	 */
	Agent generateApiKey(Long id);

	/**
	 * 重置 API Key
	 */
	Agent resetApiKey(Long id);

	/**
	 * 删除 Agent
	 */
	void deleteById(Long id);

	/**
	 * 删除 API Key
	 */
	Agent deleteApiKey(Long id);

	/**
	 * 切换 API Key 的启用状态
	 */
	Agent toggleApiKey(Long id, boolean enabled);

	/**
	 * 获取脱敏后的 API Key
	 */
	String getApiKeyMasked(Long id);

}
