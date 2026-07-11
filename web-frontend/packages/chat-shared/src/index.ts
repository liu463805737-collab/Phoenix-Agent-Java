// Types
export type {
  ChatMessage,
  ChatRole,
  ChatSession,
  SendPayload,
} from './types/chat';
export type { Agent } from './types/agent';
export type { LoginPayload, LoginResult, User } from './types/user';

// Transport contracts + mock impl
export type {
  AgentTransport,
  AuthTransport,
  ChatTransport,
} from './transport/types';
export {
  mockAgentTransport,
  mockAuthTransport,
  mockChatTransport,
} from './transport/mock';

// Stores
export { configureAuthStorage, useAuthStore } from './stores/auth';
export { useAgentStore } from './stores/agent';
export { useChatStore } from './stores/chat';

// Composables
export { useChatSession } from './composables/useChatSession';
export { useLoginFlow } from './composables/useLoginFlow';

// Mocks (供 app 在 dev 或测试时直接消费)
export { MOCK_AGENTS } from './mocks/agents';
export { MOCK_MESSAGES } from './mocks/messages';
export { MOCK_SESSIONS } from './mocks/sessions';
