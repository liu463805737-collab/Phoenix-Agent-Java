<script setup lang="ts">
import type { Agent } from '#/api/core/agent';
import type { ChatMessage, ChatSession } from '#/api/core/chat';
import type {
  ChatApiRequest,
  ConfirmButton,
  GraphNodeResponse,
  GraphRequest,
  HarnessChatRequest,
} from '#/api/core/graph';
import type {
  ResultData,
  ResultSetData,
  ResultSetDisplayConfig,
} from '#/api/core/resultSet';
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  ElAvatar,
  ElButton,
  ElButtonGroup,
  ElEmpty,
  ElIcon,
  ElInput,
  ElMessage,
  ElOption,
  ElRadioButton,
  ElRadioGroup,
  ElSelect,
  ElSwitch,
  ElTooltip,
} from 'element-plus';
import {
  ArrowDown,
  CircleClose,
  Close,
  Document,
  Download,
  FullScreen,
  Loading,
  Promotion,
  WarningFilled,
} from '@element-plus/icons-vue';

import {
  confirmHarnessChat,
  createSessionApi,
  getAgentApi,
  getSessionMessagesApi,
  saveMessageApi,
  streamChat,
  streamHarnessChat,
  streamSearch,
  TextType,
} from '#/api';

import { downloadHtmlReportApi } from '#/api/core/chat';

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

import ChatSessionSidebar from '#/components/run/ChatSessionSidebar.vue';
import HumanFeedback from '#/components/run/HumanFeedback.vue';
import PresetQuestions from '#/components/run/PresetQuestions.vue';
import MarkdownAgentContainer from '#/components/run/markdown';
import ReportHtmlView from '#/components/run/ReportHtmlView.vue';
import ResultSetDisplay from '#/components/run/ResultSetDisplay.vue';

declare global {
  interface Window {
    copyTextToClipboard: (btn: HTMLElement) => void;
    handleResultSetPagination: (
      btn: HTMLElement,
      direction: 'prev' | 'next',
    ) => void;
  }
}

interface SessionRuntimeState {
  isStreaming: boolean;
  nodeBlocks: GraphNodeResponse[][];
  closeStream: (() => void) | null;
  lastRequest: GraphRequest | null;
  htmlReportContent: string;
  htmlReportSize: number;
  markdownReportContent: string;
}

function useSessionStateManager() {
  const sessionStates = reactive<Map<string, SessionRuntimeState>>(new Map());

  const getSessionState = (sessionId: string): SessionRuntimeState => {
    if (!sessionStates.has(sessionId)) {
      sessionStates.set(sessionId, {
        isStreaming: false,
        nodeBlocks: [],
        closeStream: null,
        lastRequest: null,
        htmlReportContent: '',
        htmlReportSize: 0,
        markdownReportContent: '',
      });
    }
    return sessionStates.get(sessionId)!;
  };

  const syncStateToView = (
    sessionId: string,
    viewState: {
      isStreaming: { value: boolean };
      nodeBlocks: { value: GraphNodeResponse[][] };
    },
  ) => {
    const state = getSessionState(sessionId);
    viewState.isStreaming.value = state.isStreaming;
    viewState.nodeBlocks.value = state.nodeBlocks;
  };

  const saveViewToState = (
    sessionId: string,
    viewState: {
      isStreaming: { value: boolean };
      nodeBlocks: { value: GraphNodeResponse[][] };
    },
  ) => {
    const state = getSessionState(sessionId);
    state.isStreaming = viewState.isStreaming.value;
    state.nodeBlocks = viewState.nodeBlocks.value;
  };

  const deleteSessionState = (sessionId: string) => {
    const state = sessionStates.get(sessionId);
    if (state?.closeStream) {
      state.closeStream();
    }
    sessionStates.delete(sessionId);
  };

  return {
    sessionStates,
    getSessionState,
    syncStateToView,
    saveViewToState,
    deleteSessionState,
  };
}

const route = useRoute();
const router = useRouter();

const agent = ref<Agent>({});
const currentSession = ref<ChatSession | null>(null);
const currentMessages = ref<ChatMessage[]>([]);
const userInput = ref('');
const {
  getSessionState,
  syncStateToView,
  saveViewToState,
  deleteSessionState,
} = useSessionStateManager();
const isStreaming = ref(false);
const nodeBlocks = ref<GraphNodeResponse[][]>([]);
const options = ref({
  markdownIt: { linkify: true },
  linkAttributes: { attrs: { target: '_blank', rel: 'noopener' } },
});
const requestOptions = reactive({
  humanFeedback: false,
  nl2sqlOnly: false,
  reportFormat: 'markdown' as 'markdown' | 'html',
});
const showReportFullscreen = ref(false);
const fullscreenReportContent = ref('');
const fullscreenReportFormat = ref<'markdown' | 'html'>('markdown');
const messageReportFormat = reactive<Record<string, 'markdown' | 'html'>>({});
function getMessageFormat(messageId: number | undefined): 'markdown' | 'html' {
  return (messageId != null && messageReportFormat[messageId]) || 'markdown';
}
const inputControlsCollapsed = ref(false);
const autoScroll = ref(true);
const chatContainer = ref<HTMLElement | null>(null);
const showHumanFeedback = ref(false);
const showHarnessConfirm = ref(false);
const pendingConfirmButtons = ref<ConfirmButton[]>([]);
const pendingConfirmSessionId = ref('');
const pendingConfirmAgentSn = ref('');
const lastRequest = ref<GraphRequest | null>(null);
const resultSetDisplayConfig = reactive<ResultSetDisplayConfig>({
  showSqlResults: false,
  pageSize: 20,
});

const agentId = computed(() => Number(route.params.id));

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

