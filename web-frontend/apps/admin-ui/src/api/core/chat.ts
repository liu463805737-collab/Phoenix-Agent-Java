import { requestClient } from '#/api/request';

export interface ChatSession {
  id: string;
  agentId: number;
  title: string;
  status: string;
  isPinned: boolean;
  userId?: number;
  createTime?: string;
  updateTime?: string;
}

export interface ChatMessage {
  id?: number;
  sessionId: string;
  role: string;
  content: string;
  messageType: string;
  metadata?: string;
  createTime?: string;
  titleNeeded?: boolean;
}

const API_BASE_URL = '/api';

export async function getAgentSessionsApi(agentId: number): Promise<ChatSession[]> {
  const response = await requestClient.get<{ success: boolean; data: ChatSession[] }>(
    `${API_BASE_URL}/agent/${agentId}/sessions`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    return [];
  }
  return response.data ?? [];
}

export async function createSessionApi(agentId: number, title?: string, userId?: number): Promise<ChatSession | null> {
  const response = await requestClient.post<{ success: boolean; data: ChatSession }>(
    `${API_BASE_URL}/agent/${agentId}/sessions`,
    { title, userId },
    { responseReturn: 'body' },
  );
  if (!response.success) {
    return null;
  }
  return response.data;
}

export async function clearAgentSessionsApi(agentId: number): Promise<void> {
  const response = await requestClient.delete<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/agent/${agentId}/sessions`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '清除会话失败');
  }
}

export async function getSessionMessagesApi(sessionId: string): Promise<ChatMessage[]> {
  const response = await requestClient.get<{ success: boolean; data: ChatMessage[] }>(
    `${API_BASE_URL}/sessions/${sessionId}/messages`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    return [];
  }
  return response.data ?? [];
}

export async function saveMessageApi(sessionId: string, message: ChatMessage): Promise<void> {
  const response = await requestClient.post<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/sessions/${sessionId}/messages`,
    { ...message, sessionId },
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '保存消息失败');
  }
}

export async function pinSessionApi(sessionId: string, isPinned: boolean): Promise<void> {
  const response = await requestClient.put<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/sessions/${sessionId}/pin`,
    null,
    { params: { isPinned }, responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '置顶操作失败');
  }
}

export async function renameSessionApi(sessionId: string, title: string): Promise<void> {
  const response = await requestClient.put<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/sessions/${sessionId}/rename`,
    null,
    { params: { title: title.trim() }, responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '重命名失败');
  }
}

export async function deleteSessionApi(sessionId: string): Promise<void> {
  const response = await requestClient.delete<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/sessions/${sessionId}`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '删除会话失败');
  }
}

export async function downloadHtmlReportApi(sessionId: string, content: string): Promise<void> {
  const token = localStorage.getItem('phoenix-token');
  const response = await fetch(`${API_BASE_URL}/api/sessions/${sessionId}/reports/html`, {
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain;charset=utf-8',
      ...(token ? { 'phoenix-token': token } : {}),
    },
    body: content,
  });
  const blob = await response.blob();
  const contentDisposition = response.headers.get('content-disposition');
  let filename = 'report.html';
  if (contentDisposition) {
    const filenameMatch = contentDisposition.match(/filename="?([^;"]+)"?/);
    if (filenameMatch?.[1]) {
      filename = filenameMatch[1];
    }
  }
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
