import type {
  AgentKnowledge,
  AgentKnowledgeQueryDTO,
} from '#/api/core/agentKnowledge';
import {
  queryAgentKnowledgePageApi,
  updateAgentKnowledgeApi,
  deleteAgentKnowledgeApi,
  updateKnowledgeRecallStatusApi,
  retryKnowledgeEmbeddingApi,
} from '#/api/core/agentKnowledge';

export type { AgentKnowledge, AgentKnowledgeQueryDTO };

const agentKnowledgeService = {
  async queryByPage(queryDTO: AgentKnowledgeQueryDTO) {
    return queryAgentKnowledgePageApi(queryDTO);
  },

  async delete(id: number) {
    try {
      await deleteAgentKnowledgeApi(id);
      return true;
    } catch {
      return false;
    }
  },

  async update(id: number, data: Partial<AgentKnowledge>) {
    try {
      await updateAgentKnowledgeApi(id, data);
      return true;
    } catch {
      return false;
    }
  },

  async updateRecallStatus(id: number, isRecall: boolean) {
    try {
      await updateKnowledgeRecallStatusApi(id, isRecall);
      return true;
    } catch {
      return false;
    }
  },

  async retryEmbedding(id: number) {
    try {
      await retryKnowledgeEmbeddingApi(id);
      return true;
    } catch {
      return false;
    }
  },
};

export default agentKnowledgeService;
