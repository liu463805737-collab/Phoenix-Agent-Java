package com.phoenix.data.service.agent;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.entity.Agent;
import com.phoenix.data.mapper.AgentMapper;
import com.phoenix.data.service.file.FileStorageService;
import com.phoenix.data.service.vectorstore.AgentVectorStoreService;
import com.phoenix.data.util.ApiKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Agent 服务实现类，处理 Agent 的 CRUD、API Key 管理及向量数据清理。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements AgentService {

	private final AgentVectorStoreService agentVectorStoreService;

	private final FileStorageService fileStorageService;

	public AgentServiceImpl(AgentVectorStoreService agentVectorStoreService, FileStorageService fileStorageService) {
		this.agentVectorStoreService = agentVectorStoreService;
		this.fileStorageService = fileStorageService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Agent> findByIds(List<Long> ids, String status) {
		return getMapper().findByIds(ids, status);
	}

	/**
	 * 获取所有 Agent 列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Agent> findAll() {
		return list();
	}

	/**
	 * 根据 ID 查找 Agent
	 */
	@Override
	@Transactional(readOnly = true)
	public Agent findById(Long id) {
		return getById(id);
	}

	/**
	 * 根据唯一标识 sn 查找 Agent
	 */
	@Override
	@Transactional(readOnly = true)
	public Agent findBySn(String sn) {
		return QueryChain.of(getMapper())
			.eq(Agent::getSn, sn)
			.one();
	}

	/**
	 * 根据状态查找 Agent 列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Agent> findByStatus(String status) {
		return QueryChain.of(getMapper())
			.eq(Agent::getStatus, status)
			.orderBy(Agent::getCreateTime, false)
			.list();
	}

	/**
	 * 根据关键字搜索 Agent
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Agent> search(String keyword) {
		return getMapper().searchByKeyword(keyword);
	}

	/**
	 * 保存 Agent，新增时设置创建/更新时间，更新时仅更新时间
	 */
	@Override
	public Agent saveAgent(Agent agent) {
		LocalDateTime now = LocalDateTime.now();

		if (agent.getId() == null) {
			// Add
			agent.setCreateTime(now);
			agent.setUpdateTime(now);
			if (agent.getApiKeyEnabled() == null) {
				agent.setApiKeyEnabled(0);
			}
			getMapper().insert(agent);
		}
		else {
			// Update
			agent.setUpdateTime(now);
			if (agent.getApiKeyEnabled() == null) {
				agent.setApiKeyEnabled(0);
			}
			getMapper().update(agent);
		}

		return agent;
	}

	/**
	 * 根据 sn 保存 Agent，若已存在则不重复插入
	 */
	@Override
	public void saveBySn(Agent agent) {
		Agent existing = QueryChain.of(getMapper())
			.eq(Agent::getSn, agent.getSn())
			.one();
		if (existing == null) {
			agent.setId(null);
			saveAgent(agent);
		}
	}

	/**
	 * 根据 ID 删除 Agent，同时清理向量数据和头像文件
	 */
	@Override
	public void deleteById(Long id) {
		try {
			// 获取头像信息用于文件清理
			Agent existing = getById(id);
			String avatar = existing != null ? existing.getAvatar() : null;

			// Delete agent record from database
			getMapper().deleteById(id);

			// Also clean up the agent's vector data
			if (agentVectorStoreService != null) {
				try {
					agentVectorStoreService.deleteDocumentsByMetedata(id.toString(), new HashMap<>());
					log.info("Successfully deleted vector data for agent: {}", id);
				}
				catch (Exception vectorException) {
					log.warn("Failed to delete vector data for agent: {}, error: {}", id, vectorException.getMessage());
					// Vector data deletion failure does not affect the main process
				}
			}

			// 清理头像文件
			try {
				if (avatar != null && !avatar.isBlank()) {
					fileStorageService.deleteFile(avatar);
					log.info("Successfully deleted avatar file: {} for agent: {}", avatar, id);
				}
			}
			catch (Exception avatarEx) {
				log.warn("Failed to cleanup avatar file: {} for agent: {}, error: {}", avatar, id,
						avatarEx.getMessage());
			}

			log.info("Successfully deleted agent: {}", id);
		}
		catch (Exception e) {
			log.error("Failed to delete agent: {}", id, e);
			throw e;
		}
	}

	/**
	 * 生成新的 API Key 并启用
	 */
	@Override
	public Agent generateApiKey(Long id) {
		Agent agent = requireAgent(id);
		String apiKey = ApiKeyUtil.generate();
		getMapper().updateApiKey(id, apiKey, 1);
		agent.setApiKey(apiKey);
		agent.setApiKeyEnabled(1);
		return agent;
	}

	/**
	 * 重置 API Key（重新生成）
	 */
	@Override
	public Agent resetApiKey(Long id) {
		return generateApiKey(id);
	}

	/**
	 * 删除 API Key
	 */
	@Override
	public Agent deleteApiKey(Long id) {
		Agent agent = requireAgent(id);
		getMapper().updateApiKey(id, null, 0);
		agent.setApiKey(null);
		agent.setApiKeyEnabled(0);
		return agent;
	}

	/**
	 * 切换 API Key 的启用/禁用状态
	 */
	@Override
	public Agent toggleApiKey(Long id, boolean enabled) {
		getMapper().toggleApiKey(id, enabled ? 1 : 0);
		Agent agent = requireAgent(id);
		agent.setApiKeyEnabled(enabled ? 1 : 0);
		return agent;
	}

	/**
	 * 获取脱敏后的 API Key（中间部分用星号代替）
	 */
	@Override
	@Transactional(readOnly = true)
	public String getApiKeyMasked(Long id) {
		Agent agent = requireAgent(id);
		String apiKey = agent.getApiKey();
		if (apiKey == null || apiKey.isBlank()) {
			return null;
		}
		return ApiKeyUtil.mask(apiKey);
	}

	/**
	 * 根据 ID 获取 Agent，不存在则抛出异常
	 */
	private Agent requireAgent(Long id) {
		Agent agent = getById(id);
		if (agent == null) {
			throw new IllegalArgumentException("Agent not found: " + id);
		}
		return agent;
	}

}
