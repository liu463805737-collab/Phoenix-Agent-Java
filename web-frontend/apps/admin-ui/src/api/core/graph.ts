export interface GraphRequest {
  agentId: string;
  threadId?: string;
  query: string;
  humanFeedback: boolean;
  humanFeedbackContent?: string;
  rejectedPlan: boolean;
  nl2sqlOnly: boolean;
}

export interface GraphNodeResponse {
  agentId: string;
  threadId: string;
  nodeName: string;
  textType: TextType;
  text: string;
  error: boolean;
  complete: boolean;
}

export enum TextType {
  JSON = 'JSON',
  PYTHON = 'PYTHON',
  SQL = 'SQL',
  HTML = 'HTML',
  MARK_DOWN = 'MARK_DOWN',
  RESULT_SET = 'RESULT_SET',
  TEXT = 'TEXT',
}

const API_BASE_URL = '/api';

export interface ChatApiRequest {
  sessionId: string;
  content: string;
  agentSn: string;
  type: string;
}

export function streamChat(
  request: ChatApiRequest,
  onMessage: (response: GraphNodeResponse) => Promise<void>,
  onError?: (error: Error) => Promise<void>,
  onComplete?: () => Promise<void>,
): () => void {
  const url = `${API_BASE_URL}/api/admin/agent/chat`;
  const controller = new AbortController();

  const doFetch = async () => {
    try {
      const token = localStorage.getItem('phoenix-token');
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'phoenix-token': token || '',
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
      if (!reader) {
        throw new Error('No reader available');
      }

      let buffer = '';
      let currentData = '';

      const dispatchEvent = async () => {
        if (currentData) {
          try {
            const parsed = JSON.parse(currentData);
            if (parsed.end || parsed.complete) {
              await onComplete?.();
              return;
            }
            if (parsed.error) {
              await onError?.(new Error(parsed.content || parsed.text || 'Unknown error'));
              return;
            }
            const textTypeValue = parsed.textType || 'TEXT';
            const nodeResponse: GraphNodeResponse = {
              agentId: request.agentSn,
              threadId: request.sessionId,
              nodeName: parsed.nodeName || 'Agent',
              textType: Object.values(TextType).includes(textTypeValue)
                ? (textTypeValue as TextType)
                : TextType.TEXT,
              text: parsed.content || parsed.text || '',
              error: false,
              complete: false,
            };
            await onMessage(nodeResponse);
          } catch {
            await onError?.(new Error('Failed to parse server response'));
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
            await dispatchEvent();
          } else if (line.startsWith('data:')) {
            currentData = line.slice(5).trim();
          }
        }
      }
      if (buffer) {
        const line = buffer.trim();
        if (line.startsWith('data:')) {
          currentData = line.slice(5).trim();
          await dispatchEvent();
        }
      }
    } catch (error: any) {
      if (error.name === 'AbortError') return;
      await onError?.(new Error('Stream connection failed'));
    }
  };

  doFetch();

  return () => {
    controller.abort();
  };
}

export function streamSearch(
  request: GraphRequest,
  onMessage: (response: GraphNodeResponse) => Promise<void>,
  onError?: (error: Error) => Promise<void>,
  onComplete?: () => Promise<void>,
): () => void {
  const params = new URLSearchParams();
  params.append('agentId', request.agentId);
  if (request.threadId) {
    params.append('threadId', request.threadId);
  }
  params.append('query', request.query);
  params.append('humanFeedback', request.humanFeedback.toString());
  params.append('rejectedPlan', request.rejectedPlan.toString());
  params.append('nl2sqlOnly', request.nl2sqlOnly.toString());

  if (request.humanFeedbackContent) {
    params.append('humanFeedbackContent', request.humanFeedbackContent);
  }

  const url = `${API_BASE_URL}/api/admin/agent/stream/chatsql?${params.toString()}`;
  const controller = new AbortController();
  let isCompleted = false;

  const doFetch = async () => {
    try {
      const token = localStorage.getItem('phoenix-token');
      const response = await fetch(url, {
        headers: {
          'phoenix-token': token || '',
          Accept: 'text/event-stream',
        },
        signal: controller.signal,
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      if (!reader) {
        throw new Error('No reader available');
      }

      let buffer = '';
      let currentEvent = '';
      let currentData = '';

      const dispatchEvent = async () => {
        if (currentEvent === 'complete') {
          isCompleted = true;
          await onComplete?.();
          return;
        }
        if (currentData) {
          try {
            const nodeResponse: GraphNodeResponse = JSON.parse(currentData);
            await onMessage(nodeResponse);
          } catch {
            await onError?.(new Error('Failed to parse server response'));
          }
        }
        currentEvent = '';
        currentData = '';
      };

      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          break;
        }
        buffer += decoder.decode(value, { stream: true });
        const parts = buffer.split('\n');
        buffer = parts.pop() || '';
        for (const line of parts) {
          if (line === '') {
            await dispatchEvent();
          } else if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim();
          } else if (line.startsWith('data:')) {
            currentData = line.slice(5).trim();
          }
        }
      }
      if (buffer) {
        if (buffer.startsWith('data:')) {
          currentData = buffer.slice(5).trim();
        } else if (buffer.startsWith('event:')) {
          currentEvent = buffer.slice(6).trim();
        }
        await dispatchEvent();
      }
    } catch (error: any) {
      if (error.name === 'AbortError') return;
      if (isCompleted) return;
      await onError?.(new Error('Stream connection failed'));
    }
  };

  doFetch();

  return () => {
    controller.abort();
  };
}
