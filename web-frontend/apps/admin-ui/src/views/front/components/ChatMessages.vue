<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';
import ReportMessage from './report/ReportMessage.vue';
import type { ResultData } from '#/api/core/resultSet';
import ResultSetDisplay from '#/components/run/ResultSetDisplay.vue';

const chat = useChatStore();
const agentStore = useAgentStore();
const { activeMessages, activeSession, activeSessionId, sending } =
  storeToRefs(chat);
const { agents } = storeToRefs(agentStore);

const currentAgent = computed(() => {
  if (!activeSession.value) return null;
  return (
    agents.value.find((a) => a.id === activeSession.value?.agentId) ?? null
  );
});
const botName = computed(() => currentAgent.value?.name ?? 'AI');

function renderMessage(msg: Record<string, any>): string {
  const content = String(msg.content ?? '');
  if (msg.role === 'user') {
    return escapeHtml(content).replaceAll('\n', '<br>');
  }
  return content;
  // return content.replaceAll('\n', '<br>');
}

function escapeHtml(text: string): string {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

const scrollRef = ref<HTMLElement | null>(null);

async function scrollToBottom() {
  await nextTick();
  const el = scrollRef.value;
  if (!el) return;
  el.scrollTop = el.scrollHeight;
}

watch(activeMessages, () => {
  void scrollToBottom();
});

watch(activeSessionId, () => {
  void scrollToBottom();
});
</script>

<template>
  <div ref="scrollRef" class="chat-messages">
    <div class="chat-messages__inner">
      <div
··        v-for="(msg, index) in activeMessages"
        :key="msg.id"
        :class="['chat-message', msg.role]"
      >
        <div
          v-if="msg.role === 'assistant'"
          class="chat-message__avatar"
          :class="{
            'chat-message__avatar--hidden':
              index > 0 && activeMessages[index - 1].role === 'assistant',
          }"
        >
          {{ botName.charAt(0) }}
        </div>

        <div class="chat-message__content">
          <div
            v-if="(msg as any).messageType === 'html'"
            class="chat-message__html"
            v-html="msg.content"
          ></div>
          <div
            v-else-if="(msg as any).messageType === 'markdown-report'"
            class="chat-message__report"
          >
            <ReportMessage :content="msg.content" :sessionId="activeSession?.id" />
          </div>
          <div
            v-else-if="(msg as any).messageType === 'result-set'"
            class="chat-message__result-set"
          >
            <ResultSetDisplay
              v-if="msg.content"
              :resultData="JSON.parse(msg.content) as ResultData"
              :pageSize="20"
            />
          </div>
          <div
            v-else
            class="chat-message__text"
            :class="{
              'chat-message__text--markdown': msg.role === 'assistant',
              'chat-message__text--streaming': msg.streaming,
            }"
            v-html="renderMessage(msg)"
          ></div>
        </div>

        <div
          v-if="msg.role === 'user'"
          class="chat-message__avatar chat-message__avatar--user"
        >
          我
        </div>
      </div>

      <div
        v-if="sending && !activeMessages.some((m) => m.streaming)"
        class="chat-message assistant chat-message--typing"
      >
        <div class="chat-message__avatar">
          {{ botName.charAt(0) }}
        </div>
        <div class="chat-message__content">
          <div class="chat-message__bubble chat-message__bubble--typing">
            <span class="dot" />
            <span class="dot" />
            <span class="dot" />
          </div>
        </div>
      </div>

      <div
        v-if="activeMessages.length === 0 && !sending"
        class="chat-messages__empty"
      >
        当前会话还没有消息，发送一条试试
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chat-messages {
  height: 100%;
  overflow-y: auto;
  background: hsl(var(--background));

  &__inner {
    display: flex;
    flex-direction: column;
    gap: 18px;
    max-width: var(--pc-chat-content-width);
    padding: 24px 0 16px;
    margin: 0 auto;
  }
}

