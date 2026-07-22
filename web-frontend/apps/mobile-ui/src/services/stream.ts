const API_BASE_URL = import.meta.env.VITE_GLOB_API_URL || '/api';
const TOKEN_KEY = 'mobile-ui:auth:token';
const USER_KEY = 'mobile-ui:auth:user';

function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || '';
}

function handleUnauthorized(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  import('vant').then(({ showFailToast }) => {
    showFailToast('登录已过期，请重新登录');
  });
  import('../router').then(({ default: router }) => {
    router.push('/login');
  });
}

export interface FrontChatStreamRequest {
  sessionId: string;
  content: string;
  agentSn: string;
  type: string;
}

export interface ConfirmButton {
  text: string;
  action: string;
  type?: string;
}

export interface StreamNodeResponse {
  agentId?: string;
  threadId?: string;
  nodeName: string;
  textType: string;
  text: string;
  error?: boolean;
  complete?: boolean;
  needConfirm?: boolean;
  buttons?: ConfirmButton[];
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
      if (response.status === 401) {
        handleUnauthorized();
        throw new Error('未授权，请先登录');
      }
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

export interface FrontHarnessChatRequest {
  sessionId: string;
  message: string;
  harnessSn: string;
}

export interface FrontHarnessConfirmRequest {
  sessionId: string;
  agentSn: string;
  allowed: boolean;
  suggestedRules?: any[];
}

export function streamFrontHarnessChat(
  request: FrontHarnessChatRequest,
  onMessage: (response: StreamNodeResponse) => void,
  onError: (error: Error) => void,
  onComplete: () => void,
): () => void {
  const url = `${API_BASE_URL}/api/front/harness/chat`;
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
      if (response.status === 401) {
        handleUnauthorized();
        throw new Error('未授权，请先登录');
      }
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
            const nodeResponse: StreamNodeResponse = {
              agentId: request.harnessSn,
              threadId: request.sessionId,
              nodeName: 'Harness',
              textType: 'MARK_DOWN',
              text: parsed.content || '',
              error: false,
              complete: false,
              needConfirm: parsed.needConfirm || false,
              buttons: parsed.buttons || undefined,
            };
            onMessage(nodeResponse);
          } catch {
            // skip
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

export async function confirmFrontHarnessChat(
  request: FrontHarnessConfirmRequest,
  onMessage?: (response: StreamNodeResponse) => void,
  onComplete?: () => void,
): Promise<void> {
  const httpResponse = await fetch(`${API_BASE_URL}/api/front/harness/confirm`, {
    method: 'POST',
    headers: {
      'phoenix-token': getToken(),
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
    body: JSON.stringify(request),
  });
  if (httpResponse.status === 401) {
    handleUnauthorized();
    throw new Error('未授权，请先登录');
  }
  if (!httpResponse.ok) {
    throw new Error(`Confirm request failed! status: ${httpResponse.status}`);
  }
  const reader = httpResponse.body?.getReader();
  const decoder = new TextDecoder();
  if (!reader) return;
  let buffer = '';
  let currentData = '';
  const dispatchEvent = () => {
    if (currentData) {
      try {
        const parsed = JSON.parse(currentData);
        if (parsed.end) {
          onComplete?.();
          return;
        }
        const nodeResponse: StreamNodeResponse = {
          agentId: request.agentSn,
          threadId: request.sessionId,
          nodeName: 'Harness',
          textType: 'MARK_DOWN',
          text: parsed.content || '',
          error: false,
          complete: false,
        };
        onMessage?.(nodeResponse);
      } catch {
        // skip
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
      if (response.status === 401) {
        handleUnauthorized();
        throw new Error('未授权，请先登录');
      }
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
