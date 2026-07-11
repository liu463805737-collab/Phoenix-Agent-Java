import { requestClient } from '#/api/request';

export interface AgentKnowledge {
  id?: number;
  agentId?: number;
  title?: string;
  content?: string;
  type?: string;
  question?: string;
  isRecall?: boolean;
  embeddingStatus?: string;
  splitterType?: string;
  errorMsg?: string;
  createdTime?: string;
  updatedTime?: string;
}

export interface AgentKnowledgeQueryDTO {
  agentId: number;
  title?: string;
  type?: string;
  embeddingStatus?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface KnowledgePageResult<T> {
  success: boolean;
  data: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  totalPages: number;
  message?: string;
}

const API_BASE_URL = '/api/agent-knowledge';

export async function queryAgentKnowledgePageApi(queryDTO: AgentKnowledgeQueryDTO) {
  const response = await requestClient.post<KnowledgePageResult<AgentKnowledge> & { message?: string }>(
    `${API_BASE_URL}/query/page`,
    queryDTO,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '查询知识列表失败');
  }
  return response;
}

export async function listAgentKnowledgeByAgentIdApi(
  agentId: number,
  type?: string,
  status?: string,
  keyword?: string,
): Promise<AgentKnowledge[]> {
  const params: Record<string, string | number> = {};
  if (type) params.type = type;
  if (status) params.status = status;
  if (keyword) params.keyword = keyword;
  const response = await requestClient.get<{ success: boolean; data: AgentKnowledge[] }>(
    `${API_BASE_URL}/${agentId}`,
    { params, responseReturn: 'body' },
  );
  if (!response.success) {
    return [];
  }
  return response.data ?? [];
}

export async function getAgentKnowledgeApi(id: number): Promise<AgentKnowledge | null> {
  const response = await requestClient.get<{ success: boolean; data: AgentKnowledge }>(
    `${API_BASE_URL}/${id}`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    return null;
  }
  return response.data;
}

export async function createAgentKnowledgeApi(knowledge: AgentKnowledge): Promise<AgentKnowledge | null> {
  const response = await requestClient.post<{ success: boolean; data: AgentKnowledge; message?: string }>(
    `${API_BASE_URL}/create`,
    knowledge,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '创建知识失败');
  }
  return response.data;
}

export async function updateAgentKnowledgeApi(id: number, knowledge: Partial<AgentKnowledge>) {
  const response = await requestClient.put<{ success: boolean; data: AgentKnowledge; message?: string }>(
    `${API_BASE_URL}/${id}`,
    knowledge,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '更新知识失败');
  }
  return response.data;
}

export async function updateKnowledgeRecallStatusApi(id: number, isRecall: boolean) {
  const response = await requestClient.put<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/recall/${id}`,
    null,
    { params: { isRecall }, responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '更新召回状态失败');
  }
}

export async function deleteAgentKnowledgeApi(id: number) {
  const response = await requestClient.delete<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/${id}`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '删除知识失败');
  }
}

export async function retryKnowledgeEmbeddingApi(id: number) {
  const response = await requestClient.post<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/retry-embedding/${id}`,
    undefined,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '重试向量化失败');
  }
}

export async function getKnowledgeStatisticsApi(agentId: number) {
  return requestClient.get<{ totalCount: number; typeStatistics: Array<[string, number]> }>(
    `${API_BASE_URL}/statistics/${agentId}`,
    { responseReturn: 'body' },
  );
}
