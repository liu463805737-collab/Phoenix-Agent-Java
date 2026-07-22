import type {
  ChatMessage,
  ChatSession,
  ChatTransport,
  SendPayload,
} from '@phoenix/chat-shared';
import { useChatStore, useAgentStore } from '@phoenix/chat-shared';

import {
  createSessionApi,
  deleteSessionApi,
  renameSessionApi,
  saveMessageApi,
  TextType,
} from '#/api';
import type { GraphNodeResponse, ResultSetData } from '#/api';
import {
  getSessionMessagesApi,
  streamFrontChat,
  streamFrontChatSql,
  streamFrontHarnessChat,
  confirmFrontHarnessChat,
} from '#/api/front/chat';

import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';
import sql from 'highlight.js/lib/languages/sql';
import python from 'highlight.js/lib/languages/python';
import json from 'highlight.js/lib/languages/json';
import { marked } from 'marked';
import DOMPurify from 'dompurify';

hljs.registerLanguage('sql', sql);
hljs.registerLanguage('python', python);
hljs.registerLanguage('json', json);

let msgCounter = 0;
function uid(): string {
  msgCounter++;
  return `m-${Date.now().toString(36)}-${msgCounter}`;
}

function toStoreMessage(api: any): ChatMessage {
  return {
    id: String(api.id ?? `${Date.now()}-${Math.random()}`),
    role: (api.role === 'user' ? 'user' : 'assistant') as 'assistant' | 'user',
    content: api.content ?? '',
    createdAt: api.createTime ? new Date(api.createTime).getTime() : Date.now(),
    messageType: api.messageType ?? 'text',
    metadata: api.metadata,
  };
}

if (!window.copyTextToClipboard) {
  window.copyTextToClipboard = (btn: HTMLElement) => {
    const text = btn.previousElementSibling?.textContent || '';
    const originalText = btn.textContent || '';
    navigator.clipboard
      .writeText(text)
      .then(() => {
        btn.textContent = '已复制!';
        setTimeout(() => {
          btn.textContent = originalText;
        }, 3000);
      })
      .catch(() => {
        btn.textContent = '复制失败';
        setTimeout(() => {
          btn.textContent = originalText;
        }, 3000);
      });
  };
}

function markdownToHtml(markdown: string): string {
  if (!markdown) return '';
  marked.setOptions({ gfm: true, breaks: true });
  const rawHtml = marked.parse(markdown) as string;
  return DOMPurify.sanitize(rawHtml);
}

