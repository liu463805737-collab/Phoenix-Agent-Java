import type { Agent } from '../types/agent';
import type { ChatMessage, ChatSession, SendPayload } from '../types/chat';
import type { LoginPayload, LoginResult, User } from '../types/user';

/** 认证相关接口，app 启动时通过 setTransport 注入实际实现 */
export interface AuthTransport {
  login(payload: LoginPayload): Promise<LoginResult>;
  logout(): Promise<void>;
  /** 拉取当前用户信息（页面刷新时用） */
  fetchCurrentUser(token: string): Promise<User | null>;
}

/** 智能体相关接口 */
export interface AgentTransport {
  listAgents(): Promise<Agent[]>;
}

/** 节点消息回调，用于在流式过程中将节点消息推送到 UI */
export type OnNodeMessage = (message: ChatMessage) => void;

/** 会话与消息相关接口 */
export interface ChatTransport {
  listSessions(): Promise<ChatSession[]>;
  listMessages(sessionId: string): Promise<ChatMessage[]>;
  /** 新建会话，返回会话对象 */
  createSession(agentId: string): Promise<ChatSession>;
  deleteSession(sessionId: string): Promise<void>;
  renameSession(sessionId: string, title: string): Promise<void>;
  pinSession(sessionId: string, isPinned: boolean): Promise<void>;
  /**
   * 发送一条消息，返回助手回复。
   * 若支持流式，会通过 onProgress 回调逐步返回已累积的文本。
   * onNodeMessage 在每个节点完成时回调，用于将节点消息推送到 UI。
   */
  send(
    payload: SendPayload,
    signal?: AbortSignal,
    onProgress?: (text: string) => void,
    onNodeMessage?: OnNodeMessage,
  ): Promise<ChatMessage>;
}