.chat-message {
  display: flex;
  gap: 10px;
  align-items: flex-start;

  &.user {
    justify-content: flex-end;
  }

  &__avatar {
    display: flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    font-size: 13px;
    font-weight: 600;
    color: hsl(var(--primary-foreground));
    background: hsl(var(--primary));
    border-radius: 50%;

    &--user {
      color: hsl(var(--primary-foreground));
      background: hsl(var(--primary));
    }
  }

  &.assistant &__avatar {
    color: hsl(var(--primary));
    background: hsl(var(--primary) / 12%);
  }

  &.assistant &__content {
    width: 91.5%;
  }

  &__avatar--hidden {
    visibility: hidden;
  }

  &__content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
    max-width: 91.5%;
  }

  &__text {
    padding: 10px 14px;
    font-size: 14px;
    line-height: 1.65;
    color: hsl(var(--foreground));
    word-break: break-word;
    background: hsl(var(--card));
    border-radius: 10px;

    :deep(p) {
      margin: 0;
    }
  }

  &.user &__text {
    color: hsl(var(--primary-foreground));
    background: hsl(var(--primary));
    border: none;
  }

  &__text--streaming {
    &::after {
      display: inline-block;
      width: 1.2em;
      font-size: 1.3em;
      text-align: left;
      content: '.';
      animation: pc-dots 1.5s steps(3, end) infinite;
    }
  }

  &__text--markdown {
    padding: 10px 14px;
    line-height: 1.65;
    word-break: break-word;

    :deep(p) {
      margin: 0 0 8px;
    }

    :deep(p:last-child) {
      margin-bottom: 0;
    }

    :deep(pre) {
      padding: 12px;
      margin: 8px 0;
      overflow-x: auto;
      background: #f6f8fa;
      border: 1px solid #e1e4e8;
      border-radius: 6px;
    }

    :deep(code) {
      font-family:
        SFMono-Regular, Consolas, 'Liberation Mono', Menlo, monospace;
      font-size: 13px;
      line-height: 1.45;
    }

    :deep(pre code) {
      padding: 0;
      background: transparent;
      border: none;
    }

    :deep(code:not(pre code)) {
      padding: 2px 6px;
      color: #476582;
      background: #f0f4f8;
      border-radius: 4px;
    }

    :deep(ul),
    :deep(ol) {
      padding-left: 20px;
      margin: 8px 0;
    }

    :deep(li) {
      margin: 4px 0;
    }

    :deep(blockquote) {
      padding: 4px 12px;
      margin: 8px 0;
      color: #606266;
      border-left: 4px solid #409eff;
    }

    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4),
    :deep(h5),
    :deep(h6) {
      margin: 16px 0 8px;
      line-height: 1.3;
    }

    :deep(table) {
      width: 100%;
      margin: 8px 0;
      font-size: 13px;
      border-collapse: collapse;
    }

    :deep(th),
    :deep(td) {
      padding: 6px 10px;
      text-align: left;
      border: 1px solid #e0e0e0;
    }

    :deep(th) {
      font-weight: 600;
      background: #f5f7fa;
    }

    :deep(tr:nth-child(even)) {
      background: #fafafa;
    }

    :deep(img) {
      max-width: 100%;
      border-radius: 6px;
    }

    :deep(a) {
      color: #409eff;
      text-decoration: none;
    }

    :deep(a:hover) {
      text-decoration: underline;
    }

    :deep(hr) {
      margin: 16px 0;
      border: none;
      border-top: 1px solid #e0e0e0;
    }
  }
}

.result-set-table-wrap {
  margin: 8px 0;
  overflow-x: auto;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.result-set-table {
  width: 100%;
  font-size: 13px;
  border-collapse: collapse;

  th {
    padding: 8px 12px;
    font-weight: 600;
    color: #606266;
    text-align: left;
    white-space: nowrap;
    background: #f5f7fa;
    border-bottom: 1px solid #e8e8e8;
  }

  td {
    max-width: 200px;
    padding: 8px 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    word-break: break-word;
    border-bottom: 1px solid #f0f0f0;
  }

  tr:hover {
    background: #f5f7fa;
  }
}

.chat-messages__empty {
  padding: 32px 0;
  font-size: 13px;
  color: hsl(var(--muted-foreground));
  text-align: center;
}

.chat-message__bubble--typing {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  padding: 12px 14px;

  .dot {
    width: 6px;
    height: 6px;
    background: hsl(var(--muted-foreground));
    border-radius: 50%;
    animation: pc-typing 1s infinite ease-in-out;

    &:nth-child(2) {
      animation-delay: 0.15s;
    }

    &:nth-child(3) {
      animation-delay: 0.3s;
    }
  }
}

@keyframes pc-typing {
  0%,
  60%,
  100% {
    opacity: 0.3;
    transform: translateY(0);
  }

  30% {
    opacity: 1;
    transform: translateY(-2px);
  }
}

@keyframes pc-dots {
  0% {
    content: '.';
  }

  33% {
    content: '..';
  }

  66% {
    content: '...';
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
  margin-bottom: 10px;
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

.markdown-report p {
  margin: 0 0 8px;
}

.markdown-report p:last-child {
  margin-bottom: 0;
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
</style>
