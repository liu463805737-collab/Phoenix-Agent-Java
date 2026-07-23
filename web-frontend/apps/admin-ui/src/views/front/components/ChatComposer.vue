<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';
import { ElMessage, ElIcon, ElTooltip } from 'element-plus';
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue';

import PresetQuestions from './PresetQuestions.vue';

const chat = useChatStore();
const agentStore = useAgentStore();
const { isActiveSessionSending, activeSessionId } = storeToRefs(chat);
const { activeAgent } = storeToRefs(agentStore);

const inputValue = ref('');
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const presetCollapsed = ref(true);

const hasPendingConfirm = computed(() => {
  const sessionId = activeSessionId.value;
  if (!sessionId) return false;
  const msgs = chat.messagesByS[sessionId] ?? [];
  return msgs.some((m: any) => m.messageType === 'harness-confirm');
});

const MIN_HEIGHT = 48;
const MAX_HEIGHT = 200;

async function resize() {
  await nextTick();
  const el = textareaRef.value;
  if (!el) return;
  el.style.height = 'auto';
  const next = Math.min(Math.max(el.scrollHeight, MIN_HEIGHT), MAX_HEIGHT);
  el.style.height = `${next}px`;
  el.style.overflowY = el.scrollHeight > MAX_HEIGHT ? 'auto' : 'hidden';
}

watch(inputValue, () => {
  resize();
});

async function handlePresetQuestionClick(question: string) {
  if (isActiveSessionSending.value) {
    ElMessage.warning('智能体正在处理中，请稍后...');
    return;
  }
  if (hasPendingConfirm.value) {
    ElMessage.warning('请先处理当前确认请求');
    return;
  }

  if (!activeSessionId.value) {
    try {
      if (!activeAgent.value?.id) {
        ElMessage.error('当前没有可用的智能体');
        return;
      }
      await chat.createSession(activeAgent.value.id);
    } catch {
      ElMessage.error('创建会话失败');
      return;
    }
  }

  inputValue.value = question;
  await nextTick();
  await handleSubmit();
}

async function handleSubmit() {
  const value = inputValue.value.trim();
  if (!value) return;
  if (isActiveSessionSending.value) return;
  if (hasPendingConfirm.value) return;
  inputValue.value = '';
  resize();
  await chat.send(value);
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter') return;
  if (event.shiftKey || event.isComposing) return;
  if (hasPendingConfirm.value) return;
  event.preventDefault();
  handleSubmit();
}
</script>

<template>
  <div class="composer">

    <div class="composer__preset">
      <el-tooltip v-if="!presetCollapsed" content="隐藏预设问题">
        <div class="composer__preset-close" @click="presetCollapsed = !presetCollapsed">
          <el-icon
            class="composer__preset-toggle"
            :class="{ collapsed: presetCollapsed }"
          >
            <ArrowUp v-if="presetCollapsed" />
            <ArrowDown v-else />
          </el-icon>
        </div>
      </el-tooltip>
      <div v-show="!presetCollapsed" class="composer__preset-body">
        <PresetQuestions @select="handlePresetQuestionClick" />
      </div>

    </div>
    <form class="composer__inner" @submit.prevent="handleSubmit">
      <div
        v-if="presetCollapsed"
        class="composer__preset-header"
        @click="presetCollapsed = !presetCollapsed"
      >
        <span class="composer__preset-title">预设问题</span>
        <el-tooltip content="展开预设问题">
          <el-icon
            class="composer__preset-toggle"
            :class="{ collapsed: presetCollapsed }"
          >
            <ArrowUp v-if="presetCollapsed" />
            <ArrowDown v-else />
          </el-icon>
        </el-tooltip>

      </div>
      <textarea
        ref="textareaRef"
        v-model="inputValue"
        class="composer__input"
        rows="1"
        placeholder="输入您的问题，Enter 发送，Shift + Enter 换行"
        @keydown="handleKeydown"
      />
      <div class="composer__actions">
        <span class="composer__actions-spacer" />
        <el-tooltip
          v-if="!isActiveSessionSending"
          :content="!inputValue.trim() ? '请输入问题' : '发送'"
          placement="top"
          :show-after="300"
        >
          <button
            type="submit"
            class="composer__send"
            :disabled="!inputValue.trim() || hasPendingConfirm"
            aria-label="发送"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
              <path
                d="M2.5 8h9m0 0L7.75 4.25M11.5 8l-3.75 3.75"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>
        </el-tooltip>
        <el-tooltip
          v-else
          content="停止"
          placement="top"
          :show-after="300"
        >
          <button
            type="button"
            class="composer__send composer__send--stop"
            aria-label="停止"
            @click="chat.stopSending(activeSessionId ?? undefined)"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" aria-hidden="true">
              <rect x="3" y="3" width="10" height="10" rx="2" fill="currentColor" />
            </svg>
          </button>
        </el-tooltip>
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
.composer {
  padding: 12px 24px 16px;

  &__inner {
    position: relative;
    display: flex;
    flex-direction: column;
    max-width: calc(var(--pc-chat-content-width) - 68px);
    margin: 0 auto;
    background: hsl(var(--background));
    border: 1px solid hsl(var(--border));
    border-radius: 12px;
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.08);
    transition:
      border-color 0.15s ease,
      box-shadow 0.15s ease;


    &:focus-within {
      border-color: hsl(var(--primary));
      box-shadow: 0 0 0 3px hsl(var(--primary) / 12%);
    }
  }

  &__input {
    width: 100%;
    min-height: 48px;
    max-height: 200px;
    padding: 12px 14px 4px;
    overflow-y: hidden;
    font-family: inherit;
    font-size: 14px;
    line-height: 1.6;
    color: hsl(var(--foreground));
    resize: none;
    outline: none;
    background: transparent;
    border: none;

    &::placeholder {
      color: hsl(var(--muted-foreground));
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 6px 10px 8px 14px;
  }

  &__actions-spacer {
    flex: 1 1 auto;
  }

  &__send {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    color: #fff;
    cursor: pointer;
    background: hsl(var(--primary));
    border: 1px solid hsl(var(--primary));
    border-radius: 50%;
    transition:
      background 0.15s ease,
      color 0.15s ease,
      border-color 0.15s ease,
      opacity 0.15s ease;

    &:hover {
      filter: brightness(0.95);
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }

    &--stop {
      color: #fff;
      cursor: pointer;
      background: #f56c6c;
      border-color: #f56c6c;

      &:hover {
        filter: brightness(0.9);
      }
    }
  }

  &__preset {
    position: relative;
    max-width: calc(var(--pc-chat-content-width) - 68px);
    margin: 0 auto 10px;
  }

  &__preset-header {
    position: absolute;
    top: -24px;
    right: 0;
    display: flex;
    gap: 4px;
    align-items: center;
    justify-content: space-between;
    padding: 3px 8px;
    cursor: pointer;
    border-radius: 8px;
    transition: background 0.15s ease;

    &:hover {
      /* background: hsl(var(--primary) / 8%); */
      *{
        color: hsl(var(--primary) / 80%);
      }
    }
  }

  &__preset-title {
    font-size: 13px;
    font-weight: 500;
    color: hsl(var(--muted-foreground));
  }

  &__preset-close {
    position: absolute;
    top: 10px;
    right: 8px;
    font-size: 16px;
    color: hsl(var(--muted-foreground));
    cursor: pointer;
    transition: transform 0.2s ease;
  }

  &__preset-toggle {
    font-size: 16px;
    color: hsl(var(--muted-foreground));
    transition: transform 0.2s ease;
  }

  &__preset-body {
    margin-top: 8px;
  }
}
</style>