function escapeHtml(text: string): string {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function generateResultSetTable(
  resultSetData: ResultSetData,
  pageSize: number,
): string {
  const columns = resultSetData.column || [];
  const allData = resultSetData.data || [];
  const total = allData.length;
  const totalPages = Math.ceil(total / pageSize);

  let tableHtml = `<div class="result-set-container"><div class="result-set-header"><div class="result-set-info"><span>查询结果 (共 ${total} 条记录)</span></div></div><div class="result-set-table-container">`;

  for (let page = 1; page <= totalPages; page++) {
    const startIndex = (page - 1) * pageSize;
    const endIndex = Math.min(startIndex + pageSize, total);
    const currentPageData = allData.slice(startIndex, endIndex);

    tableHtml += `<div class="result-set-page ${page === 1 ? 'result-set-page-active' : ''}" data-page="${page}"><table class="result-set-table"><thead><tr>`;
    columns.forEach((column) => {
      tableHtml += `<th>${escapeHtml(column)}</th>`;
    });
    tableHtml += `</tr></thead><tbody>`;

    if (currentPageData.length === 0) {
      tableHtml += `<tr><td colspan="${columns.length}" class="result-set-empty-cell">暂无数据</td></tr>`;
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

  tableHtml += `</div></div>`;
  return tableHtml;
}

function formatNodeContent(node: GraphNodeResponse[]): string {
  let content = '';
  for (let idx = 0; idx < node.length; idx++) {
    const nd = node[idx];
    if (!nd) continue;

    if (nd.textType === TextType.HTML) {
      content += nd.text;
    } else if (nd.textType === TextType.TEXT) {
      content += nd.text.replaceAll('\n', '<br>');
    } else if (
      nd.textType === TextType.JSON ||
      nd.textType === TextType.PYTHON ||
      nd.textType === TextType.SQL
    ) {
      let pre = '';
      let p = idx;
      for (; p < node.length; p++) {
        if (node[p]?.textType !== nd.textType) break;
        pre += node[p]?.text || '';
      }
      try {
        const language = nd.textType.toLowerCase();
        const highlighted = hljs.highlight(pre, { language });
        content += `<pre><div style="display: flex; justify-content: space-between; align-items: center; background: #f8f9fa; padding: 8px 12px; border-bottom: none; font-family: system-ui, sans-serif; font-size: 14px;"><span style="color: #666;">${language}</span><span hidden>${pre}</span><button onclick='copyTextToClipboard(this)' style="background: #f8f9fa; border: none; padding: 4px 12px; border-radius: 12px; font-size: 13px; cursor: pointer; transition: background 0.2s;">复制</button></div><code class="hljs ${language}">${highlighted.value}</code></pre>`;
      } catch {
        content += `<pre><code>${pre}</code></pre>`;
      }
      if (p < node.length) idx = p - 1;
      else break;
    } else if (nd.textType === TextType.MARK_DOWN) {
      let markdown = '';
      let p = idx;
      for (; p < node.length; p++) {
        if (node[p]?.textType !== TextType.MARK_DOWN) break;
        markdown += node[p]?.text || '';
      }
      const safeHtml = markdownToHtml(markdown);
      content += `<div class="markdown-report">${safeHtml}</div>`;
      if (p < node.length) idx = p - 1;
      else break;
    } else if (nd.textType === TextType.RESULT_SET) {
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

function generateNodeHtml(node: GraphNodeResponse[]): string {
  const content = formatNodeContent(node);
  return `
    <div class="agent-response-block" style="display: block !important; width: 100% !important;">
      <div class="agent-response-title">${node[0]?.nodeName ?? '空节点'}</div>
      <div class="agent-response-content">${content}</div>
    </div>
  `;
}

function generateBlocksHtml(blocks: GraphNodeResponse[][]): string {
  return blocks.map((block) => generateNodeHtml(block)).join('\n');
}

// Per-session threadId for conversation continuation
const sessionThreadIds = new Map<string, string>();

export const apiChatTransport: ChatTransport = {
  async listSessions(): Promise<ChatSession[]> {
    throw new Error('listSessions is not supported via API transport');
  },

  async listMessages(sessionId: string): Promise<ChatMessage[]> {
    const list = await getSessionMessagesApi(sessionId);
    const messages = list.map((m) => toStoreMessage(m));
    // Convert markdown content for assistant text messages
    for (const msg of messages) {
      if (msg.role === 'assistant' && !msg.messageType) {
        msg.messageType = 'text';
      }
      if (
        msg.role === 'assistant' &&
        msg.messageType === 'text' &&
        !/<[a-z][\s\S]*>/i.test(msg.content)
      ) {
        msg.content = markdownToHtml(msg.content);
        msg.messageType = 'html';
      }
    }
    // Restore threadId from last assistant message's metadata
    const lastAssistant = [...messages]
      .reverse()
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
        // ignore parse errors
      }
    }
    return messages;
  },

  async createSession(agentId: string): Promise<ChatSession> {
    const session = await createSessionApi(Number(agentId), '新会话');
    if (!session) throw new Error('创建会话失败');
    return {
      id: String(session.id),
      title: session.title || '新会话',
      preview: '',
      agentId: String(session.agentId ?? agentId),
      updatedAt: session.createTime
        ? new Date(session.createTime).getTime()
        : Date.now(),
    };
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

    const needsTitle = (() => {
      try {
        const store = useChatStore();
        const session = store.sessions.find((s) => s.id === sessionId);
        return !session?.title || session.title === '新会话';
      } catch {
        return false;
      }
    })();

    const userMessage: any = {
      sessionId,
      role: 'user',
      content,
      messageType: 'text',
      titleNeeded: needsTitle,
    };
    await saveMessageApi(sessionId, userMessage);

    const agentStore = useAgentStore();
    const currentAgent = agentStore.agents.find((a) => a.id === agentId);
    const isSql = currentAgent?.type === 'sql';
    const isHarness = currentAgent?.type === 'harness';

    const reply = await new Promise<ChatMessage>((resolve, reject) => {
      if (isHarness) {
        let fullText = '';
        let abortRequested = false;

        const onAbort = () => {
          abortRequested = true;
        };
        signal?.addEventListener('abort', onAbort, { once: true });

        const closeStream = streamFrontHarnessChat(
          {
            sessionId,
            message: content,
            harnessSn: currentAgent?.sn || '',
          },
          async (response) => {
            if (abortRequested) return;
            if (response.error) return;
            if (response.text) {
              fullText += response.text;
              onProgress?.(markdownToHtml(fullText));
            }
          },
          async (error) => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;
            reject(error);
          },
          async () => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;

            const text = fullText || '已处理完成';
            const html = markdownToHtml(text);
            const assistantMessage: any = {
              sessionId,
              role: 'assistant',
              content: html,
              messageType: 'text',
            };
            try {
              await saveMessageApi(sessionId, assistantMessage);
            } catch {
              /* ignore */
            }

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
      } else if (isSql) {
        // --- SQL intelligent agent: use streamFrontChatSql with full node processing ---
        let currentNodeName: string | null = null;
        let currentBlockIndex = -1;
        const nodeBlocks: GraphNodeResponse[][] = [];
        let threadId: string | undefined = sessionThreadIds.get(sessionId);
        let abortRequested = false;
        const pendingSavePromises: Promise<void>[] = [];

        const saveNodeMessage = async (
          node: GraphNodeResponse[],
        ): Promise<void> => {
          if (!node || node.length === 0) return;

          const first = node[0]!;
          if (first.textType === TextType.RESULT_SET) {
            try {
              const resultData = JSON.parse(first.text);
              if (
                resultData.displayStyle?.type &&
                resultData.displayStyle.type !== 'table'
              ) {
                const storeMsg: ChatMessage = {
                  id: uid(),
                  role: 'assistant',
                  content: first.text,
                  createdAt: Date.now(),
                  messageType: 'result-set',
                };
                onNodeMessage?.(storeMsg);
                const aiMessage: any = {
                  sessionId,
                  role: 'assistant',
                  content: first.text,
                  messageType: 'result-set',
                };
                await saveMessageApi(sessionId, aiMessage);
                return;
              }
            } catch {
              /* ignore */
            }
          }

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
          async (response) => {
            if (abortRequested) return;
            if (response.error) return;
            threadId = response.threadId || threadId;

            if (response.textType === TextType.RESULT_SET) {
              currentNodeName = 'result_set';
              if (currentBlockIndex >= 0 && nodeBlocks[currentBlockIndex]) {
                const p = saveNodeMessage(nodeBlocks[currentBlockIndex]!);
                pendingSavePromises.push(p);
              }
              nodeBlocks.push([{ ...response, text: response.text }]);
              currentBlockIndex = nodeBlocks.length - 1;
            } else {
              const isNewNode =
                currentNodeName === null || response.nodeName !== currentNodeName;

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
            }

            // 只发送 MARK_DOWN 报告文本（节点消息通过 onNodeMessage 推送）
            let reportText = '';
            for (const block of nodeBlocks) {
              for (const nd of block) {
                if (nd.textType === TextType.MARK_DOWN) {
                  reportText += nd.text || '';
                }
              }
            }
            if (reportText) {
              onProgress?.(markdownToHtml(reportText));
            }
          },
          async (error) => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;
            try {
              if (pendingSavePromises.length > 0) {
                await Promise.all(pendingSavePromises);
              }
            } catch {
              // ignore
            }
            reject(error);
          },
          async () => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;

            try {
              if (pendingSavePromises.length > 0) {
                await Promise.all(pendingSavePromises);
              }
            } catch {
              // ignore
            }

            // Detect if any blocks contain MARK_DOWN type (report content)
            let markdownContent = '';
            for (const block of nodeBlocks) {
              for (const nd of block) {
                if (nd.textType === TextType.MARK_DOWN) {
                  markdownContent += nd.text || '';
                }
              }
            }

            if (markdownContent) {
              // Save markdown-report message (raw markdown for client-side rendering)
              const mdMessage: any = {
                sessionId,
                role: 'assistant',
                content: markdownContent,
                messageType: 'markdown-report',
                metadata: threadId ? JSON.stringify({ threadId }) : undefined,
              };
              try {
                await saveMessageApi(sessionId, mdMessage);
              } catch {
                // ignore
              }

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
              // Fallback: generate combined HTML message (original behavior)
              const html = generateBlocksHtml(nodeBlocks);
              const text = html || '已处理完成';
              const assistantMessage: any = {
                sessionId,
                role: 'assistant',
                content: text,
                messageType: 'html',
                metadata: threadId ? JSON.stringify({ threadId }) : undefined,
              };
              try {
                await saveMessageApi(sessionId, assistantMessage);
              } catch {
                // ignore
              }

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
        // --- Non-SQL intelligent agent: use streamFrontChat with simple text accumulation ---
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
          async (response) => {
            if (abortRequested) return;
            if (response.text) {
              fullText += response.text;
              onProgress?.(markdownToHtml(fullText));
            }
          },
          async (error) => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;
            reject(error);
          },
          async () => {
            signal?.removeEventListener('abort', onAbort);
            if (abortRequested) return;

            const text = fullText || '已处理完成';
            const html = markdownToHtml(text);
            const assistantMessage: any = {
              sessionId,
              role: 'assistant',
              content: html,
              messageType: 'text',
            };
            try {
              await saveMessageApi(sessionId, assistantMessage);
            } catch {
              /* ignore */
            }

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

export async function handleHarnessConfirm(
  sessionId: string,
  agentSn: string,
  allowed: boolean,
  onNodeMessage?: (message: ChatMessage) => void,
  onProgress?: (text: string) => void,
): Promise<string> {
  let currentNodeName: string | null = null;
  let currentBlockIndex = -1;
  const nodeBlocks: GraphNodeResponse[][] = [];
  let markdownContent = '';

  await confirmFrontHarnessChat(
    { sessionId, agentSn, allowed },
    async (response) => {
      if (response.error) return;

      const isNewNode =
        currentNodeName === null || response.nodeName !== currentNodeName;

      if (isNewNode) {
        if (currentBlockIndex >= 0 && nodeBlocks[currentBlockIndex]) {
          const nodeHtml = generateNodeHtml(nodeBlocks[currentBlockIndex]!);
          const storeMsg: ChatMessage = {
            id: uid(),
            role: 'assistant',
            content: nodeHtml,
            createdAt: Date.now(),
            messageType: 'html',
          };
          onNodeMessage?.(storeMsg);
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

      markdownContent = '';
      for (const block of nodeBlocks) {
        for (const nd of block) {
          if (nd.textType === TextType.MARK_DOWN) {
            markdownContent += nd.text || '';
          }
        }
      }
      if (markdownContent) {
        onProgress?.(markdownToHtml(markdownContent));
      }
    },
    async () => {
      if (currentBlockIndex >= 0 && nodeBlocks[currentBlockIndex]) {
        const nodeHtml = generateNodeHtml(nodeBlocks[currentBlockIndex]!);
        const storeMsg: ChatMessage = {
          id: uid(),
          role: 'assistant',
          content: nodeHtml,
          createdAt: Date.now(),
          messageType: 'html',
        };
        onNodeMessage?.(storeMsg);
      }
    },
  );

  return markdownContent || generateBlocksHtml(nodeBlocks) || '已处理完成';
}
