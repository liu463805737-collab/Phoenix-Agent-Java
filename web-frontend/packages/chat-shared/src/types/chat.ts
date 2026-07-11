export type ChatRole = 'assistant' | 'user';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
  /** 创建时间戳 */
  createdAt: number;
  /** 流式中标记，true 时 UI 显示打字光标；预留给后续接入 */
  streaming?: boolean;
  /** 消息类型：text | html | result-set | markdown-report | html-report */
  messageType?: string;
  /** 消息附加元数据 */
  metadata?: any;
}

export interface ChatSession {
  id: string;
  /** 标题，新建会话时可由首条消息推断 */
  title: string;
  /** 末条消息预览 */
  preview: string;
  /** 关联的 agent id */
  agentId: string;
  /** 最近一次更新时间戳（用于排序 / 展示） */
  updatedAt: number;
  /** 是否置顶 */
  isPinned?: boolean;
}

export interface SendPayload {
  sessionId: string;
  content: string;
  agentId?: string;
}
