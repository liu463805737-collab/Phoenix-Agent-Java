import { requestClient } from '#/api/request';

export interface BusinessKnowledgeVO {
  id?: number;
  businessTerm: string;
  description: string;
  synonyms: string;
  isRecall: boolean;
  agentId: number;
  createdTime?: string;
  updatedTime?: string;
  embeddingStatus?: string;
  errorMsg?: string;
}

export interface CreateBusinessKnowledgeDTO {
  businessTerm: string;
  description: string;
  synonyms: string;
  isRecall: boolean;
  agentId: number;
}

export interface UpdateBusinessKnowledgeDTO {
  businessTerm: string;
  description: string;
  synonyms: string;
  agentId: number;
}

const API_BASE_URL = '/api/business-knowledge';

export async function getBusinessKnowledgeListApi(agentId: number, keyword?: string): Promise<BusinessKnowledgeVO[]> {
  const params: Record<string, string> = { agentId: agentId.toString() };
  if (keyword) params.keyword = keyword;
  const response = await requestClient.get<{ success: boolean; data: BusinessKnowledgeVO[] }>(
    API_BASE_URL,
    { params, responseReturn: 'body' },
  );
  if (!response.success) {
    return [];
  }
  return response.data ?? [];
}

export async function getBusinessKnowledgeApi(id: number): Promise<BusinessKnowledgeVO | null> {
  const response = await requestClient.get<{ success: boolean; data: BusinessKnowledgeVO }>(
    `${API_BASE_URL}/${id}`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    return null;
  }
  return response.data;
}

export async function createBusinessKnowledgeApi(knowledge: CreateBusinessKnowledgeDTO): Promise<void> {
  const response = await requestClient.post<{ success: boolean; message?: string }>(
    API_BASE_URL,
    knowledge,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '创建业务知识失败');
  }
}

export async function updateBusinessKnowledgeApi(id: number, knowledge: UpdateBusinessKnowledgeDTO): Promise<void> {
  const response = await requestClient.put<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/${id}`,
    knowledge,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '更新业务知识失败');
  }
}

export async function deleteBusinessKnowledgeApi(id: number): Promise<void> {
  const response = await requestClient.delete<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/${id}`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '删除业务知识失败');
  }
}

export async function recallBusinessKnowledgeApi(id: number, isRecall: boolean): Promise<void> {
  const response = await requestClient.post<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/recall/${id}`,
    null,
    { params: { isRecall }, responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '设置召回状态失败');
  }
}

export async function retryBusinessKnowledgeEmbeddingApi(id: number): Promise<void> {
  const response = await requestClient.post<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/retry-embedding/${id}`,
    undefined,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '重试向量化失败');
  }
}

export async function refreshAllKnowledgeToVectorStoreApi(agentId: string): Promise<void> {
  const response = await requestClient.post<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/refresh-vector-store`,
    null,
    { params: { agentId }, responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '同步到向量库失败');
  }
}
