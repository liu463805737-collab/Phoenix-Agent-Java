import type {
  ChatMessage,
  ChatSession,
  ChatTransport,
  SendPayload,
} from '@phoenix/chat-shared';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';

import { renderMarkdown, escapeHtml } from '../utils/markdown';
import {
  createSessionApi,
  deleteSessionApi,
  getAgentSessionsApi,
  getSessionMessagesApi,
  renameSessionApi,
  saveMessageApi,
} from './chat';
import { streamFrontChat, streamFrontChatSql } from './stream';
import type { StreamNodeResponse } from './stream';

let msgCounter = 0;

function uid(): string {
  msgCounter++;
  return `m-${Date.now().toString(36)}-${msgCounter}`;
}

const sessionThreadIds = new Map<string, string>();

function toApiMessage(
  sessionId: string,
  role: string,
  content: string,
  extra: Record<string, unknown> = {},
) {
  return {
    sessionId,
    role,
    content,
    messageType: 'text',
    ...extra,
  };
}

function generateResultSetTable(
  resultSetData: { column: string[]; data: Array<Record<string, string>> },
  pageSize: number,
): string {
  const columns = resultSetData.column || [];
  const allData = resultSetData.data || [];
  const total = allData.length;
  const totalPages = Math.ceil(total / pageSize);

  let tableHtml =
    '<div class="sql-result-set"><div class="sql-result-set-header"><span>查询结果 (共 ' +
    total +
    ' 条记录)</span></div>';

  for (let page = 1; page <= totalPages; page++) {
    const startIndex = (page - 1) * pageSize;
    const endIndex = Math.min(startIndex + pageSize, total);
    const currentPageData = allData.slice(startIndex, endIndex);

    tableHtml += `<div class="sql-result-set-page ${page === 1 ? 'sql-result-set-page-active' : ''}" data-page="${page}"><table><thead><tr>`;
    columns.forEach((column) => {
      tableHtml += `<th>${escapeHtml(column)}</th>`;
    });
    tableHtml += `</tr></thead><tbody>`;

    if (currentPageData.length === 0) {
      tableHtml += `<tr><td colspan="${columns.length}">暂无数据</td></tr>`;
    } else {
      currentPageData.forEach((row) => {
        tableHtml += `<tr>`;
        columns.forEach((column) => {
          tableHtml += `<td>${escapeHtml(row[column] || '')}</td>`;
        });
        tableHtml += `</tr>`;
      });
    }
    tableHtml += `</tbody></table></div>`;
  }

  tableHtml += `</div>`;
  return tableHtml;
}

function formatNodeContent(node: StreamNodeResponse[]): string {
  let content = '';
  for (let idx = 0; idx < node.length; idx++) {
    const nd = node[idx];
    if (!nd) continue;

    if (nd.textType === 'HTML') {
      content += nd.text;
    } else if (nd.textType === 'TEXT') {
      content += nd.text.replaceAll('\n', '<br>');
    } else if (
      nd.textType === 'JSON' ||
      nd.textType === 'PYTHON' ||
      nd.textType === 'SQL'
    ) {
      let codeText = '';
      let p = idx;
      for (; p < node.length; p++) {
        if (node[p]?.textType !== nd.textType) break;
        codeText += node[p]?.text || '';
      }
      const lang = nd.textType.toLowerCase();
      content += `<pre><div class="code-block-header"><span>${lang}</span></div><code class="lang-${lang}">${escapeHtml(codeText)}</code></pre>`;
      if (p < node.length) idx = p - 1;
      else break;
    } else if (nd.textType === 'MARK_DOWN') {
      let markdown = '';
      let p = idx;
      for (; p < node.length; p++) {
        if (node[p]?.textType !== 'MARK_DOWN') break;
        markdown += node[p]?.text || '';
      }
      content += `<div class="markdown-report">${renderMarkdown(markdown)}</div>`;
      if (p < node.length) idx = p - 1;
      else break;
    } else if (nd.textType === 'RESULT_SET') {
      try {
        const resultData = JSON.parse(nd.text);
        const resultSetData = resultData.resultSet;
        if (resultSetData.errorMsg) {
          content += `<div class="result-set-error">错误: ${resultSetData.errorMsg}</div>`;
          continue;
        }
        if (!resultSetData.column?.length || !resultSetData.data?.length) {
          content += `<div class="result-set-empty">查询结果为空</div>`;
          continue;
        }
        content += generateResultSetTable(resultSetData, 20);
      } catch {
        content += `<div class="result-set-error">解析结果集数据失败</div>`;
      }
    } else {
      content += nd.text;
    }
  }
  return content;
}

