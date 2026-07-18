<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';

interface Props {
  disabled?: boolean;
}
const props = withDefaults(defineProps<Props>(), { disabled: false });

const emit = defineEmits<{
  (e: 'submit', content: string): void;
}>();

const value = ref('');
const textareaRef = ref<HTMLTextAreaElement | null>(null);

const MIN_H = 24;
const MAX_H = 140;

async function resize() {
  await nextTick();
  const el = textareaRef.value;
  if (!el) return;
  el.style.height = 'auto';
  const next = Math.min(Math.max(el.scrollHeight, MIN_H), MAX_H);
  el.style.height = `${next}px`;
  el.style.overflowY = el.scrollHeight > MAX_H ? 'auto' : 'hidden';
}

watch(value, () => resize());

function handleSubmit() {
  const trimmed = value.value.trim();
  if (!trimmed || props.disabled) return;
  emit('submit', trimmed);
  value.value = '';
  resize();
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter') return;
  if (event.shiftKey || event.isComposing) return;
  event.preventDefault();
  handleSubmit();
}
</script>

<template>
  <form class="composer" @submit.prevent="handleSubmit">
    <div class="composer__shell">
      <textarea
        ref="textareaRef"
        v-model="value"
        class="composer__input"
        rows="1"
        placeholder="发消息给智能体…"
        @keydown="handleKeydown"
      />
      <button
        type="submit"
        class="composer__send"
        :class="{ 'is-ready': !!value.trim() && !props.disabled }"
        :disabled="!value.trim() || props.disabled"
        aria-label="发送"
      >
        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
          <path
            d="M12 19V5M5 12l7-7 7 7"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>
  </form>
</template>

<style lang="scss" scoped>
.composer {
  padding: 8px 12px 12px;
}

.composer__shell {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 8px 8px 8px 16px;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: var(--m-radius-input);
  transition: border-color 0.15s ease;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.08);

  &:focus-within {
    border-color: var(--m-brand-primary);
  }
}

.composer__input {
  flex: 1 1 auto;
  min-height: 24px;
  max-height: 140px;
  font-size: 16px;
  line-height: 1.5;
  color: var(--m-text-primary);
  resize: none;
  outline: none;
  background: transparent;
  border: none;
}

.composer__input::placeholder {
  color: var(--m-text-muted);
}

.composer__send {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: var(--m-text-muted);
  cursor: pointer;
  background: var(--m-border);
  border: none;
  border-radius: 50%;
  transition:
    background 0.15s ease,
    color 0.15s ease,
    transform 0.15s ease;

  &.is-ready {
    color: #fff;
    background: var(--m-brand-primary);
  }

  &:active.is-ready {
    transform: scale(0.94);
  }

  &:disabled {
    cursor: not-allowed;
  }
}
</style>
