import type { Agent, AgentTransport } from '@phoenix/chat-shared';

import { http } from './http';

interface BackendAgent {
  id?: number;
  type?: string;
  sn?: null | string;
  name?: string;
  description?: string;
  avatar?: null | string;
  status?: string;
  [key: string]: unknown;
}

export const realAgentTransport: AgentTransport = {
  async listAgents(): Promise<Agent[]> {
    try {
      const data = await http.get<BackendAgent[]>(
        '/platform/account-info/getMyAgents',
        { status: 'published' },
      );
      return (data ?? []).map((a) => ({
        id: String(a.id),
        name: a.name || '',
        sn: a.sn || '',
        description: a.description || '',
        avatar: a.avatar || (a.name || '').charAt(0) || '智',
        type: a.type ?? '',
      }));
    } catch {
      return [];
    }
  },
};
