import type {
  BusinessKnowledgeVO,
  CreateBusinessKnowledgeDTO,
  UpdateBusinessKnowledgeDTO,
} from '#/api/core/businessKnowledge';
import {
  getBusinessKnowledgeListApi,
  deleteBusinessKnowledgeApi,
  recallBusinessKnowledgeApi,
  updateBusinessKnowledgeApi,
  createBusinessKnowledgeApi,
  refreshAllKnowledgeToVectorStoreApi,
  retryBusinessKnowledgeEmbeddingApi,
} from '#/api/core/businessKnowledge';

export type { BusinessKnowledgeVO, CreateBusinessKnowledgeDTO, UpdateBusinessKnowledgeDTO };

const businessKnowledgeService = {
  async list(agentId: number, keyword?: string) {
    return getBusinessKnowledgeListApi(agentId, keyword);
  },

  async delete(id: number) {
    try {
      await deleteBusinessKnowledgeApi(id);
      return true;
    } catch {
      return false;
    }
  },

  async recallKnowledge(id: number, isRecall: boolean) {
    try {
      await recallBusinessKnowledgeApi(id, isRecall);
      return true;
    } catch {
      return false;
    }
  },

  async update(id: number, data: UpdateBusinessKnowledgeDTO) {
    try {
      await updateBusinessKnowledgeApi(id, data);
      return true;
    } catch {
      return false;
    }
  },

  async create(data: CreateBusinessKnowledgeDTO) {
    try {
      await createBusinessKnowledgeApi(data);
      return true;
    } catch {
      return false;
    }
  },

  async refreshAllKnowledgeToVectorStore(agentId: string) {
    await refreshAllKnowledgeToVectorStoreApi(agentId);
  },

  async retryEmbedding(id: number) {
    try {
      await retryBusinessKnowledgeEmbeddingApi(id);
      return true;
    } catch {
      return false;
    }
  },
};

export default businessKnowledgeService;
