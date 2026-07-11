package com.phoenix.data.event;

import com.phoenix.data.entity.AgentKnowledge;
import com.phoenix.data.enums.EmbeddingStatus;
import com.phoenix.data.mapper.AgentKnowledgeMapper;
import com.phoenix.data.service.knowledge.AgentKnowledgeResourceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * 智能体知识事件监听器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AgentKnowledgeEventListener {

	private final AgentKnowledgeMapper agentKnowledgeMapper;

	private final AgentKnowledgeResourceManager agentKnowledgeResourceManager;

	/**
	 * phase = TransactionPhase.AFTER_COMMIT 核心作用：只有当 Service 层的主事务提交成功后，才会执行这个方法。
	 */
	@Async("dbOperationExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleEmbeddingEvent(AgentKnowledgeEmbeddingEvent event) {
		log.info("Received AgentKnowledgeEmbeddingEvent. agentKnowledgeId: {}", event.getKnowledgeId());
		Integer id = event.getKnowledgeId();

		// 1. 查询数据
		AgentKnowledge knowledge = agentKnowledgeMapper.selectById(id);
		if (knowledge == null) {
			log.error("Knowledge not found during async processing. Id: {}", id);
			return;
		}

		try {
			// 2. 更新状态为 PROCESSING
			updateStatus(knowledge, EmbeddingStatus.PROCESSING, null);

			// 3. 执行核心向量化逻辑
			agentKnowledgeResourceManager.doEmbedingToVectorStore(knowledge);

			// 4. 更新状态为 COMPLETED
			updateStatus(knowledge, EmbeddingStatus.COMPLETED, null);

			log.info("Successfully embedded knowledge. Id: {}", id);

		}
		catch (Exception e) {
			log.error("Failed to embed knowledge. Id: {}", id, e);
			// 5. 失败处理
			updateStatus(knowledge, EmbeddingStatus.FAILED, e.getMessage());
		}
		log.info("Finished processing AgentKnowledgeEmbeddingEvent. agentKnowledgeId: {}", event.getKnowledgeId());

	}

	/**
	 * 更新知识向量化状态
	 *
	 * @param knowledge 知识对象
	 * @param status 目标状态
	 * @param errorMsg 错误信息（可为null）
	 */
	private void updateStatus(AgentKnowledge knowledge, EmbeddingStatus status, String errorMsg) {
		knowledge.setEmbeddingStatus(status);
		knowledge.setUpdatedTime(LocalDateTime.now());
		if (errorMsg != null) {
			// 截断错误信息防止数据库报错
			knowledge.setErrorMsg(errorMsg.length() > 250 ? errorMsg.substring(0, 250) : errorMsg);
		}
		agentKnowledgeMapper.update(knowledge);
	}

	/**
	 * 异步处理知识删除事件 - 清理向量和文件资源
	 *
	 * @param event 知识删除事件
	 */
	@Async("dbOperationExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleDeletionEvent(AgentKnowledgeDeletionEvent event) {
		Integer id = event.getKnowledgeId();
		log.info("Starting async resource cleanup for knowledgeId: {}", id);

		// 1. 重新查询
		AgentKnowledge knowledge = agentKnowledgeMapper.selectByIdIncludeDeleted(id);
		if (knowledge == null) {
			log.warn("Knowledge record physically missing, skipping cleanup. ID: {}", id);
			return;
		}

		try {
			// 2. 删除向量
			boolean vectorDeleted = agentKnowledgeResourceManager.deleteFromVectorStore(knowledge.getAgentId(), id);

			// 3. 删除文件
			boolean fileDeleted = agentKnowledgeResourceManager.deleteKnowledgeFile(knowledge);

			// 4. 更新清理状态
			if (vectorDeleted && fileDeleted) {
				// 只有都成功了，才标记为资源已清理
				knowledge.setIsResourceCleaned(1);
				knowledge.setUpdatedTime(LocalDateTime.now());
				agentKnowledgeMapper.update(knowledge);
				log.info("Resources cleaned up successfully. AgentKnowledgeID: {}", id);
			}
			else {
				log.error("Cleanup incomplete. AgentKnowledgeID: {}, VectorDeleted: {}, FileDeleted: {}", id,
						vectorDeleted, fileDeleted);
				// isResourceCleaned=0，有定时任务兜底清理。
			}

		}
		catch (Exception e) {
			log.error("Exception during async cleanup for agentKnowledgeId: {}", id, e);
		}
	}

}
