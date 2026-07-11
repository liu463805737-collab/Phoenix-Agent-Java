import type { Agent } from '#/api/core/agent';
import {
  updateAgentApi,
  deleteAgentApi,
} from '#/api/core/agent';

export type { Agent };

const agentService = {
  async update(id: number, data: Partial<Agent>) {
    const response = await updateAgentApi(id, data);
    return response as unknown as Agent;
  },

  async delete(id: number) {
    try {
      await deleteAgentApi(id);
      return true;
    } catch {
      return false;
    }
  },
};

export default agentService;
