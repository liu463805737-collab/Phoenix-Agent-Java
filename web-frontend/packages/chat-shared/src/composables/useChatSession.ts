import { storeToRefs } from 'pinia';
import { computed } from 'vue';

import { useAgentStore } from '../stores/agent';
import { useChatStore } from '../stores/chat';

/**
 * 聚合聊天页所需的状态与动作，view 只关心渲染与事件分发。
 */
export function useChatSession() {
  const chat = useChatStore();
  const agent = useAgentStore();

  const {
    sessions,
    activeSessionId,
    activeSession,
    activeMessages,
    sending,
    loadingMessages,
  } = storeToRefs(chat);

  const { agents, activeAgent } = storeToRefs(agent);

  const currentAgent = computed(() => {
    if (activeSession.value) {
      return (
        agents.value.find((a) => a.id === activeSession.value!.agentId) ?? null
      );
    }
    return activeAgent.value;
  });

  async function send(content: string) {
    await chat.send(content);
  }

  async function switchSession(id: string) {
    await chat.switchSession(id);
  }

  async function createSessionWithAgent(agentId: string) {
    return chat.createSession(agentId);
  }

  return {
    sessions,
    activeSessionId,
    activeSession,
    activeMessages,
    sending,
    loadingMessages,
    currentAgent,
    send,
    switchSession,
    createSessionWithAgent,
  };
}
