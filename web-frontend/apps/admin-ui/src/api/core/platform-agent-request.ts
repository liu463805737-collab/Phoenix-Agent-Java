import { requestClient } from '#/api/request';

export interface ChatSessionRequestVO {
  sessionId: string;
  agentId: number;
  agentName: string;
  title: string;
  userId: string;
  username: string;
  realName: string;
  role: string;
  content: string;
  messageType: string;
  createTime: string;
}

export interface ChatSessionAgentStatVO {
  agentId: number;
  agentName: string;
  userCount: number;
  requestCount: number;
  lastRequestTime: string;
}

export interface ChatSessionUserStatVO {
  userId: string;
  username: string;
  realName: string;
  agentCount: number;
  requestCount: number;
  lastRequestTime: string;
}

export async function getAgentRequestPageApi(params: Record<string, any>) {
  return requestClient.get<{ records: ChatSessionRequestVO[]; totalRow?: number }>(
    '/monitoring/agent-request/page',
    { params },
  );
}

export async function getAgentRequestAgentStatsApi(params: Record<string, any>) {
  return requestClient.get<ChatSessionAgentStatVO[]>(
    '/monitoring/agent-request/agent-stats',
    { params },
  );
}

export async function getAgentRequestUserStatsApi(params: Record<string, any>) {
  return requestClient.get<ChatSessionUserStatVO[]>(
    '/monitoring/agent-request/user-stats',
    { params },
  );
}