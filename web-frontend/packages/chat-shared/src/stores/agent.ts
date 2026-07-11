import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

import type { Agent } from '../types/agent';
import type { AgentTransport } from '../transport/types';

import { mockAgentTransport } from '../transport/mock';

export const useAgentStore = defineStore('phoenix-chat-shared/agent', () => {
  let transport: AgentTransport = mockAgentTransport;

  const agents = ref<Agent[]>([]);
  const activeAgentId = ref<string | null>(null);
  const loading = ref(false);

  const activeAgent = computed<Agent | null>(() => {
    if (!activeAgentId.value) return null;
    return agents.value.find((a) => a.id === activeAgentId.value) ?? null;
  });

  function setTransport(next: AgentTransport) {
    transport = next;
  }

  function setActiveAgent(id: string | null) {
    activeAgentId.value = id;
  }

  async function loadAll() {
    loading.value = true;
    try {
      const list = await transport.listAgents();
      agents.value = list;
      if (!activeAgentId.value && list.length > 0) {
        activeAgentId.value = list[0]!.id;
      }
    } finally {
      loading.value = false;
    }
  }

  return {
    agents,
    activeAgentId,
    activeAgent,
    loading,
    setTransport,
    setActiveAgent,
    loadAll,
  };
});
