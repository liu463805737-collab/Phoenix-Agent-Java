import { requestClient } from '#/api/request';

export interface Agent {
  id?: number;
  type?: string;
  sn?: null | string;
  name?: string;
  description?: string;
  avatar?: null | string;
  status?: string;
  apiKey?: null | string;
  apiKeyEnabled?: number;
  prompt?: null | string;
  category?: null | string;
  adminId?: null | number;
  tags?: null | string;
  createTime?: string;
  updateTime?: string;
  humanReviewEnabled?: number;
}

export async function getAgentListApi(
  status?: string,
  keyword?: string,
): Promise<Agent[]> {
  const params: Record<string, string> = {};
  if (status) params.status = status;
  if (keyword) params.keyword = keyword;
  const response = await requestClient.get<{ data: Agent[]; success: boolean }>(
    '/api/agent/list',
    { params, responseReturn: 'body' },
  );
  if (!response.success) {
    return [];
  }
  return response.data ?? [];
}

export async function getAgentApi(id: number): Promise<null> {
  const response = await requestClient.get<{ data: Agent; success: boolean }>(
    `/api/agent/${id}`,
    { responseReturn: 'body' },
  );
  if (!response) {
    return null;
  }
  return response;
}

export async function createAgentApi(data: Partial<Agent>) {
  return requestClient.post<Agent>('/api/agent', data, {
    responseReturn: 'body',
  });
}

export async function updateAgentApi(id: number, data: Partial<Agent>) {
  return requestClient.put<Agent>(`/api/agent/${id}`, data, {
    responseReturn: 'body',
  });
}

export async function deleteAgentApi(id: number) {
  await requestClient.delete(`/api/agent/${id}`, { responseReturn: 'body' });
}

export interface UploadResult {
  success: boolean;
  message?: string;
  url?: string;
}

export interface AgentApiKeyResponse {
  apiKey: null | string;
  apiKeyEnabled: boolean | number;
}

// ===== 发布/下线 =====

export async function publishAgentApi(id: number) {
  return requestClient.post(`/api/agent/${id}/publish`, undefined, {
    responseReturn: 'body',
  });
}

export async function offlineAgentApi(id: number) {
  return requestClient.post(`/api/agent/${id}/offline`, undefined, {
    responseReturn: 'body',
  });
}

// ===== API Key =====

export async function getApiKeyApi(id: number) {
  return requestClient.get<AgentApiKeyResponse>(`/api/agent/${id}/api-key`, {
    responseReturn: 'body',
  });
}

export async function generateApiKeyApi(id: number) {
  return requestClient.post<AgentApiKeyResponse>(
    `/api/agent/${id}/api-key/generate`,
    undefined,
    { responseReturn: 'body' },
  );
}

export async function resetApiKeyApi(id: number) {
  return requestClient.post<AgentApiKeyResponse>(
    `/api/agent/${id}/api-key/reset`,
    undefined,
    { responseReturn: 'body' },
  );
}

export async function deleteApiKeyApi(id: number) {
  return requestClient.delete<AgentApiKeyResponse>(`/api/agent/${id}/api-key`, {
    responseReturn: 'body',
  });
}

export async function toggleApiKeyApi(id: number, enabled: boolean) {
  return requestClient.post<AgentApiKeyResponse>(
    `/api/agent/${id}/api-key/enable`,
    { enabled },
    { responseReturn: 'body' },
  );
}

// ===== 预设问题 =====

export interface PresetQuestion {
  id?: number;
  question?: string;
  answer?: string;
  sortOrder?: number;
}

export async function getPresetQuestionsApi(agentId: number) {
  return requestClient.get<PresetQuestion[]>(
    `/api/agent/${agentId}/preset-questions`,
    { responseReturn: 'body' },
  );
}

export async function batchSavePresetQuestionsApi(
  agentId: number,
  questions: PresetQuestion[],
) {
  return requestClient.post(
    `/api/agent/${agentId}/preset-questions`,
    questions,
    { responseReturn: 'body' },
  );
}

export async function deletePresetQuestionApi(
  agentId: number,
  questionId: number,
) {
  return requestClient.delete(
    `/api/agent/${agentId}/preset-questions/${questionId}`,
    { responseReturn: 'body' },
  );
}

// ===== Prompt 配置 =====

export interface PromptConfig {
  id?: number;
  agentId?: number;
  promptType?: string;
  content?: string;
  enabled?: boolean;
  priority?: number;
  description?: string;
}

export async function getPromptConfigListApi(
  promptType: string,
  agentId?: number,
) {
  const params: Record<string, string> = {};
  if (agentId) params.agentId = String(agentId);
  return requestClient.get<PromptConfig[]>(
    `/api/prompt-config/list-by-type/${promptType}`,
    { params, responseReturn: 'body' },
  );
}

export async function savePromptConfigApi(data: PromptConfig) {
  return requestClient.post('/api/prompt-config/save', data, {
    responseReturn: 'body',
  });
}

export async function togglePromptConfigApi(id: number, enabled: boolean) {
  return requestClient.post(
    `/api/prompt-config/${id}/${enabled ? 'enable' : 'disable'}`,
    undefined,
    { responseReturn: 'body' },
  );
}

export async function deletePromptConfigApi(id: number) {
  return requestClient.delete(`/api/prompt-config/${id}`, {
    responseReturn: 'body',
  });
}

export async function uploadAvatarApi(file: File): Promise<UploadResult> {
  const formData = new FormData();
  formData.append('file', file);
  const response = await fetch('/api/upload/avatar', {
    method: 'POST',
    body: formData,
  });
  if (!response.ok) {
    const text = await response.text().catch(() => '');
    throw new Error(`Upload failed: ${response.status} ${text}`);
  }
  const ct = response.headers.get('content-type') || '';
  if (ct.includes('application/json')) {
    return await response.json();
  }
  const text = await response.text();
  return { success: true, url: text };
}
