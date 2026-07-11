package com.phoenix.data.service.knowledge;

import com.phoenix.data.entity.AgentKnowledge;
import com.phoenix.data.mapper.AgentKnowledgeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能体知识资源兜底清理定时任务，定期清理已被软删除的知识关联的向量和文件资源
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AgentKnowledgeResourceCleanerTask {

	/** 知识Mapper */
	private final AgentKnowledgeMapper mapper;

	/** 知识资源管理器 */
	private final AgentKnowledgeResourceManager resourceManager;

	/**
	 * 每隔 1 小时执行一次兜底清理 cron = "0 0 * * * ?" (整点执行)
	 */
	/**
	 * 每隔1小时执行一次兜底清理，处理30分钟前已软删除的资源
	 */
	@Scheduled(cron = "0 0 * * * ?")
	public void cleanupZombieResources() {
		log.info("Starting zombie resources cleanup task...");

		// 1. 定义时间缓冲：只处理 30 分钟前删除的数据
		// 这样不会跟用户刚刚操作的异步任务冲突
		LocalDateTime timeBuffer = LocalDateTime.now().minusMinutes(30);
		int batchSize = 100;

		// 2. 查询脏数据
		List<AgentKnowledge> dirtyRecords = mapper.selectDirtyRecords(timeBuffer, batchSize);

		if (dirtyRecords.isEmpty()) {
			log.info("No zombie resources found. Task finished.");
			return;
		}

		log.info("Found {} zombie records to clean.", dirtyRecords.size());

		// 3. 逐条清理
		for (AgentKnowledge knowledge : dirtyRecords) {
			try {
				cleanupSingleRecord(knowledge);
			}
			catch (Exception e) {
				// 单条失败不影响其他记录，只记录日志，等下个周期再试
				log.error("Failed to clean resources for ID: {}", knowledge.getId(), e);
			}
		}
	}

	/**
	 * 清理单条记录的向量和文件资源
	 * @param knowledge 知识记录
	 */
	private void cleanupSingleRecord(AgentKnowledge knowledge) {
		Integer id = knowledge.getId();

		// A. 删除向量 (复用 ResourceManager 的逻辑)
		boolean vectorDeleted = resourceManager.deleteFromVectorStore(knowledge.getAgentId(), id);

		// B. 删除文件
		boolean fileDeleted = resourceManager.deleteKnowledgeFile(knowledge);

		// C. 如果都清理干净了，更新数据库状态
		if (vectorDeleted && fileDeleted) {
			knowledge.setIsResourceCleaned(1);
			knowledge.setUpdatedTime(LocalDateTime.now());
			mapper.update(knowledge);
			log.info("Zombie resource cleaned: ID={}", id);
		}
		else {
			log.warn("Partial cleanup for ID={}. VectorDel={}, FileDel={}", id, vectorDeleted, fileDeleted);
		}
	}

}
