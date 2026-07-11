import type {
  AgentTransport,
  AuthTransport,
  ChatTransport,
} from './types';
import type { ChatMessage, ChatSession } from '../types/chat';

import { MOCK_AGENTS } from '../mocks/agents';
import { MOCK_MESSAGES } from '../mocks/messages';
import { MOCK_SESSIONS } from '../mocks/sessions';

function delay<T>(value: T, ms = 120, signal?: AbortSignal): Promise<T> {
  return new Promise((resolve, reject) => {
    if (signal?.aborted) {
      return reject(new DOMException('Aborted', 'AbortError'));
    }
    const timer = setTimeout(() => resolve(value), ms);
    const onAbort = () => {
      clearTimeout(timer);
      reject(new DOMException('Aborted', 'AbortError'));
    };
    signal?.addEventListener('abort', onAbort, { once: true });
  });
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

function uid(prefix: string): string {
  return `${prefix}-${Date.now().toString(36)}-${Math.random()
    .toString(36)
    .slice(2, 8)}`;
}

/** 仅为了 mock 形成“回复感”的固定话术池 */
const MOCK_REPLIES: string[] = [
  '收到，我先按你说的方向拆一下，等我半秒。',
  '可以。我建议从最近 7 天的数据切入，先看趋势再看结构。',
  '已经在生成了，稍后给你一版可下载的版本。',
  '你提到的这个点比较关键，我把相关指标也一起拉出来对比下。',
  '明白。我会优先关注异常波动较大的维度，避免被噪声带偏。',
];

function pickReply(): string {
  const i = Math.floor(Math.random() * MOCK_REPLIES.length);
  return MOCK_REPLIES[i] ?? MOCK_REPLIES[0]!;
}

/** mock 内部可变状态：在内存里保存“服务端”的最新会话 / 消息 */
const state = {
  sessions: clone(MOCK_SESSIONS) as ChatSession[],
  messagesByS: clone(MOCK_MESSAGES) as Record<string, ChatMessage[]>,
};

export const mockAuthTransport: AuthTransport = {
  async login(payload) {
    const token = `mock-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    return delay({
      token,
      user: {
        username: payload.username,
        displayName: payload.username,
        loginAt: Date.now(),
      },
    });
  },
  async logout() {
    return delay(undefined);
  },
  async fetchCurrentUser() {
    // mock 模式下没有真实持久化，返回 null 让 store 用自己的本地存储兜底
    return delay(null);
  },
};

export const mockAgentTransport: AgentTransport = {
  async listAgents() {
    return delay(clone(MOCK_AGENTS));
  },
};

export const mockChatTransport: ChatTransport = {
  async listSessions() {
    const list = [...state.sessions].sort((a, b) => b.updatedAt - a.updatedAt);
    return delay(clone(list));
  },
  async listMessages(sessionId) {
    return delay(clone(state.messagesByS[sessionId] ?? []));
  },
  async createSession(agentId) {
    const session: ChatSession = {
      id: uid('s'),
      title: '新会话',
      preview: '',
      agentId,
      updatedAt: Date.now(),
    };
    state.sessions.unshift(session);
    state.messagesByS[session.id] = [];
    return delay(clone(session));
  },
  async deleteSession(sessionId) {
    state.sessions = state.sessions.filter((s) => s.id !== sessionId);
    delete state.messagesByS[sessionId];
    return delay(undefined);
  },
  async renameSession(sessionId, title) {
    const target = state.sessions.find((s) => s.id === sessionId);
    if (target) {
      target.title = title;
      target.updatedAt = Date.now();
    }
    return delay(undefined);
  },
  async send(payload, signal, _onProgress?, _onNodeMessage?) {
    const { sessionId, content } = payload;
    const list = state.messagesByS[sessionId] ?? [];
    const reply: ChatMessage = {
      id: uid('m'),
      role: 'assistant',
      content: pickReply(),
      createdAt: Date.now(),
    };
    list.push({
      id: uid('m'),
      role: 'user',
      content,
      createdAt: Date.now(),
    });
    list.push(reply);
    state.messagesByS[sessionId] = list;
    const session = state.sessions.find((s) => s.id === sessionId);
    if (session) {
      session.preview = content;
      session.updatedAt = Date.now();
      if (session.title === '新会话') {
        session.title = content.length > 20 ? `${content.slice(0, 20)}…` : content;
      }
    }
    return delay(clone(reply), 320, signal);
  },
};
