<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';

const chat = useChatStore();
const agentStore = useAgentStore();
const { activeMessages, activeSession, activeSessionId, sending } =
  storeToRefs(chat);
const { agents } = storeToRefs(agentStore);

const currentAgent = computed(() => {
  if (!activeSession.value) return null;
  return (
    agents.value.find((a) => a.id === activeSession.value!.agentId) ?? null
  );
});
const botAvatar = computed(() => currentAgent.value?.avatar ?? '智');

const scrollRef = ref<HTMLElement | null>(null);

async function scrollToBottom() {
  await nextTick();
  const el = scrollRef.value;
  if (!el) return;
  el.scrollTop = el.scrollHeight;
}

watch(
  () => activeMessages.value.length,
  () => {
    void scrollToBottom();
  },
);

watch(activeSessionId, () => {
  void scrollToBottom();
});
</script>

<template>
  <div ref="scrollRef" class="chat-messages">
    <div class="chat-messages__inner">
      <div
        v-for="msg in activeMessages"
        :key="msg.id"
        class="chat-message"
        :class="`chat-message--${msg.role}`"
      >
        <div
          v-if="msg.role === 'assistant'"
          class="chat-message__avatar chat-message__avatar--bot"
        >
          {{ botAvatar }}
        </div>

        <div class="chat-message__bubble">
          {{ msg.content }}
        </div>

        <div
          v-if="msg.role === 'user'"
          class="chat-message__avatar chat-message__avatar--user"
        >
          U
        </div>
      </div>

      <div
        v-if="sending"
        class="chat-message chat-message--assistant chat-message--typing"
      >
        <div class="chat-message__avatar chat-message__avatar--bot">
          {{ botAvatar }}
        </div>
        <div class="chat-message__bubble chat-message__bubble--typing">
          <span class="dot" />
          <span class="dot" />
          <span class="dot" />
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
  background: #f7f9fc;

  &__inner {
    display: flex;
    flex-direction: column;
    gap: 18px;
    max-width: var(--pc-chat-content-width);
    padding: 24px 24px 16px;
    margin: 0 auto;
  }
}

.chat-message {
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
    width: 32px;
    height: 32px;
    font-size: 13px;
    font-weight: 600;
    border-radius: 50%;

    &--bot {
      color: var(--pc-primary);
      background: var(--pc-primary-soft);
    }

    &--user {
      color: #fff;
      background: var(--pc-bubble-user);
    }
  }

  &__bubble {
    max-width: 70%;
    padding: 10px 14px;
    font-size: 14px;
    line-height: 1.65;
    word-break: break-word;
    white-space: pre-wrap;
    border-radius: 10px;
  }

  &--assistant &__bubble {
    color: var(--pc-text);
    background: var(--pc-bubble-bot);
    border: 1px solid var(--pc-border-soft);
  }

  &--user &__bubble {
    color: #fff;
    background: var(--pc-bubble-user);
  }
}

.chat-messages__empty {
  padding: 32px 0;
  font-size: 13px;
  color: var(--pc-text-muted);
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
    background: var(--pc-text-muted);
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
</style>
