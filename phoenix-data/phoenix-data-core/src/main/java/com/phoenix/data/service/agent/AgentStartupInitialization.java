package com.phoenix.data.service.agent;

import com.phoenix.data.entity.Agent;
import com.phoenix.data.entity.AgentDatasource;
import com.phoenix.data.service.datasource.AgentDatasourceService;
import com.phoenix.data.service.vectorstore.AgentVectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Agent 启动初始化器，应用启动时异步初始化所有已发布的 Agent 数据源到向量存储。
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class AgentStartupInitialization implements ApplicationRunner, DisposableBean {

    private final AgentService agentService;

    private final AgentVectorStoreService agentVectorStoreService;

    private final AgentDatasourceService agentDatasourceService;

    private final ExecutorService executorService;

    /**
     * 应用启动后异步初始化所有已发布的 Agent
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting automatic initialization of published agents...");

        try {
            //TODO 只能是手动创建向量库
            // 因为异步可以让初始化过程在后台运行，不会阻塞Spring启动主线程，提高启动速度和响应性；即使初始化很耗时也不会影响主程序正常启动。
//            CompletableFuture.runAsync(this::initializePublishedAgents, executorService).exceptionally(throwable -> {
//                log.error("Error during agent initialization: {}", throwable.getMessage());
//                return null;
//            });

        } catch (Exception e) {
            log.error("Failed to start agent initialization process", e);
        }
    }

    /**
     * 初始化所有已发布的 Agent，逐个处理数据源并写入向量存储
     */
    private void initializePublishedAgents() {
        try {
            List<Agent> publishedAgents = agentService.findByStatus("published");

            if (publishedAgents.isEmpty()) {
                log.info("No published agents found, skipping initialization");
                return;
            }

            log.info("Found {} published agents, starting initialization...", publishedAgents.size());

            int successCount = 0;
            int failureCount = 0;

            for (Agent agent : publishedAgents) {
                try {
                    boolean initialized = initializeAgentDataSource(agent);
                    if (initialized) {
                        successCount++;
                        log.info("Successfully initialized agent: {} (ID: {})", agent.getName(), agent.getId());
                    } else {
                        failureCount++;
                        log.warn("Failed to initialize agent: {} (ID: {}) - no active datasource or tables", agent.getName(), agent.getId());
                    }
                } catch (Exception e) {
                    failureCount++;
                    log.error("Error initializing agent: {} (ID: {}, reason: {})", agent.getName(), agent.getId(), e.getMessage());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            log.info("Agent initialization completed. Success: {}, Failed: {}, Total: {}", successCount, failureCount, publishedAgents.size());

        } catch (Exception e) {
            log.error("Error during published agents initialization", e);
        }
    }

    /**
     * 初始化单个 Agent 的数据源
     *
     * @param agent Agent 对象
     * @return 是否初始化成功
     */
    private boolean initializeAgentDataSource(Agent agent) {
        try {
            Long agentId = agent.getId();

            boolean hasData = isAlreadyInitialized(agentId);

            if (hasData) {
                log.info("Agent {} already has vector data , skipping initialization", agentId);
                return true;
            }

            AgentDatasource activeDatasource = agentDatasourceService.getCurrentAgentDatasource(agentId);

            Integer datasourceId = activeDatasource.getDatasourceId();

            List<String> tables = activeDatasource.getSelectTables();

            if (tables.isEmpty()) {
                log.warn("Datasource {} has no tables available for agent {}", datasourceId, agentId);
                return false;
            }

            log.info("Initializing agent {} with datasource {} and {} tables", agentId, datasourceId, tables.size());

            Boolean result = agentDatasourceService.initializeSchemaForAgentWithDatasource(agentId, datasourceId, tables);

            if (result) {
                log.info("Successfully initialized datasource for agent {} with {} tables", agentId, tables.size());
                return true;
            } else {
                log.error("Failed to initialize datasource for agent {}", agentId);
                return false;
            }

        } catch (Exception e) {
            log.error("Error initializing datasource for agent {}, reason: {}", agent.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 检查 Agent 是否已经初始化（向量存储中是否已有文档）
     */
    private boolean isAlreadyInitialized(Long agentId) {
        try {
            String agentIdStr = String.valueOf(agentId);
            return agentVectorStoreService.hasDocuments(agentIdStr);
        } catch (Exception e) {
            log.error("Failed to check initialization status for agent: {}, assuming not initialized", agentId, e);
            return false;
        }
    }

    /**
     * 应用关闭时清理执行器线程池资源
     */
    @Override
    public void destroy() {
        if (!executorService.isShutdown()) {
            log.info("Shutting down agent initialization executor service");
            executorService.shutdown();
        }
    }

}
