<script setup lang="ts">
import { computed, ref } from 'vue';

import { escapeHtml } from '../../utils/markdown';

interface Props {
  role: 'assistant' | 'user';
  content?: string;
  messageType?: string;
  botAvatar?: string;
  botName?: string;
  typing?: boolean;
  showAvatar?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  messageType: 'text',
  botAvatar: '智',
  botName: '',
  typing: false,
  showAvatar: true,
});

const emit = defineEmits<{
  (e: 'longpress'): void;
  (e: 'copy'): void;
  (e: 'regenerate'): void;
}>();

const imgError = ref(false);

const botAvatar = computed(() => {
  const a = props.botAvatar;
  return a && !imgError.value && (a.startsWith('/') || a.startsWith('http'));
});

const isHtml = computed(
  () =>
    props.role === 'assistant' &&
    (props.messageType === 'html' || props.messageType === 'text'),
);
const userContent = computed(() => {
  if (props.role !== 'user') return '';
  return escapeHtml(props.content).replaceAll('\n', '<br>');
});

const LONG_PRESS_MS = 450;
let timer: number | null = null;
const pressed = ref(false);

function startPress() {
  pressed.value = true;
  clearTimer();
  timer = window.setTimeout(() => emit('longpress'), LONG_PRESS_MS);
}
function clearTimer() {
  if (timer != null) {
    clearTimeout(timer);
    timer = null;
  }
  pressed.value = false;
}
</script>