window.handleResultSetPagination = (
  btn: HTMLElement,
  direction: 'prev' | 'next',
) => {
  const container = btn.closest('.result-set-container');
  if (!container) return;
  const currentPageElement = container.querySelector(
    '.result-set-current-page',
  );
  const prevBtn = container.querySelector(
    '.result-set-pagination-prev',
  ) as HTMLButtonElement;
  const nextBtn = container.querySelector(
    '.result-set-pagination-next',
  ) as HTMLButtonElement;
  const pages = container.querySelectorAll('.result-set-page');
  if (!currentPageElement || !prevBtn || !nextBtn || pages.length === 0) return;
  let currentPage = Number.parseInt(currentPageElement.textContent || '1');
  const totalPages = pages.length;
  if (direction === 'prev' && currentPage > 1) currentPage--;
  else if (direction === 'next' && currentPage < totalPages) currentPage++;
  pages.forEach((page: Element) =>
    page.classList.remove('result-set-page-active'),
  );
  const targetPage = container.querySelector(
    `.result-set-page[data-page="${currentPage}"]`,
  );
  if (targetPage) targetPage.classList.add('result-set-page-active');
  currentPageElement.textContent = currentPage.toString();
  prevBtn.disabled = currentPage === 1;
  nextBtn.disabled = currentPage === totalPages;
};

async function loadAgent() {
  try {
    const result = await getAgentApi(agentId.value);
    if (!result) {
      ElMessage.error('智能体不存在');
      router.push('/agent/list');
      return;
    }
    agent.value = result;
  } catch {
    ElMessage.error('加载智能体失败');
    router.push('/agent/list');
  }
}

async function selectSession(session: ChatSession | null) {
  if (currentSession.value) {
    saveViewToState(currentSession.value.id, { isStreaming, nodeBlocks });
  }
  currentSession.value = session;
  try {
    if (session === null) {
      currentMessages.value = [];
      nodeBlocks.value = [];
      isStreaming.value = false;
      return;
    }
    syncStateToView(session.id, { isStreaming, nodeBlocks });
    currentMessages.value = await getSessionMessagesApi(session.id);
    await nextTick();
    scrollToBottom();
  } catch {
    ElMessage.error('加载消息失败');
  }
}

async function sendMessage() {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入请求消息！');
    return;
  }
  if (!currentSession.value || isStreaming.value) {
    ElMessage.warning('智能体正在处理中，请稍后...');
    return;
  }

  const needsTitle =
    !currentSession.value.title || currentSession.value.title === '新会话';
  const sessionId = currentSession.value.id;

  const userMessage: ChatMessage = {
    sessionId,
    role: 'user',
    content: userInput.value,
    messageType: 'text',
    titleNeeded: needsTitle,
  };

  try {
    await saveMessageApi(sessionId, userMessage);
    currentMessages.value.push(userMessage);

    const sessionState = getSessionState(sessionId);
    const request: GraphRequest = {
      agentId: String(agentId.value),
      query: userInput.value,
      humanFeedback: requestOptions.humanFeedback,
      nl2sqlOnly: requestOptions.nl2sqlOnly,
      rejectedPlan: false,
      humanFeedbackContent: undefined,
      threadId: sessionState.lastRequest?.threadId || undefined,
    };

    userInput.value = '';
    await sendGraphRequest(request, true);
  } catch {
    ElMessage.error('发送消息失败');
  }
}

