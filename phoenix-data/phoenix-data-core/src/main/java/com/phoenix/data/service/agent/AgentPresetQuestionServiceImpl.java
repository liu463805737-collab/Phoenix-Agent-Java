package com.phoenix.data.service.agent;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.data.entity.AgentPresetQuestion;
import com.phoenix.data.mapper.AgentPresetQuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AgentPresetQuestion 服务实现类，提供预设问题的数据库操作及批量保存逻辑。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AgentPresetQuestionServiceImpl extends ServiceImpl<AgentPresetQuestionMapper, AgentPresetQuestion>
		implements AgentPresetQuestionService {

	/**
	 * 根据 Agent ID 获取活跃的预设问题列表
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AgentPresetQuestion> findByAgentId(Long agentId) {
		return QueryChain.of(getMapper())
			.eq(AgentPresetQuestion::getAgentId, agentId)
			.eq(AgentPresetQuestion::getIsActive, 1)
			.orderBy(AgentPresetQuestion::getSortOrder, true)
			.orderBy(AgentPresetQuestion::getId, true)
			.list();
	}

	/**
	 * 根据 Agent ID 获取所有预设问题（包含非活跃的）
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AgentPresetQuestion> findAllByAgentId(Long agentId) {
		return QueryChain.of(getMapper())
			.eq(AgentPresetQuestion::getAgentId, agentId)
			.orderBy(AgentPresetQuestion::getSortOrder, true)
			.orderBy(AgentPresetQuestion::getId, true)
			.list();
	}

	@Override
	public List<AgentPresetQuestion> findAllByAgentIdAndAccountId(Long agentId, String accountId) {
		return QueryChain.of(this.mapper)
				.eq(AgentPresetQuestion::getAgentId, agentId)
				.eq(AgentPresetQuestion::getAccountId, accountId)
				.orderBy(AgentPresetQuestion::getSortOrder, true)
				.orderBy(AgentPresetQuestion::getId, true)
				.list();
	}

	/**
	 * 创建预设问题，设置默认排序值和活跃状态
	 */
	@Override
	public AgentPresetQuestion create(AgentPresetQuestion question) {
		if (question.getSortOrder() == null) {
			question.setSortOrder(0);
		}
		if (question.getIsActive() == null) {
			question.setIsActive(true);
		}
		getMapper().insert(question);
		return question;
	}

	/**
	 * 根据 ID 更新预设问题
	 */
	@Override
	public void update(Long id, AgentPresetQuestion question) {
		question.setId(id);
		getMapper().updateWithNow(question);
	}

	/**
	 * 根据 ID 删除预设问题
	 */
	@Override
	public void deleteById(Long id) {
		getMapper().deleteById(id);
	}

	/**
	 * 根据 Agent ID 删除所有预设问题
	 */
	@Override
	public void deleteByAgentId(Long agentId) {
		getMapper().deleteByAgentId(agentId);
	}

	@Override
	public void deleteByAgentIdAndAccountId(Long agentId, String accountId) {
		this.remove(QueryChain.of(getMapper()).eq(AgentPresetQuestion::getAgentId, agentId).eq(AgentPresetQuestion::getAccountId, accountId));
	}

	/**
	 * 批量保存：先删除该 Agent 的所有预设问题，再按顺序插入新列表
	 */
	@Override
	public void batchSave(Long agentId, List<AgentPresetQuestion> questions) {
		deleteByAgentId(agentId);
		for (int i = 0; i < questions.size(); i++) {
			AgentPresetQuestion question = questions.get(i);
			question.setAgentId(agentId);
			question.setSortOrder(i);
			if (question.getIsActive() == null) {
				question.setIsActive(true);
			}
			create(question);
		}
	}

	@Override
	public void batchSave(Long agentId, String accountId, List<AgentPresetQuestion> questions) {
		deleteByAgentIdAndAccountId(agentId, accountId);
		for (int i = 0; i < questions.size(); i++) {
			AgentPresetQuestion question = questions.get(i);
			question.setAgentId(agentId);
			question.setAccountId(accountId);
			question.setSortOrder(i);
			if (question.getIsActive() == null) {
				question.setIsActive(true);
			}
			create(question);
		}
	}
}
