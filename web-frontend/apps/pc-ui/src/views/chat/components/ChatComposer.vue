<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useChatStore } from '@phoenix/chat-shared';

const chat = useChatStore();
const { sending } = storeToRefs(chat);

const inputValue = ref('');
const textareaRef = ref<HTMLTextAreaElement | null>(null);

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

async function handleSubmit() {
  const value = inputValue.value.trim();
  if (!value) return;
  if (sending.value) return;
  inputValue.value = '';
  resize();
  await chat.send(value);
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter') return;
  if (event.shiftKey || event.isComposing) return;
  event.preventDefault();
  handleSubmit();
}
</script>

<template>
  <div class="composer">
    <form class="composer__inner" @submit.prevent="handleSubmit">
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
        <button
          type="submit"
          class="composer__send"
          :disabled="!inputValue.trim()"
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
      </div>
    </form>
  </div>
</template>

<style lang="scss" scoped>
.composer {
  padding: 12px 24px 16px;

  &__inner {
    display: flex;
    flex-direction: column;
    max-width: var(--pc-chat-content-width);
    margin: 0 auto;
    background: #fff;
    border: 1px solid var(--pc-border);
    border-radius: 12px;
    transition:
      border-color 0.15s ease,
      box-shadow 0.15s ease;

    &:focus-within {
      border-color: var(--pc-primary);
      box-shadow: 0 0 0 3px rgb(37 99 235 / 12%);
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
    color: var(--pc-text);
    resize: none;
    outline: none;
    background: transparent;
    border: none;

    &::placeholder {
      color: var(--pc-text-muted);
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
    background: var(--pc-primary);
    border: 1px solid var(--pc-primary);
    border-radius: 8px;
    transition:
      background 0.15s ease,
      color 0.15s ease,
      border-color 0.15s ease,
      opacity 0.15s ease;

    &:hover {
      filter: brightness(0.95);
    }

    &:disabled {
      color: var(--pc-text-muted);
      cursor: not-allowed;
      background: #f1f3f8;
      border-color: var(--pc-border);
    }
  }
}
</style>