function generateNodeHtml(node: StreamNodeResponse[]): string {
  const content = formatNodeContent(node);
  return `<div class="agent-node"><div class="agent-node__title">${node[0]?.nodeName ?? '空节点'}</div><div class="agent-node__body">${content}</div></div>`;
}

export const realChatTransport: ChatTransport = {
  async listSessions(): Promise<ChatSession[]> {
    const agentId = useAgentStore().activeAgentId;
    if (!agentId) return [];
    return getAgentSessionsApi(agentId);
  },

  async listMessages(sessionId: string): Promise<ChatMessage[]> {
    const messages = await getSessionMessagesApi(sessionId);
    const lastAssistant = [...messages]
      .toReversed()
      .find((m) => m.role === 'assistant' && m.metadata);
    if (lastAssistant?.metadata) {
      try {
        const meta =
          typeof lastAssistant.metadata === 'string'
            ? JSON.parse(lastAssistant.metadata)
            : lastAssistant.metadata;
        if (meta.threadId) {
          sessionThreadIds.set(sessionId, meta.threadId);
        }
      } catch {
        // ignore
      }
    }
    return messages;
  },

  async createSession(agentId: string): Promise<ChatSession> {
    const session = await createSessionApi(agentId);
    if (!session) throw new Error('创建会话失败');
    return session;
  },

  async deleteSession(sessionId: string): Promise<void> {
    await deleteSessionApi(sessionId);
  },

  async renameSession(sessionId: string, title: string): Promise<void> {
    await renameSessionApi(sessionId, title);
  },

  async send(
    payload: SendPayload,
    signal?: AbortSignal,
    onProgress?: (text: string) => void,
    onNodeMessage?: (message: ChatMessage) => void,
  ): Promise<ChatMessage> {
    const { sessionId, content, agentId } = payload;

    if (!agentId) {
      throw new Error('agentId is required for sending messages');
    }

    const chatStore = useChatStore();
    const agentStore = useAgentStore();
    const currentAgent = agentStore.agents.find((a) => a.id === agentId);

    const needsTitle = (() => {
      const session = chatStore.sessions.find((s) => s.id === sessionId);
      return !session?.title || session.title === '新会话';
    })();

    const userMessage = toApiMessage(sessionId, 'user', content, {
      titleNeeded: needsTitle,
    });
    await saveMessageApi(sessionId, userMessage);

    const isSql = currentAgent?.type === 'sql';

    const reply = await new Promise<ChatMessage>((resolve, reject) => {
      if (isSql) {
        let currentNodeName: string | null = null;
        let currentBlockIndex = -1;
        const nodeBlocks: StreamNodeResponse[][] = [];
        let threadId: string | undefined = sessionThreadIds.get(sessionId);
        let abortRequested = false;
        const pendingSavePromises: Promise<void>[] = [];

        const saveNodeMessage = async (
          node: StreamNodeResponse[],
        ): Promise<void> => {
          if (!node || node.length === 0) return;

          const nodeHtml = generateNodeHtml(node);
          const storeMsg: ChatMessage = {
            id: uid(),
            role: 'assistant',
            content: nodeHtml,
            createdAt: Date.now(),
            messageType: 'html',
          };
          onNodeMessage?.(storeMsg);
          const aiMessage: any = {
            sessionId,
            role: 'assistant',
            content: nodeHtml,
            messageType: 'html',
          };
          try {
            await saveMessageApi(sessionId, aiMessage);
          } catch {
            /* ignore */
          }
        };

        const onAbort = () => {
          abortRequested = true;
        };
        signal?.addEventListener('abort', onAbort, { once: true });

        const closeStream = streamFrontChatSql(
          {
            agentId,
            query: content,
            humanFeedback: false,
            nl2sqlOnly: false,
            rejectedPlan: false,
            ...(threadId ? { threadId } : {}),
          },
          (response) => {
            if (abortRequested) return;
            if (response.error) return;
            threadId = response.threadId || threadId;

            const isNewNode =
              currentNodeName === null ||
              response.nodeName !== currentNodeName;

            if (isNewNode) {
              if (currentBlockIndex >= 0 && nodeBlocks[currentBlockIndex]) {
                const p = saveNodeMessage(nodeBlocks[currentBlockIndex]!);
                pendingSavePromises.push(p);
              }
              nodeBlocks.push([{ ...response, text: response.text }]);
              currentBlockIndex = nodeBlocks.length - 1;
              currentNodeName = response.nodeName;
            } else {
              if (currentBlockIndex >= 0 && nodeBlocks[currentBlockIndex]) {
                nodeBlocks[currentBlockIndex]?.push({
                  ...response,
                  text: response.text,
                });
              } else {
                nodeBlocks.push([{ ...response, text: response.text }]);
                currentBlockIndex = nodeBlocks.length - 1;
                currentNodeName = response.nodeName;
              }
            }

            let reportText = '';
            for (const block of nodeBlocks) {
              for (const nd of block) {
                if (nd.textType === 'MARK_DOWN') {
                  reportText += nd.text || '';
                }
              }
            }
            if (reportText) {
              onProgress?.(renderMarkdown(reportText));
            }
          },
          (error) => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;
            reject(error);
          },
          () => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;

            let markdownContent = '';
            for (const block of nodeBlocks) {
              for (const nd of block) {
                if (nd.textType === 'MARK_DOWN') {
                  markdownContent += nd.text || '';
                }
              }
            }

            if (markdownContent) {
              const mdMessage: any = {
                sessionId,
                role: 'assistant',
                content: markdownContent,
                messageType: 'markdown-report',
                metadata: threadId
                  ? JSON.stringify({ threadId })
                  : undefined,
              };
              saveMessageApi(sessionId, mdMessage).catch(() => {});

              if (threadId) {
                sessionThreadIds.set(sessionId, threadId);
              }

              resolve({
                id: uid(),
                role: 'assistant',
                content: markdownContent,
                createdAt: Date.now(),
                messageType: 'markdown-report',
              });
            } else {
              const html = nodeBlocks
                .map((block) => generateNodeHtml(block))
                .join('\n');
              const text = html || '已处理完成';
              const assistantMessage: any = {
                sessionId,
                role: 'assistant',
                content: text,
                messageType: 'html',
                metadata: threadId
                  ? JSON.stringify({ threadId })
                  : undefined,
              };
              saveMessageApi(sessionId, assistantMessage).catch(() => {});

              if (threadId) {
                sessionThreadIds.set(sessionId, threadId);
              }

              resolve({
                id: uid(),
                role: 'assistant',
                content: text,
                createdAt: Date.now(),
                messageType: 'html',
              });
            }
          },
        );

        if (signal?.aborted) {
          abortRequested = true;
          closeStream();
          signal?.removeEventListener('abort', onAbort);
          reject(new DOMException('Aborted', 'AbortError'));
          return;
        }

        const abortHandler = () => {
          abortRequested = true;
          closeStream();
          signal?.removeEventListener('abort', abortHandler);
          reject(new DOMException('Aborted', 'AbortError'));
        };
        signal?.addEventListener('abort', abortHandler, { once: true });
      } else {
        let fullText = '';
        let abortRequested = false;

        const onAbort = () => {
          abortRequested = true;
        };
        signal?.addEventListener('abort', onAbort, { once: true });

        const closeStream = streamFrontChat(
          {
            sessionId,
            content,
            agentSn: currentAgent?.sn || '',
            type: currentAgent?.type || '',
          },
          (response) => {
            if (abortRequested) return;
            if (response.content) {
              fullText += response.content;
              onProgress?.(renderMarkdown(fullText));
            }
          },
          (error) => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;
            reject(error);
          },
          () => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;

            const text = fullText || '已处理完成';
            const html = renderMarkdown(text);
            const assistantMessage = toApiMessage(
              sessionId,
              'assistant',
              text,
            );
            saveMessageApi(sessionId, assistantMessage).catch(() => {});

            resolve({
              id: uid(),
              role: 'assistant',
              content: html,
              createdAt: Date.now(),
              messageType: 'text',
            });
          },
        );

        if (signal?.aborted) {
          abortRequested = true;
          closeStream();
          signal?.removeEventListener('abort', onAbort);
          reject(new DOMException('Aborted', 'AbortError'));
          return;
        }

        const abortHandler = () => {
          abortRequested = true;
          closeStream();
          signal?.removeEventListener('abort', abortHandler);
          reject(new DOMException('Aborted', 'AbortError'));
        };
        signal?.addEventListener('abort', abortHandler, { once: true });
      }
    });

    return reply;
  },
};