async function sendGraphRequest(request: GraphRequest, rejectedPlan: boolean) {
  if (!currentSession.value) return;
  const sessionId = currentSession.value.id;
  const sessionTitle = currentSession.value.title;
  const sessionState = getSessionState(sessionId);

  try {
    lastRequest.value = request;
    isStreaming.value = true;
    nodeBlocks.value = [];
    showHarnessConfirm.value = false;

    let currentNodeName: string | null = null;
    let currentBlockIndex = -1;
    const pendingSavePromises: Promise<void>[] = [];

    resetReportState(sessionState, request);

    const saveNodeMessage = async (
      node: GraphNodeResponse[],
    ): Promise<void> => {
      if (!node || node.length === 0) return;

      const first = node[0]!;
      if (first.textType === TextType.RESULT_SET) {
        try {
          const resultData: ResultData = JSON.parse(first.text);
          if (
            resultData.displayStyle?.type &&
            resultData.displayStyle.type !== 'table'
          ) {
            const aiMessage: ChatMessage = {
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
      const aiMessage: ChatMessage = {
        sessionId,
        role: 'assistant',
        content: nodeHtml,
        messageType: 'html',
      };
      await saveMessageApi(sessionId, aiMessage);
    };

    let closeStreamFn: (() => void) | null = null;

    switch (agent.value.type) {
      case 'agent':
        closeStreamFn = startAgentStream();
        break;
      case 'harness':
        closeStreamFn = startHarnessStream();
        break;
      default:
        closeStreamFn = startSearchStream();
        break;
    }

    function startAgentStream() {
      const chatRequest: ChatApiRequest = {
        sessionId,
        content: request.query,
        agentSn: String(agent.value.sn || agent.value.id),
        type: 'agent',
      };

      return streamChat(
        chatRequest,
        async (response: GraphNodeResponse) => {
          if (response.error) return;

          handleNodeResponse(response);
        },
        async (error: Error) => {
          ElMessage.error(`请求失败: ${error.message}`);
          handleStreamError(error);
        },
        async () => {
          if (pendingSavePromises.length > 0) {
            await Promise.all(pendingSavePromises);
          }

          if (sessionState.htmlReportContent) {
            const htmlReportMessage: ChatMessage = {
              sessionId,
              role: 'assistant',
              content: sessionState.htmlReportContent,
              messageType: 'html-report',
            };
            try {
              await saveMessageApi(sessionId, htmlReportMessage);
              if (currentSession.value?.id === sessionId) {
                currentMessages.value.push(htmlReportMessage);
              }
            } catch {
              ElMessage.error('保存HTML报告失败！');
            }
            sessionState.isStreaming = false;
            if (currentSession.value?.id === sessionId) {
              isStreaming.value = false;
              nodeBlocks.value = [];
            }
          } else if (sessionState.markdownReportContent) {
            const markdownMessage: ChatMessage = {
              sessionId,
              role: 'assistant',
              content: sessionState.markdownReportContent,
              messageType: 'markdown-report',
            };
            try {
              await saveMessageApi(sessionId, markdownMessage);
              if (currentSession.value?.id === sessionId) {
                currentMessages.value.push(markdownMessage);
              }
            } catch {
              console.error('保存Markdown报告失败');
            }
            sessionState.isStreaming = false;
            if (currentSession.value?.id === sessionId) {
              isStreaming.value = false;
              nodeBlocks.value = [];
            }
          } else {
            // streamChat 路径：合并所有节点文本作为报告保存
            const allText = sessionState.nodeBlocks
              .flat()
              .map((n) => n.text)
              .filter(Boolean)
              .join('');
            if (allText) {
              const reportMessage: ChatMessage = {
                sessionId,
                role: 'assistant',
                content: allText,
                messageType: 'markdown-report',
              };
              try {
                await saveMessageApi(sessionId, reportMessage);
                if (currentSession.value?.id === sessionId) {
                  currentMessages.value.push(reportMessage);
                }
              } catch {
                console.error('保存报告失败');
              }
              sessionState.isStreaming = false;
              if (currentSession.value?.id === sessionId) {
                isStreaming.value = false;
                nodeBlocks.value = [];
              }
            } else {
              if (
                currentBlockIndex >= 0 &&
                sessionState.nodeBlocks[currentBlockIndex]
              ) {
                await saveNodeMessage(
                  sessionState.nodeBlocks[currentBlockIndex]!,
                );
              }

              if (requestOptions.humanFeedback && rejectedPlan) {
                showHumanFeedback.value = true;
              } else {
                sessionState.isStreaming = false;
                if (currentSession.value?.id === sessionId) {
                  isStreaming.value = false;
                }
              }
            }
          }

          handleStreamComplete();
        },
      );
    }

    function startHarnessStream() {
      const harnessRequest: HarnessChatRequest = {
        sessionId,
        message: request.query,
        harnessSn: String(agent.value.sn || agent.value.id),
      };

      return streamHarnessChat(
        harnessRequest,
        async (response: GraphNodeResponse) => {
          if (response.error) {
            ElMessage.error(`处理错误: ${response.text}`);
            return;
          }

          handleNodeResponse(response);
        },
        async (error: Error) => {
          ElMessage.error(`流式请求失败: ${error.message}`);
          handleStreamError(error);
        },
        async () => {
          await handleStreamEnd();
        },
      );
    }

    function startSearchStream() {
      return streamSearch(
        request,
        async (response: GraphNodeResponse) => {
          if (response.error) {
            ElMessage.error(`处理错误: ${response.text}`);
            return;
          }

          if (sessionState.lastRequest) {
            sessionState.lastRequest.threadId = response.threadId;
          }

          handleNodeResponse(response);
        },
        async (error: Error) => {
          ElMessage.error(`流式请求失败: ${error.message}`);
          handleStreamError(error);
        },
        async () => {
          await handleStreamEnd();
        },
      );
    }

    function handleNodeResponse(response: GraphNodeResponse) {
      if (isReportGeneratorNode(response)) {
        const isNewNode =
          currentNodeName === null ||
          response.nodeName !== currentNodeName;

        if (isNewNode) {
          if (
            currentBlockIndex >= 0 &&
            sessionState.nodeBlocks[currentBlockIndex]
          ) {
            const p = saveNodeMessage(
              sessionState.nodeBlocks[currentBlockIndex]!,
            );
            pendingSavePromises.push(p);
          }
          sessionState.nodeBlocks.push([
            { ...response, text: response.text },
          ]);
          currentBlockIndex = sessionState.nodeBlocks.length - 1;
          currentNodeName = response.nodeName;
        }

        if (response.textType === 'HTML') {
          sessionState.htmlReportContent += response.text;
          sessionState.htmlReportSize =
            sessionState.htmlReportContent.length;
          const reportNode = sessionState.nodeBlocks.find(
            (block) =>
              block.length > 0 &&
              isReportGeneratorNode(block[0]!) &&
              block[0]!.textType === 'HTML',
          );
          if (reportNode) {
            reportNode[0]!.text = `正在收集HTML报告... 已收集 ${sessionState.htmlReportSize} 字节`;
          } else {
            sessionState.nodeBlocks.push([
              {
                ...response,
                text: `正在收集HTML报告... 已收集 ${sessionState.htmlReportSize} 字节`,
              },
            ]);
          }
        } else if (response.textType === 'MARK_DOWN') {
          sessionState.markdownReportContent += response.text;
          const reportNode = sessionState.nodeBlocks.find(
            (block) =>
              block.length > 0 &&
              isReportGeneratorNode(block[0]!) &&
              block[0]!.textType === 'MARK_DOWN',
          );
          if (reportNode) {
            reportNode[0]!.text = `正在收集Markdown报告... 已收集 ${sessionState.markdownReportContent.length} 字节`;
          } else {
            sessionState.nodeBlocks.push([
              {
                ...response,
                text: `正在收集Markdown报告... 已收集 ${sessionState.markdownReportContent.length} 字节`,
              },
            ]);
          }
        }
      } else if (response.textType === TextType.RESULT_SET) {
        currentNodeName = 'result_set';
        if (
          currentBlockIndex >= 0 &&
          sessionState.nodeBlocks[currentBlockIndex]
        ) {
          const p = saveNodeMessage(
            sessionState.nodeBlocks[currentBlockIndex]!,
          );
          pendingSavePromises.push(p);
        }
        sessionState.nodeBlocks.push([
          { ...response, text: response.text },
        ]);
        currentBlockIndex = sessionState.nodeBlocks.length - 1;
      } else {
        const isNewNode =
          currentNodeName === null ||
          response.nodeName !== currentNodeName;

        if (isNewNode) {
          if (
            currentBlockIndex >= 0 &&
            sessionState.nodeBlocks[currentBlockIndex]
          ) {
            const p = saveNodeMessage(
              sessionState.nodeBlocks[currentBlockIndex]!,
            );
            pendingSavePromises.push(p);
          }
          sessionState.nodeBlocks.push([
            { ...response, text: response.text },
          ]);
          currentBlockIndex = sessionState.nodeBlocks.length - 1;
          currentNodeName = response.nodeName;
        } else {
          if (
            currentBlockIndex >= 0 &&
            sessionState.nodeBlocks[currentBlockIndex]
          ) {
            sessionState.nodeBlocks[currentBlockIndex]!.push({
              ...response,
              text: response.text,
            });
          } else {
            sessionState.nodeBlocks.push([
              { ...response, text: response.text },
            ]);
            currentBlockIndex = sessionState.nodeBlocks.length - 1;
            currentNodeName = response.nodeName;
          }
        }
      }

      if (response.needConfirm && response.buttons && response.buttons.length > 0) {
        showHarnessConfirm.value = true;
        pendingConfirmButtons.value = response.buttons;
        pendingConfirmSessionId.value = response.threadId;
        pendingConfirmAgentSn.value = response.agentId;
      }

      if (currentSession.value?.id === sessionId) {
        nodeBlocks.value = sessionState.nodeBlocks;
        if (autoScroll.value) scrollToBottom();
      }
    }

    function handleStreamError(error: Error) {
      if (pendingSavePromises.length > 0) {
        Promise.all(pendingSavePromises);
      }
      sessionState.isStreaming = false;
      sessionState.closeStream = null;
      currentNodeName = null;
      if (currentSession.value?.id === sessionId) {
        isStreaming.value = false;
        selectSession(currentSession.value);
      }
    }

    async function handleStreamEnd() {
      if (pendingSavePromises.length > 0) {
        await Promise.all(pendingSavePromises);
      }

      if (sessionState.htmlReportContent) {
        const htmlReportMessage: ChatMessage = {
          sessionId,
          role: 'assistant',
          content: sessionState.htmlReportContent,
          messageType: 'html-report',
        };
        try {
          await saveMessageApi(sessionId, htmlReportMessage);
          if (currentSession.value?.id === sessionId) {
            currentMessages.value.push(htmlReportMessage);
          }
        } catch {
          ElMessage.error('保存HTML报告失败！');
        }
      } else if (sessionState.markdownReportContent) {
        const markdownMessage: ChatMessage = {
          sessionId,
          role: 'assistant',
          content: sessionState.markdownReportContent,
          messageType: 'markdown-report',
        };
        try {
          await saveMessageApi(sessionId, markdownMessage);
          if (currentSession.value?.id === sessionId) {
            currentMessages.value.push(markdownMessage);
          }
        } catch {
          console.error('保存Markdown报告失败');
        }
      } else {
        if (
          currentBlockIndex >= 0 &&
          sessionState.nodeBlocks[currentBlockIndex]
        ) {
          await saveNodeMessage(
            sessionState.nodeBlocks[currentBlockIndex]!,
          );
        }

        if (requestOptions.humanFeedback && rejectedPlan) {
          showHumanFeedback.value = true;
        }
      }

      handleStreamComplete();
    }

    function handleStreamComplete() {
      sessionState.isStreaming = false;
      if (currentSession.value?.id === sessionId) {
        isStreaming.value = false;
        nodeBlocks.value = [];
      }

      ElMessage.success(`会话[${sessionTitle}]处理完成`);
      currentNodeName = null;
      sessionState.closeStream = null;
      if (currentSession.value?.id === sessionId) {
        selectSession(currentSession.value);
      }
    }

    sessionState.closeStream = closeStreamFn;
  } catch {
    ElMessage.error('发送消息失败');
    sessionState.isStreaming = false;
    sessionState.closeStream = null;
    if (currentSession.value?.id === sessionId) {
      isStreaming.value = false;
    }
  }
}

function formatMessageContent(message: ChatMessage): string {
  if (message.messageType === 'text') {
    return message.content?.replaceAll(/\n/g, '<br>') || '';
  }
  return message.content || '';
}

async function downloadHtmlReportFromMessageByServer(content: string) {
  if (!content) {
    ElMessage.warning('没有可下载的HTML报告');
    return;
  }
  if (!currentSession.value) {
    ElMessage.warning('当前没有会话信息');
    return;
  }
  try {
    await downloadHtmlReportApi(currentSession.value.id, content);
    ElMessage.success('HTML报告下载成功');
  } catch {
    ElMessage.error('下载HTML报告失败');
  }
}

function openReportFullscreen(content: string, msgId?: number) {
  fullscreenReportContent.value = content;
  fullscreenReportFormat.value = getMessageFormat(msgId);
  showReportFullscreen.value = true;
}

function closeReportFullscreen() {
  showReportFullscreen.value = false;
  fullscreenReportContent.value = '';
}

function downloadMarkdownReportFromMessage(content: string) {
  if (!content) {
    ElMessage.warning('没有可下载的Markdown报告');
    return;
  }
  const blob = new Blob([content], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `report_${Date.now()}.md`;
  document.body.append(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  ElMessage.success('Markdown报告下载成功');
}

function generateNodeHtml(node: GraphNodeResponse[]): string {
  const content = formatNodeContent(node);
  return `
    <div class="agent-response-block" style="display: block !important; width: 100% !important;">
      <div class="agent-response-title">${node.length > 0 ? node[0]!.nodeName : '空节点'}</div>
      <div class="agent-response-content">${content}</div>
    </div>
  `;
}

function formatNodeContent(node: GraphNodeResponse[]): string {
  let content = '';
  for (let idx = 0; idx < node.length; idx++) {
    const nd = node[idx];
    if (!nd) continue;

    if (nd.textType === TextType.HTML) {
      content += nd.text;
    } else if (nd.textType === TextType.TEXT) {
      content += nd.text.replaceAll(/\n/g, '<br>');
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
      if (!resultSetDisplayConfig.showSqlResults) continue;
      try {
        const resultData: ResultData = JSON.parse(nd.text);
        const resultSetData = resultData.resultSet;
        if (resultSetData.errorMsg) {
          content += `<div class="result-set-error">错误: ${resultSetData.errorMsg}</div>`;
          continue;
        }
        if (!resultSetData.column?.length || !resultSetData.data?.length) {
          content += `<div class="result-set-empty">查询结果为空</div>`;
          continue;
        }
        if (
          !resultData.displayStyle?.type ||
          resultData.displayStyle.type === 'table'
        ) {
          content += generateResultSetTable(
            resultSetData,
            resultSetDisplayConfig.pageSize,
          );
        }
      } catch {
        content += `<div class="result-set-error">解析结果集数据失败</div>`;
      }
    } else {
      content += nd.text;
    }
  }
  return content;
}

function markdownToHtml(markdown: string): string {
  if (!markdown) return '';
  marked.setOptions({ gfm: true, breaks: true });
  const rawHtml = marked.parse(markdown) as string;
  return DOMPurify.sanitize(rawHtml);
}

function resetReportState(
  sessionState: SessionRuntimeState,
  request: GraphRequest,
) {
  sessionState.isStreaming = true;
  sessionState.nodeBlocks = [];
  sessionState.lastRequest = request;
  sessionState.htmlReportContent = '';
  sessionState.htmlReportSize = 0;
  sessionState.markdownReportContent = '';
}

function scrollToBottom() {
  nextTick(() => {
    requestAnimationFrame(() => {
      if (chatContainer.value) {
        chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
      }
    });
  });
}

let harnessConfirmChunkIndex = -1;

async function handleHarnessButtonClick(btn: ConfirmButton) {
  const allowed = btn.action === 'confirm';
  showHarnessConfirm.value = false;
  harnessConfirmChunkIndex = -1;
  isStreaming.value = true;
  try {
    await confirmHarnessChat(
      {
        sessionId: pendingConfirmSessionId.value,
        agentSn: pendingConfirmAgentSn.value,
        allowed,
      },
      async (response) => {
        console.log('[handleHarnessButtonClick] response text:', response.text);
        if (harnessConfirmChunkIndex < 0) {
          nodeBlocks.value = [...nodeBlocks.value, [response]];
          harnessConfirmChunkIndex = nodeBlocks.value.length - 1;
        } else {
          const block = [...nodeBlocks.value[harnessConfirmChunkIndex]!, response];
          const blocks = [...nodeBlocks.value];
          blocks[harnessConfirmChunkIndex] = block;
          nodeBlocks.value = blocks;
        }
        if (autoScroll.value) scrollToBottom();
      },
      async () => {
        if (harnessConfirmChunkIndex >= 0) {
          const confirmBlock = nodeBlocks.value[harnessConfirmChunkIndex];
          if (confirmBlock) {
            const nodeHtml = generateNodeHtml(confirmBlock);
            const aiMessage: ChatMessage = {
              sessionId: pendingConfirmSessionId.value,
              role: 'assistant',
              content: nodeHtml,
              messageType: 'html',
            };
            await saveMessageApi(pendingConfirmSessionId.value, aiMessage);
          }
        }
        harnessConfirmChunkIndex = -1;
        isStreaming.value = false;
      },
    );
  } catch (error: any) {
    ElMessage.error(`操作失败: ${error.message}`);
  }
}

async function handleHumanFeedback(
  request: GraphRequest,
  rejectedPlan: boolean,
  content: string,
) {
  content = content.trim() || 'Accept';
  showHumanFeedback.value = false;
  const newRequest: GraphRequest = { ...request };
  newRequest.rejectedPlan = rejectedPlan;
  newRequest.humanFeedbackContent = content;
  await sendGraphRequest(newRequest, rejectedPlan);
}

async function handlePresetQuestionClick(question: string) {
  if (isStreaming.value) {
    ElMessage.warning('智能体正在处理中，请稍后...');
    return;
  }

  if (!currentSession.value) {
    try {
      const newSession = await createSessionApi(agentId.value, '新会话');
      if (!newSession) {
        ElMessage.error('创建会话失败');
        return;
      }
      currentSession.value = newSession;
      ElMessage.success('新会话创建成功');
    } catch {
      ElMessage.error('创建会话失败');
      return;
    }
  }

  userInput.value = question;
  nextTick(() => sendMessage());
}

function onPresetQuestionsLoaded(payload: { hasQuestions: boolean }) {
  if (!payload.hasQuestions) {
    inputControlsCollapsed.value = true;
  }
}

async function stopStreaming() {
  if (!currentSession.value) {
    ElMessage.warning('当前没有活动的会话');
    return;
  }

  const sessionId = currentSession.value.id;
  const sessionState = getSessionState(sessionId);

  try {
    if (!sessionState.closeStream) {
      ElMessage.warning('没有正在进行的对话');
      return;
    }

    sessionState.closeStream();
    sessionState.closeStream = null;

    if (sessionState.nodeBlocks?.length > 0) {
      const saveOneNode = async (node: GraphNodeResponse[]): Promise<void> => {
        if (!node?.length) return;
        const nodeHtml = generateNodeHtml(node);
        const aiMessage: ChatMessage = {
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

      const promises = sessionState.nodeBlocks.map((block) =>
        saveOneNode(block),
      );
      await Promise.all(promises);
    }

    sessionState.isStreaming = false;
    sessionState.nodeBlocks = [];
    sessionState.htmlReportContent = '';
    sessionState.htmlReportSize = 0;
    sessionState.markdownReportContent = '';

    if (currentSession.value?.id === sessionId) {
      isStreaming.value = false;
      nodeBlocks.value = [];
    }

    await selectSession(currentSession.value);
    ElMessage.success('已停止对话');
  } catch {
    sessionState.isStreaming = false;
    sessionState.closeStream = null;
    if (currentSession.value?.id === sessionId) {
      isStreaming.value = false;
      nodeBlocks.value = [];
    }
    ElMessage.error('停止对话失败');
  }
}

function generateResultSetTable(
  resultSetData: ResultSetData,
  pageSize: number,
): string {
  const columns = resultSetData.column || [];
  const allData = resultSetData.data || [];
  const total = allData.length;
  const totalPages = Math.ceil(total / pageSize);

  let tableHtml = `<div class="result-set-container"><div class="result-set-header"><div class="result-set-info"><span>查询结果 (共 ${total} 条记录)</span><div class="result-set-pagination-controls"><span class="result-set-pagination-info">第 <span class="result-set-current-page">1</span> 页，共 ${totalPages} 页</span><div class="result-set-pagination-buttons"><button class="result-set-pagination-btn result-set-pagination-prev" onclick="handleResultSetPagination(this, 'prev')" disabled>上一页</button><button class="result-set-pagination-btn result-set-pagination-next" onclick="handleResultSetPagination(this, 'next')" ${totalPages > 1 ? '' : 'disabled'}>下一页</button></div></div></div></div><div class="result-set-table-container">`;

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

function getMarkdownContentFromNode(node: GraphNodeResponse[]): string {
  if (!node?.length) return '';
  const firstNode = node[0]!;
  if (
    isReportGeneratorNode(firstNode) &&
    firstNode.textType === 'MARK_DOWN'
  ) {
    const sessionId = currentSession.value?.id;
    if (sessionId) {
      const state = getSessionState(sessionId);
      return state.markdownReportContent || '';
    }
  }
  let markdown = '';
  for (let idx = 0; idx < node.length; idx++) {
    if (node[idx]?.textType === 'MARK_DOWN') {
      let p = idx;
      for (; p < node.length; p++) {
        if (node[p]?.textType !== 'MARK_DOWN') break;
        markdown += node[p]?.text || '';
      }
      if (p < node.length) idx = p - 1;
      else break;
    }
  }
  return markdown;
}

function escapeHtml(text: string): string {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function handleNl2sqlOnlyChange(value: string | number | boolean) {
  if (value) {
    requestOptions.humanFeedback = false;
  }
}

function isReportGeneratorNode(
  node: GraphNodeResponse | undefined | null,
): boolean {
  if (!node) return false;
  return (
    node.nodeName === 'ReportGeneratorNode' || node.nodeName === '生成报表'
  );
}

function firstNode(block: GraphNodeResponse[]): GraphNodeResponse | undefined {
  return block[0];
}

onMounted(async () => {
  await loadAgent();
});
</script>

<template>
  <Page>
    <el-container
      style="
        display: flex;
        flex-direction: row;
        gap: 0;
        height: calc(100vh - 120px);
      "
    >
      <ChatSessionSidebar
        :agent="agent"
        :handleSetCurrentSession="
          async (session: ChatSession | null) => {
            currentSession = session;
            await selectSession(session);
          }
        "
        :handleGetCurrentSession="() => currentSession"
        :handleSelectSession="selectSession"
        :handleDeleteSessionState="deleteSessionState"
      />

      <el-main
        style="
          display: flex;
          flex: 1;
          flex-direction: column;
          overflow: hidden;
        "
      >
        <div class="chat-container" ref="chatContainer">
          <div v-if="!currentSession" class="empty-state">
            <el-empty description="请选择一个会话或创建新会话开始对话" />
            <PresetQuestions
              v-if="agent.id"
              :agentId="agent.id"
              :onQuestionClick="handlePresetQuestionClick"
              class="empty-state-preset"
            />
          </div>
          <div v-else class="messages-area">
            <div
              v-for="message in currentMessages"
              :key="message.id"
              :class="
                message.messageType === 'text'
                  ? ['message-container', message.role]
                  : ''
              "
            >
              <div
                v-if="message.messageType === 'html'"
                v-html="message.content"
              ></div>
              <div
                v-else-if="message.messageType === 'result-set'"
                class="result-set-message"
              >
                <ResultSetDisplay
                  v-if="message.content"
                  :resultData="JSON.parse(message.content)"
                  :pageSize="resultSetDisplayConfig.pageSize"
                />
              </div>
              <div
                v-else-if="message.messageType === 'markdown-report'"
                class="markdown-report-message"
              >
                <div
                  class="markdown-report-header"
                  style="
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                  "
                >
                  <div class="report-info">
                    <el-icon><Document /></el-icon>
                    <span>报告已生成</span>
                    <el-radio-group
                      :model-value="getMessageFormat(message.id)"
                      @change="(val: any) => { if (message.id != null) messageReportFormat[message.id] = val }"
                      size="small"
                      class="report-format-inline"
                    >
                      <el-radio-button value="markdown"
                        >Markdown</el-radio-button
                      >
                      <el-radio-button value="html">HTML</el-radio-button>
                    </el-radio-group>
                  </div>
                  <el-button-group size="large">
                    <el-button
                      type="primary"
                      @click="
                        downloadMarkdownReportFromMessage(message.content)
                      "
                    >
                      <el-icon><Download /></el-icon>
                      下载Markdown报告
                    </el-button>
                    <el-button
                      type="success"
                      @click="
                        downloadHtmlReportFromMessageByServer(message.content)
                      "
                    >
                      <el-icon><Download /></el-icon>
                      下载HTML报告
                    </el-button>
                    <el-tooltip content="全屏查看报告" placement="top">
                      <el-button
                        type="info"
                        @click="openReportFullscreen(message.content, message.id)"
                      >
                        <el-icon><FullScreen /></el-icon>
                        全屏
                      </el-button>
                    </el-tooltip>
                  </el-button-group>
                </div>
                <div class="markdown-report-content">
                  <markdown-agent-container
                    v-if="getMessageFormat(message.id) === 'markdown'"
                    class="md-body"
                    :content="message.content"
                    :options="options"
                  />
                  <ReportHtmlView v-else :content="message.content" />
                </div>
              </div>
              <div v-else :class="['message', message.role]">
                <div class="message-avatar">
                  <el-avatar :size="32" style="font-size:16px;font-weight:600;color:#fff;background:#2f6bff">
                    {{ message.role === 'user' ? '我' : (agent.name?.charAt(0) || 'AI') }}
                  </el-avatar>
                </div>
                <div class="message-content">
                  <div
                    class="message-text"
                    v-html="formatMessageContent(message)"
                  ></div>
                </div>
              </div>
            </div>

            <div v-if="isStreaming || nodeBlocks.length > 0" class="streaming-response">
              <div class="agent-response-container">
                <template v-for="(nodeBlock, index) in nodeBlocks" :key="index">
                  <div
                    v-if="
                      isReportGeneratorNode(firstNode(nodeBlock)) &&
                      firstNode(nodeBlock)?.textType === 'MARK_DOWN'
                    "
                    class="agent-response-block"
                  >
                    <div class="agent-response-title">
                      {{ firstNode(nodeBlock)?.nodeName }}
                    </div>
                    <div class="agent-response-content">
                      <markdown-agent-container
                        v-if="requestOptions.reportFormat === 'markdown'"
                        class="md-body"
                        :content="getMarkdownContentFromNode(nodeBlock)"
                        :options="options"
                      />
                      <ReportHtmlView
                        v-else
                        :content="getMarkdownContentFromNode(nodeBlock)"
                      />
                    </div>
                  </div>
                  <div
                    v-else-if="firstNode(nodeBlock)?.textType === 'RESULT_SET'"
                    class="agent-response-block"
                  >
                    <div class="agent-response-title">
                      {{ firstNode(nodeBlock)?.nodeName }}
                    </div>
                    <div class="agent-response-content">
                      <ResultSetDisplay
                        v-if="firstNode(nodeBlock)?.text"
                        :resultData="JSON.parse(firstNode(nodeBlock)!.text)"
                        :pageSize="resultSetDisplayConfig.pageSize"
                      />
                    </div>
                  </div>
                  <div v-else v-html="generateNodeHtml(nodeBlock)"></div>
                </template>
              </div>
              <div v-if="isStreaming" class="streaming-footer">
                <div class="streaming-indicator">
                  <span class="streaming-dot"></span>
                  <span class="streaming-dot"></span>
                  <span class="streaming-dot"></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <HumanFeedback
          v-if="showHumanFeedback"
          :request="lastRequest!"
          :handleFeedback="handleHumanFeedback"
        />

        <div v-if="showHarnessConfirm && pendingConfirmButtons.length > 0" class="harness-confirm-area">
          <div class="harness-confirm-header">
            <el-icon><WarningFilled /></el-icon>
            <span>请确认操作</span>
          </div>
          <div class="harness-confirm-actions">
            <el-button
              v-for="(btn, idx) in pendingConfirmButtons"
              :key="idx"
              :type="(btn.type as any) || 'primary'"
              @click="handleHarnessButtonClick(btn)"
            >
              {{ btn.text }}
            </el-button>
          </div>
        </div>

        <div class="input-area" v-if="currentSession">
          <div class="input-controls">
            <div
              class="input-controls-header"
              @click="inputControlsCollapsed = !inputControlsCollapsed"
            >
              <span class="input-controls-title">更多选项</span>
              <el-button
                type="primary"
                size="small"
                class="input-controls-toggle-btn"
                :class="{ collapsed: inputControlsCollapsed }"
              >
                <el-icon class="input-controls-toggle-icon"
                  ><ArrowDown
                /></el-icon>
                {{ inputControlsCollapsed ? '展开' : '收起' }}
              </el-button>
            </div>
            <div v-show="!inputControlsCollapsed" class="input-controls-body">
              <PresetQuestions
                v-if="currentSession && agent.id"
                :agentId="agent.id"
                :onQuestionClick="handlePresetQuestionClick"
                @loaded="onPresetQuestionsLoaded"
              />
              <div v-if="agent.type == 'sql'" class="switch-group">
                <div class="switch-item">
                  <span class="switch-label">人工反馈</span>
                  <el-tooltip
                    :disabled="!requestOptions.nl2sqlOnly"
                    content="该功能在NL2SQL模式下不能使用"
                    placement="top"
                  >
                    <el-switch
                      v-model="requestOptions.humanFeedback"
                      :disabled="
                        requestOptions.nl2sqlOnly ||
                        isStreaming ||
                        showHumanFeedback
                      "
                    />
                  </el-tooltip>
                </div>
                <div class="switch-item">
                  <span class="switch-label">仅NL2SQL</span>
                  <el-switch
                    v-model="requestOptions.nl2sqlOnly"
                    :disabled="isStreaming || showHumanFeedback"
                    @change="handleNl2sqlOnlyChange"
                  />
                </div>
                <div class="switch-item">
                  <span class="switch-label">自动Scroll</span>
                  <el-switch v-model="autoScroll" />
                </div>
                <div class="switch-item">
                  <span class="switch-label">显示SQL结果</span>
                  <el-tooltip
                    content="启用本功能会将SQL查询结果存储到DataAgent项目的数据库中，如果数据量较大不建议开启本功能"
                    placement="top"
                  >
                    <el-switch
                      v-model="resultSetDisplayConfig.showSqlResults"
                      :disabled="isStreaming || showHumanFeedback"
                    />
                  </el-tooltip>
                </div>
                <div class="switch-item">
                  <span class="switch-label">每页数量</span>
                  <el-select
                    v-model="resultSetDisplayConfig.pageSize"
                    :disabled="isStreaming || showHumanFeedback"
                    style="width: 80px"
                  >
                    <el-option label="5" :value="5" />
                    <el-option label="10" :value="10" />
                    <el-option label="20" :value="20" />
                    <el-option label="50" :value="50" />
                    <el-option label="100" :value="100" />
                  </el-select>
                </div>
              </div>
            </div>
          </div>
          <div class="input-container">
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="3"
              placeholder="请输入您的问题..."
              :disabled="isStreaming || showHarnessConfirm"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <el-button
              v-if="!isStreaming && !showHarnessConfirm"
              type="primary"
              @click="sendMessage"
              circle
              class="send-button"
            >
              <el-icon><Promotion /></el-icon>
            </el-button>
            <el-button
              v-if="isStreaming"
              type="danger"
              @click="stopStreaming"
              circle
              class="send-button stop-button-inline"
            >
              <el-icon><CircleClose /></el-icon>
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>

    <Teleport to="body">
      <div
        v-if="showReportFullscreen"
        class="report-fullscreen-overlay"
        @click.self="closeReportFullscreen"
      >
        <div class="report-fullscreen-container">
          <div class="report-fullscreen-header">
            <span class="report-fullscreen-title">
              {{
                fullscreenReportFormat === 'markdown'
                  ? 'Markdown 报告'
                  : 'HTML 报告'
              }}
            </span>
            <el-button
              type="danger"
              circle
              class="report-fullscreen-close"
              @click="closeReportFullscreen"
            >
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="report-fullscreen-content">
            <markdown-agent-container
              v-if="fullscreenReportFormat === 'markdown'"
              class="md-body report-fullscreen-body"
              :content="fullscreenReportContent"
              :options="options"
            />
            <ReportHtmlView
              v-else
              :content="fullscreenReportContent"
              class="report-fullscreen-body"
            />
          </div>
        </div>
      </div>
    </Teleport>
  </Page>
</template>

<style scoped>
.chat-container {
  flex: 1;
  padding: 20px;
  margin-bottom: 0;
  overflow-y: auto;
  background: #f8f9fa;
  margin-left: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.empty-state-preset {
  width: 100%;
  max-width: 800px;
}

.messages-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-container {
  display: flex;
  max-width: 100%;
}

.message-container.user {
  justify-content: flex-end;
}

.message-container.assistant {
  justify-content: flex-start;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.message.assistant {
  align-self: flex-start;
}

.markdown-report {
  line-height: 1.6;
  color: #1f2933;
}

.markdown-report pre {
  padding: 10px 12px;
  overflow: auto;
  background: #f6f8fa;
  border-radius: 6px;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-text {
  padding: 12px 16px;
  line-height: 1.5;
  overflow-wrap: break-word;
  border-radius: 12px;
}

.message.user .message-text {
  color: white;
  background: #409eff;
}

.message.assistant .message-text {
  color: #303133;
  background: white;
  border: 1px solid #e8e8e8;
}

.streaming-response {
  padding: 16px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.streaming-header {
  display: flex;
  gap: 8px;
  align-items: center;
  padding-bottom: 8px;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.streaming-header span {
  font-weight: 500;
  color: #409eff;
}

.stop-button-inline {
  width: 48px;
  height: 48px;
}

.streaming-indicator {
  display: flex;
  gap: 6px;
  align-items: center;
}

.streaming-footer {
  padding-top: 12px;
}

.streaming-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: bounce 1.4s ease-in-out infinite both;
}

.streaming-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.streaming-dot:nth-child(2) {
  animation-delay: -0.16s;
}

.streaming-dot:nth-child(3) {
  animation-delay: 0s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.html-report-message {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: #f8fbff;
  border: 1px solid #e1f0ff;
  border-radius: 12px;
}

.markdown-report-message {
  padding: 16px;
  margin-bottom: 16px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
}

.markdown-report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.markdown-report-content {
  margin-top: 16px;
}

.report-info {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #409eff;
}

.report-format-inline {
  margin-left: 8px;
}

.report-fullscreen-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgb(0 0 0 / 70%);
}

.report-fullscreen-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 1200px;
  height: 90vh;
  overflow: hidden;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgb(0 0 0 / 30%);
}

.report-fullscreen-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.report-fullscreen-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.report-fullscreen-close {
  flex-shrink: 0;
}

.report-fullscreen-content {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

.report-fullscreen-body {
  min-height: 100%;
}

.input-area {
  flex-shrink: 0;
  padding: 2px 16px 16px 16px;
  background: white;
  border: 1px solid #e8e8e8;
  margin-left: 16px;
}

.input-controls {
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.input-controls-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  user-select: none;
}

.input-controls-header:hover {
  color: #409eff;
}

.input-controls-title {
  font-weight: 500;
}

.input-controls-toggle-btn {
  flex-shrink: 0;
}

.input-controls-toggle-btn .input-controls-toggle-icon {
  margin-right: 4px;
  transition: transform 0.2s ease;
}

.input-controls-toggle-btn.collapsed .input-controls-toggle-icon {
  transform: rotate(-90deg);
}

.input-controls-body {
  padding-bottom: 12px;
}

.switch-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.switch-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.switch-label {
  font-size: 14px;
  color: #606266;
}

.send-button {
  width: 48px;
  height: 48px;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

@media (max-width: 768px) {
  .el-aside {
    width: 250px !important;
  }

  .message {
    max-width: 90%;
  }

  .input-container {
    flex-direction: column;
  }
}
</style>

<style>
.agent-response-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.agent-response-block {
  overflow: hidden;
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.agent-response-block:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgb(64 158 255 / 10%);
}

.agent-response-title {
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  background: #ecf5ff;
  border-bottom: 1px solid #e8e8e8;
}

.agent-response-content {
  min-height: 40px;
  padding: 16px;
  font-family: Monaco, Menlo, 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

.agent-response-content .markdown-container {
  font-family: inherit;
  line-height: 1.4;
  white-space: normal;
}

.agent-response-content pre {
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
}

.agent-response-content code {
  padding: 0;
  font-family: Monaco, Menlo, 'Ubuntu Mono', monospace;
  background: transparent;
}

.node-content pre {
  margin: 0;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

.agent-response-content pre.hljs {
  padding: 16px;
  margin: 8px 0;
  overflow-x: auto;
  background: #f6f8fa !important;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
}

.agent-response-content code.hljs {
  padding: 0;
  font-size: 13px;
  line-height: 1.45;
  background: transparent !important;
}

.agent-response-content .hljs {
  display: block;
  padding: 16px;
  overflow-x: auto;
  color: #24292e;
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
}

.markdown-report {
  line-height: 1.6;
  color: #1f2933;
}

.markdown-report pre {
  padding: 10px 12px;
  overflow: auto;
  background: #f6f8fa;
  border-radius: 6px;
}

.result-set-container {
  margin: 8px 0;
  overflow: hidden;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.result-set-header {
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.result-set-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.result-set-pagination-controls {
  display: flex;
  gap: 16px;
  align-items: center;
}

.result-set-pagination-info {
  font-size: 14px;
  color: #606266;
}

.result-set-pagination-buttons {
  display: flex;
  gap: 8px;
}

.result-set-pagination-btn {
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

.result-set-pagination-btn:hover:not(:disabled) {
  background: #f5f7fa;
  border-color: #c6e2ff;
}

.result-set-pagination-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
  background: #f5f7fa;
}

.result-set-table-container {
  position: relative;
  overflow-x: auto;
}

.result-set-page {
  display: none;
}

.result-set-page-active {
  display: block;
}

.result-set-table {
  width: 100%;
  font-size: 13px;
  border-collapse: collapse;
}

.result-set-table th {
  padding: 8px 12px;
  font-weight: 600;
  color: #606266;
  text-align: left;
  white-space: nowrap;
  background: #f5f7fa;
  border-bottom: 1px solid #e8e8e8;
}

.result-set-table td {
  max-width: 200px;
  padding: 8px 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  border-bottom: 1px solid #f0f0f0;
}

.result-set-table tr:hover {
  background: #f5f7fa;
}

.result-set-empty-cell {
  padding: 20px;
  color: #909399;
  text-align: center;
}

.result-set-error {
  padding: 8px 12px;
  margin: 8px 0;
  color: #f56c6c;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 4px;
}

.result-set-empty {
  padding: 8px 12px;
  margin: 8px 0;
  color: #909399;
  text-align: center;
  background: #f4f4f5;
  border-radius: 4px;
}

.harness-confirm-area {
  padding: 20px;
  margin: 16px 0;
  background: #fff7e6;
  border: 1px solid #ffe7ba;
  border-radius: 12px;
}

.harness-confirm-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #d48806;
}

.harness-confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.result-set-message {
  width: 100%;
}

@media (max-width: 768px) {
  .result-set-table-container {
    font-size: 12px;
  }

  .result-set-table th,
  .result-set-table td {
    padding: 6px 8px;
  }
}
</style>
