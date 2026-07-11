const API_BASE_URL = import.meta.env.VITE_GLOB_API_URL || '/api';
const TOKEN_KEY = 'mobile-ui:auth:token';

function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export interface FrontChatStreamRequest {
  sessionId: string;
  content: string;
  agentSn: string;
  type: string;
}

export interface StreamNodeResponse {
  agentId?: string;
  threadId?: string;
  nodeName: string;
  textType: string;
  text: string;
  error?: boolean;
  complete?: boolean;
}

export function streamFrontChat(
  request: FrontChatStreamRequest,
  onMessage: (response: { content: string; end: boolean }) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
): () => void {
  const url = `${API_BASE_URL}/api/front/stream/chat`;
  const controller = new AbortController();

  const doFetch = async () => {
    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'phoenix-token': getToken(),
          'Content-Type': 'application/json',
          Accept: 'text/event-stream',
        },
        body: JSON.stringify(request),
        signal: controller.signal,
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      if (!reader) throw new Error('No reader available');

      let buffer = '';
      let currentData = '';

      const dispatchEvent = () => {
        if (currentData) {
          try {
            const parsed = JSON.parse(currentData);
            if (parsed.end) {
              onComplete();
              return;
            }
            onMessage(parsed);
          } catch {
            // skip malformed data
          }
        }
        currentData = '';
      };

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split('\n');
        buffer = parts.pop() || '';
        for (const line of parts) {
          if (line === '') {
            dispatchEvent();
          } else if (line.startsWith('data:')) {
            currentData = line.slice(5).trim();
          }
        }
      }
      if (buffer.trim()) {
        const line = buffer.trim();
        if (line.startsWith('data:')) {
          currentData = line.slice(5).trim();
          dispatchEvent();
        }
      }
    } catch (error: any) {
      if (error.name === 'AbortError') return;
      onError(error);
    }
  };

  doFetch();
  return () => controller.abort();
}

export function streamFrontChatSql(
  params: {
    agentId: string;
    query: string;
    threadId?: string;
    humanFeedback?: boolean;
    nl2sqlOnly?: boolean;
    rejectedPlan?: boolean;
  },
  onMessage: (response: StreamNodeResponse) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
): () => void {
  const qs = new URLSearchParams();
  qs.append('agentId', params.agentId);
  if (params.threadId) qs.append('threadId', params.threadId);
  qs.append('query', params.query);
  qs.append('humanFeedback', String(params.humanFeedback ?? false));
  qs.append('nl2sqlOnly', String(params.nl2sqlOnly ?? false));
  qs.append('rejectedPlan', String(params.rejectedPlan ?? false));

  const url = `${API_BASE_URL}/api/front/stream/chatsql?${qs.toString()}`;
  const controller = new AbortController();

  const doFetch = async () => {
    try {
      const response = await fetch(url, {
        headers: {
          'phoenix-token': getToken(),
          Accept: 'text/event-stream',
        },
        signal: controller.signal,
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      if (!reader) throw new Error('No reader available');

      let buffer = '';
      let currentEvent = '';
      let currentData = '';

      const dispatchEvent = () => {
        if (currentEvent === 'complete') {
          onComplete();
          return;
        }
        if (currentData) {
          try {
            const parsed = JSON.parse(currentData);
            onMessage(parsed);
          } catch {
            // skip
          }
        }
        currentEvent = '';
        currentData = '';
      };

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split('\n');
        buffer = parts.pop() || '';
        for (const line of parts) {
          if (line === '') {
            dispatchEvent();
          } else if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim();
          } else if (line.startsWith('data:')) {
            currentData = line.slice(5).trim();
          }
        }
      }
    } catch (error: any) {
      if (error.name === 'AbortError') return;
      onError(error);
    }
  };

  doFetch();
  return () => controller.abort();
}
