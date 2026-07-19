import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

import type { ChatMessage, ChatSession } from '../types/chat';
import type { ChatTransport, OnNodeMessage } from '../transport/types';

import { mockChatTransport } from '../transport/mock';

export const useChatStore = defineStore('phoenix-chat-shared/chat', () => {
  let transport: ChatTransport = mockChatTransport;

  const sessions = ref<ChatSession[]>([]);
  const messagesByS = ref<Record<string, ChatMessage[]>>({});
  const activeSessionId = ref<string | null>(null);

  const loadingSessions = ref(false);
  const loadingMessages = ref(false);
  const sending = ref(false);

  let abortController: AbortController | null = null;

  const activeSession = computed<ChatSession | null>(() => {
    if (!activeSessionId.value) return null;
    return sessions.value.find((s) => s.id === activeSessionId.value) ?? null;
  });

  const activeMessages = computed<ChatMessage[]>(() => {
    if (!activeSessionId.value) return [];
    return messagesByS.value[activeSessionId.value] ?? [];
  });

  function setTransport(next: ChatTransport) {
    transport = next;
  }

  async function loadSessions() {
    loadingSessions.value = true;
    try {
      const list = await transport.listSessions();
      sessions.value = list;
      if (!activeSessionId.value && list.length > 0) {
        await switchSession(list[0]!.id);
      }
    } finally {
      loadingSessions.value = false;
    }
  }

  async function loadMessages(sessionId: string, force = false) {
    if (!force && messagesByS.value[sessionId]) return;
    loadingMessages.value = true;
    try {
      const list = await transport.listMessages(sessionId);
      messagesByS.value = { ...messagesByS.value, [sessionId]: list };
    } finally {
      loadingMessages.value = false;
    }
  }

  async function switchSession(id: string) {
    stopSending();
    activeSessionId.value = id;
    await loadMessages(id);
  }

  async function createSession(agentId: string) {
    stopSending();
    const session: ChatSession = {
      id: `temp-${Date.now()}`,
      title: '新会话',
      preview: '',
      agentId,
      updatedAt: Date.now(),
    };
    sessions.value = [session, ...sessions.value];
    messagesByS.value = { ...messagesByS.value, [session.id]: [] };
    activeSessionId.value = session.id;
    return session;
  }

  async function persistCurrentSessionIfNeeded() {
    const session = activeSession.value;
    if (!session || !session.id.startsWith('temp-')) return;
    const persisted = await transport.createSession(session.agentId);
    const oldId = session.id;
    const idx = sessions.value.findIndex((s) => s.id === oldId);
    if (idx >= 0) {
      persisted.updatedAt = persisted.updatedAt ?? Date.now();
      sessions.value[idx] = persisted;
    }
    const msgs = messagesByS.value[oldId] ?? [];
    const next = { ...messagesByS.value };
    delete next[oldId];
    next[persisted.id] = msgs;
    messagesByS.value = next;
    activeSessionId.value = persisted.id;
  }

  async function deleteSession(id: string) {
    if (!id.startsWith('temp-')) {
      await transport.deleteSession(id);
    }
    sessions.value = sessions.value.filter((s) => s.id !== id);
    const next = { ...messagesByS.value };
    delete next[id];
    messagesByS.value = next;
    if (activeSessionId.value === id) {
      const fallback = sessions.value[0]?.id ?? null;
      activeSessionId.value = fallback;
      if (fallback) await loadMessages(fallback);
    }
  }

  async function renameSession(id: string, title: string) {
    const target = sessions.value.find((s) => s.id === id);
    if (target) {
      target.title = title;
      target.updatedAt = Date.now();
    }
    if (!id.startsWith('temp-')) {
      await transport.renameSession(id, title);
    }
  }

  async function pinSession(id: string, isPinned: boolean) {
    const target = sessions.value.find((s) => s.id === id);
    if (target) {
      target.isPinned = isPinned;
      target.updatedAt = Date.now();
    }
    if (!id.startsWith('temp-')) {
      await transport.pinSession(id, isPinned);
    }
  }

  async function send(content: string) {
    const trimmed = content.trim();
    if (!trimmed) return;
    if (!activeSessionId.value) return;
    if (sending.value) return;
    await persistCurrentSessionIfNeeded();
    const sessionId = activeSessionId.value;
    // 乐观写入用户消息
    const msgs = messagesByS.value[sessionId] ?? [];
    const optimistic: ChatMessage = {
      id: `local-${Date.now()}`,
      role: 'user',
      content: trimmed,
      createdAt: Date.now(),
    };
    messagesByS.value = {
      ...messagesByS.value,
      [sessionId]: [...msgs, optimistic],
    };

    let streamMsgId: string | null = null;

    // 报告文本流式回调：创建/更新占位消息
    function updateStreamMessage(text: string) {
      const msgs = messagesByS.value[sessionId] ?? [];
      if (!streamMsgId) {
        streamMsgId = `stream-${Date.now()}`;
        msgs.push({
          id: streamMsgId,
          role: 'assistant',
          content: '',
          createdAt: Date.now(),
          streaming: true,
        });
      }
      const idx = msgs.findIndex((m) => m.id === streamMsgId);
      if (idx >= 0) {
        msgs[idx] = { ...msgs[idx]!, content: text };
        messagesByS.value = { ...messagesByS.value, [sessionId]: [...msgs] };
      }
    }

    // 节点回调：将已完成的节点消息直接推入消息列表
    const onNodeMessage: OnNodeMessage = (nodeMsg) => {
      const msgs = messagesByS.value[sessionId] ?? [];
      msgs.push(nodeMsg);
      messagesByS.value = { ...messagesByS.value, [sessionId]: [...msgs] };
    };

    sending.value = true;
    abortController = new AbortController();
    const sendTimeout = setTimeout(
      () => abortController?.abort(),
      600_000,
    );
    try {
      const currentSession = sessions.value.find((s) => s.id === sessionId);
      const reply = await transport.send(
        { sessionId, content: trimmed, agentId: currentSession?.agentId },
        abortController.signal,
        (text: string) => {
          updateStreamMessage(text);
        },
        (nodeMsg) => {
          onNodeMessage(nodeMsg);
        },
      );
      clearTimeout(sendTimeout);
      if (abortController?.signal.aborted) return;

      // 将占位消息（报告文本流）替换为最终回复
      if (streamMsgId) {
        const msgs = messagesByS.value[sessionId] ?? [];
        const idx = msgs.findIndex((m) => m.id === streamMsgId);
        if (idx >= 0) {
          msgs[idx] = { ...reply, streaming: false };
          messagesByS.value = { ...messagesByS.value, [sessionId]: [...msgs] };
        } else {
          messagesByS.value = {
            ...messagesByS.value,
            [sessionId]: [...msgs, { ...reply, streaming: false }],
          };
        }
      } else {
        const msgs = messagesByS.value[sessionId] ?? [];
        messagesByS.value = {
          ...messagesByS.value,
          [sessionId]: [...msgs, { ...reply, streaming: false }],
        };
      }

      // 更新会话预览 / 排序
      const session = currentSession;
      if (session) {
        session.preview = trimmed;
        session.updatedAt = Date.now();
        if (session.title === '新会话') {
          session.title =
            trimmed.length > 20 ? `${trimmed.slice(0, 20)}…` : trimmed;
        }
        sessions.value = [...sessions.value].sort(
          (a, b) => b.updatedAt - a.updatedAt,
        );
      }
    } catch (err: any) {
      // 移除流式占位消息
      const msgs = messagesByS.value[sessionId] ?? [];
      messagesByS.value = {
        ...messagesByS.value,
        [sessionId]: msgs.filter((m) => m.id !== streamMsgId),
      };
      if (err?.name === 'AbortError') return;
      throw err;
    } finally {
      clearTimeout(sendTimeout);
      sending.value = false;
      abortController = null;
    }
  }

  function stopSending() {
    if (abortController) {
      abortController.abort();
      abortController = null;
    }
  }

  function reset() {
    sessions.value = [];
    messagesByS.value = {};
    activeSessionId.value = null;
  }

  return {
    sessions,
    messagesByS,
    activeSessionId,
    activeSession,
    activeMessages,
    loadingSessions,
    loadingMessages,
    sending,
    setTransport,
    loadSessions,
    loadMessages,
    switchSession,
    createSession,
    persistCurrentSessionIfNeeded,
    deleteSession,
    renameSession,
    pinSession,
    send,
    stopSending,
    reset,
  };
});