<template>
  <div class="bubble" :class="`bubble--${props.role}`">
    <template v-if="props.role === 'assistant'">
      <div v-if="props.showAvatar" class="bubble__avatar">
        <img
          v-if="botAvatar"
          :src="props.botAvatar"
          :alt="props.botName || '助手'"
          class="bubble__avatar-img"
          @error="imgError = true"
        />
        <span v-else>{{ props.botName ? [...props.botName][0] : '智' }}</span>
      </div>
      <div class="bubble__main" :class="{ 'bubble__main--no-avatar': !props.showAvatar }">
        <div
          class="bubble__content"
          :class="{
            'bubble__content--typing': props.typing,
            'bubble__content--pressed': pressed,
          }"
          @touchstart.passive="startPress"
          @touchend="clearTimer"
          @touchcancel="clearTimer"
          @touchmove="clearTimer"
        >
          <template v-if="props.typing">
            <span class="dot" />
            <span class="dot" />
            <span class="dot" />
          </template>
          <template v-else-if="isHtml">
            <div class="bubble__html" v-html="props.content" />
          </template>
          <template v-else>{{ props.content }}</template>
        </div>
        <div v-if="!props.typing && props.content" class="bubble__actions">
          <button
            type="button"
            class="bubble__action"
            aria-label="复制"
            @click="emit('copy')"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <rect
                x="8"
                y="8"
                width="11"
                height="12"
                rx="2"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
              />
              <path
                d="M5 16V6a2 2 0 0 1 2-2h9"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>
          <button
            type="button"
            class="bubble__action"
            aria-label="重新生成"
            @click="emit('regenerate')"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path
                d="M4 12a8 8 0 0 1 13.66-5.66M20 12a8 8 0 0 1-13.66 5.66"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M17 4v4h-4M7 20v-4h4"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <div
        class="bubble__content bubble__content--user"
        :class="{ 'bubble__content--pressed': pressed }"
        @touchstart.passive="startPress"
        @touchend="clearTimer"
        @touchcancel="clearTimer"
        @touchmove="clearTimer"
      >
        <span v-html="userContent" />
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.bubble {
  display: flex;
  gap: 10px;
  align-items: flex-start;

  &--user {
    justify-content: flex-end;
  }

  &__avatar {
    position: relative;
    display: flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    margin-top: 2px;
    font-size: 12px;
    font-weight: 600;
    color: var(--m-brand-primary);
    background: var(--m-brand-primary-soft);
    border-radius: 50%;
    overflow: hidden;
  }

  &__avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  &__main {
    display: flex;
    flex: 1 1 auto;
    flex-direction: column;
    gap: 4px;
    min-width: 0;

    &--no-avatar {
      margin-left: 38px;
    }
  }

  &__content {
    font-size: 14px;
    line-height: 1.5;
    color: var(--m-text-primary);
    word-break: break-word;

    &--user {
      max-width: 78%;
      padding: 10px 14px;
      font-size: 14px;
      line-height: 1.5;
      color: var(--m-bubble-user-fg);
      background: var(--m-bubble-user-bg);
      border-radius: var(--m-radius-bubble);
      white-space: pre-wrap;
    }

    &--pressed {
      opacity: 0.72;
    }

    &--typing {
      display: inline-flex;
      gap: 5px;
      align-items: center;
      padding: 12px 0;
    }
  }

  &__html {
    white-space: normal;
    overflow-x: auto;

    :deep(pre) {
      overflow-x: auto;
      padding: 12px;
      margin: 8px 0;
      font-size: 13px;
      line-height: 1.45;
      background: var(--m-fill-tertiary);
      border-radius: 8px;
    }

    :deep(code) {
      font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
      font-size: 0.9em;
      padding: 1px 4px;
      background: var(--m-fill-tertiary);
      border-radius: 3px;
    }

    :deep(pre code) {
      padding: 0;
      background: transparent;
    }

    :deep(p) {
      margin: 0 0 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    :deep(ul),
    :deep(ol) {
      padding-left: 20px;
      margin: 4px 0;
    }

    :deep(li) {
      margin: 2px 0;
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
      border: 1px solid var(--m-border);
    }

    :deep(th) {
      font-weight: 600;
      background: var(--m-fill-tertiary);
    }

    :deep(blockquote) {
      margin: 8px 0;
      padding: 4px 12px;
      color: var(--m-text-soft);
      border-left: 3px solid var(--m-border);
    }

    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4) {
      margin: 12px 0 6px;
      line-height: 1.35;
    }

    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: 6px;
    }

    :deep(a) {
      color: var(--m-brand-primary);
      text-decoration: underline;
    }

    :deep(.agent-node) {
      margin-bottom: 12px;
      border: 1px solid var(--m-border);
      border-radius: 10px;
      overflow: hidden;
    }

    :deep(.agent-node__title) {
      padding: 8px 12px;
      font-size: 13px;
      font-weight: 600;
      color: var(--m-text-regular);
      background: var(--m-fill-tertiary);
      border-bottom: 1px solid var(--m-border);
    }

    :deep(.agent-node__body) {
      padding: 10px 12px;
    }

    :deep(.agent-node__body > :last-child) {
      margin-bottom: 0;
    }

    :deep(.code-block-header) {
      display: flex;
      align-items: center;
      padding: 6px 12px;
      font-size: 12px;
      color: #666;
      background: #f5f5f5;
      border-bottom: 1px solid #e5e5e5;
    }

    :deep(.sql-result-set) {
      margin: 8px 0;
      border: 1px solid var(--m-border);
      border-radius: 8px;
      overflow: hidden;
    }

    :deep(.sql-result-set-header) {
      padding: 8px 12px;
      font-size: 13px;
      font-weight: 600;
      color: var(--m-text-regular);
      background: var(--m-fill-tertiary);
      border-bottom: 1px solid var(--m-border);
    }

    :deep(.sql-result-set table) {
      margin: 0;
    }

    :deep(.result-set-error),
    :deep(.result-set-empty) {
      padding: 12px;
      font-size: 13px;
      color: var(--m-text-soft);
      text-align: center;
    }

    :deep(.result-set-error) {
      color: #e74c3c;
    }

    :deep(.markdown-report) {
      margin-bottom: 8px;
    }
  }

  &__actions {
    display: flex;
    gap: 4px;
    margin-left: -6px;
    opacity: 0.6;
  }

  &__action {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    color: var(--m-text-soft);
    cursor: pointer;
    background: transparent;
    border: none;
    border-radius: 8px;

    &:active {
      background: var(--m-border-soft);
    }
  }
}

.dot {
  width: 6px;
  height: 6px;
  background: var(--m-text-muted);
  border-radius: 50%;
  animation: m-typing 1.2s infinite ease-in-out;

  &:nth-child(2) {
    animation-delay: 0.15s;
  }

  &:nth-child(3) {
    animation-delay: 0.3s;
  }
}

@keyframes m-typing {
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
</style>
