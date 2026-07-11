<script setup lang="ts">
import { ref } from 'vue';

interface Props {
  role: 'assistant' | 'user';
  content?: string;
  botAvatar?: string;
  typing?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  botAvatar: '智',
  typing: false,
});

const emit = defineEmits<{
  (e: 'longpress'): void;
  (e: 'copy'): void;
  (e: 'regenerate'): void;
}>();

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
      <div class="bubble__avatar">{{ props.botAvatar }}</div>
      <div class="bubble__main">
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
          <template v-else>{{ props.content }}</template>
        </div>
        <div v-if="!props.typing && props.content" class="bubble__actions">
          <button
            type="button"
            class="bubble__action"
            aria-label="复制"
            @click="emit('copy')"
          >
            <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
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
            <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
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
        {{ props.content }}
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
  }

  &__main {
    display: flex;
    flex: 1 1 auto;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  &__content {
    font-size: 15px;
    line-height: 1.65;
    color: var(--m-text-primary);
    word-break: break-word;
    white-space: pre-wrap;

    &--user {
      max-width: 78%;
      padding: 10px 14px;
      font-size: 15px;
      line-height: 1.55;
      color: var(--m-bubble-user-fg);
      background: var(--m-bubble-user-bg);
      border-radius: var(--m-radius-bubble);
    }

    &--pressed {
      opacity: 0.72;
    }

    &--typing {
      display: inline-flex;
      gap: 5px;
      align-items: center;
      padding: 6px 0;
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
