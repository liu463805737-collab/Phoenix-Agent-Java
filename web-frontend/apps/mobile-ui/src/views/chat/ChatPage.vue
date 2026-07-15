<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import {
  useAgentStore,
  useChatSession,
  useChatStore,
} from '@phoenix/chat-shared';
import { getAgentSessionsApi } from '../../services/chat';
import { showToast } from 'vant';

import { useActionMenu } from '../../components/useActionMenu';
import ChatBubble from '../../components/chat/ChatBubble.vue';
import ChatComposer from '../../components/chat/ChatComposer.vue';
import SessionsDrawer from '../../components/chat/SessionsDrawer.vue';
import AgentPickerSheet from '../../components/chat/AgentPickerSheet.vue';

const router = useRouter();
const route = useRoute();
const chat = useChatStore();
const agentStore = useAgentStore();
const { activeMessages, activeSessionId, sending, currentAgent } =
  useChatSession();
const { sessions } = storeToRefs(chat);

const scrollRef = ref<HTMLElement | null>(null);
const drawerOpen = ref(false);
const pickerOpen = ref(false);
const avatarError = ref(false);
const bubbleMenu = useActionMenu();
const longPressIndex = ref<number>(-1);

const SUGGESTIONS = [
  '帮我看下上周华东大区的 GMV 同比变化',
  '生成一份本周工作总结',
  '把这段代码优化一下并解释',
  '给我推荐一条北京周末的城市漫步路线',
];

const hasMessages = computed(() => activeMessages.value.length > 0);
const greetingTitle = computed(
  () => `有什么可以帮你的，${currentAgent.value?.name ?? '我'}？`,
);

async function scrollToBottom() {
  await nextTick();
  const el = scrollRef.value;
  if (!el) return;
  el.scrollTop = el.scrollHeight;
}

watch(
  () => activeMessages.value.length,
  () => void scrollToBottom(),
);
watch(activeSessionId, () => void scrollToBottom());
watch(currentAgent, () => { avatarError.value = false; });

onMounted(async () => {
  if (agentStore.agents.length === 0) await agentStore.loadAll();

  const agentId = (route.query.agentId as string) || agentStore.agents[0]?.id;
  if (agentId) {
    agentStore.setActiveAgent(agentId);
    const list = await getAgentSessionsApi(agentId);
    chat.sessions = list;
    if (list.length > 0) {
      await chat.switchSession(list[0]!.id);
    }
  } else if (sessions.value.length > 0 && activeSessionId.value) {
    await chat.loadMessages(activeSessionId.value);
  }
  void scrollToBottom();
});

async function ensureSession() {
  if (activeSessionId.value) return activeSessionId.value;
  const agentId = currentAgent.value?.id ?? agentStore.agents[0]?.id;
  if (!agentId) return null;
  await router.replace({ query: { agentId } });
  const s = await chat.createSession(agentId);
  return s.id;
}

async function handleSend(content: string) {
  const id = await ensureSession();
  if (!id) return;
  await chat.send(content);
}

async function handleNewChat() {
  const agentId = currentAgent.value?.id ?? agentStore.agents[0]?.id;
  if (!agentId) return;
  await router.replace({ query: { agentId } });
  await chat.createSession(agentId);
}

async function handlePickAgent(agentId: string) {
  agentStore.setActiveAgent(agentId);
  await router.replace({ query: { agentId } });
  const list = await getAgentSessionsApi(agentId);
  chat.sessions = list;
  if (list.length > 0) {
    await chat.switchSession(list[0]!.id);
  } else {
    await chat.createSession(agentId);
  }
}

async function handleSelectSession(id: string) {
  const session = chat.sessions.find((s) => s.id === id);
  const agentId = session?.agentId ?? currentAgent.value?.id;
  if (agentId) {
    agentStore.setActiveAgent(agentId);
    await router.replace({ query: { agentId } });
  }
  await chat.switchSession(id);
}

function handleSuggest(text: string) {
  void handleSend(text);
}

async function handleLongPress(idx: number) {
  longPressIndex.value = idx;
  const r = await bubbleMenu.open([{ name: '复制' }, { name: '重新生成' }]);
  if (!r) return;
  if (r.name === '复制') {
    const msg = activeMessages.value[idx];
    if (msg) {
      try {
        await navigator.clipboard.writeText(msg.content);
        showToast({ message: '已复制', position: 'bottom' });
      } catch {
        /* ignore */
      }
    }
  } else if (r.name === '重新生成') {
    const last = activeMessages.value
      .slice(0, idx + 1)
      .toReversed()
      .find((m) => m.role === 'user');
    if (last) await chat.send(last.content);
  }
}

async function handleCopy(content: string) {
  try {
    await navigator.clipboard.writeText(content);
    showToast({ message: '已复制', position: 'bottom' });
  } catch {
    /* ignore */
  }
}

async function handleRegenerate(idx: number) {
  const last = activeMessages.value
    .slice(0, idx + 1)
    .toReversed()
    .find((m) => m.role === 'user');
  if (last) await chat.send(last.content);
}
</script>

<template>
  <div>
    <div class="chat-page" :class="{'slide-with-drawer': drawerOpen}" >
      <header class="chat-nav">
        <button
            type="button"
            class="chat-nav__btn"
            aria-label="历史"
            @click="drawerOpen = true"
        >
          <svg viewBox="0 0 24 24" width="28" height="28" aria-hidden="true">
            <path
                d="M4 7h16M4 12h16M4 17h10"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
            />
          </svg>
        </button>

        <button
            type="button"
            class="chat-nav__title"
            aria-label="切换智能体"
            @click="pickerOpen = true"
        >
          <span style="line-height: 1;">{{ currentAgent?.name ?? '智能体' }}</span>
          <van-icon name="arrow-down" />
        </button>

        <button
            type="button"
            class="chat-nav__btn"
            aria-label="新建对话"
            @click="handleNewChat"
        >
          <svg viewBox="0 0 24 24" width="28" height="28" aria-hidden="true">
            <path
                d="M12 5v14M5 12h14"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
            />
          </svg>
        </button>
      </header>

      <div ref="scrollRef" class="chat-page__scroll">
        <div v-if="!hasMessages" class="empty-state">
          <div class="empty-state__avatar">
            <img
                v-if="currentAgent?.avatar && !avatarError && (currentAgent.avatar.startsWith('/') || currentAgent.avatar.startsWith('http'))"
                :src="currentAgent.avatar"
                :alt="currentAgent.name || '智能体'"
                class="empty-state__avatar-img"
                @error="avatarError = true"
            />
            <span v-else>{{ currentAgent?.name ? [...currentAgent.name][0] : '智' }}</span>
          </div>
          <div class="empty-state__title">{{ greetingTitle }}</div>
          <div class="empty-state__sub">
            {{ currentAgent?.description ?? '挑一个智能体或直接发问' }}
          </div>
          <div class="empty-state__suggest">
            <button
                v-for="(s, i) in SUGGESTIONS"
                :key="i"
                type="button"
                class="suggest-chip"
                @click="handleSuggest(s)"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <div v-else class="chat-page__inner">
          <ChatBubble
              v-for="(msg, idx) in activeMessages"
              :key="msg.id"
              :role="msg.role"
              :content="msg.content"
              :message-type="msg.messageType ?? 'text'"
              :bot-avatar="currentAgent?.avatar || '智'"
              @longpress="handleLongPress(idx)"
              @copy="handleCopy(msg.content)"
              @regenerate="handleRegenerate(idx)"
          />
          <ChatBubble
              v-if="sending"
              role="assistant"
              :bot-avatar="currentAgent?.avatar || '智'"
              typing
          />
        </div>
      </div>

      <div class="chat-page__composer">
        <ChatComposer :disabled="sending" @submit="handleSend" />
      </div>

    </div>

    <SessionsDrawer
        v-model:show="drawerOpen"
        @select="handleSelectSession"
        @new-chat="handleNewChat"
        @goto-me="router.push('/me')"
    />

    <AgentPickerSheet v-model:show="pickerOpen" @picked="handlePickAgent" />

    <van-action-sheet
        v-model:show="bubbleMenu.state.show"
        :actions="bubbleMenu.state.actions"
        :cancel-text="bubbleMenu.state.cancelText"
        close-on-click-action
        @select="bubbleMenu.onSelect"
        @cancel="bubbleMenu.onCancel"
    />
  </div>
</template>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100dvh;
  background: var(--m-bg);
  transition: transform var(--van-duration-base) ease-out;
}

.chat-nav {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
  height: calc(var(--m-navbar-h) + var(--m-safe-top));
  padding: 6px 8px;
  padding-top: calc(var(--m-safe-top) + 6px);
  background: var(--m-bg);
}

.chat-nav__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: var(--m-text-primary);
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 10px;
  transition: background 0.15s ease;
}

.chat-nav__btn:active {
  background: var(--m-border-soft);
}

.chat-nav__title {
  display: inline-flex;
  flex: 1 1 auto;
  gap: 4px;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--m-text-primary);
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 18px;
}

.chat-nav__title:active {
  background: var(--m-border-soft);
}

.chat-page__scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}

.chat-page__inner {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 12px 14px;
}

.chat-page__composer {
  flex: 0 0 auto;
  background: var(--m-bg);
}

.empty-state {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 32px 20px;
  text-align: center;
}

.empty-state__avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 6px;
  font-size: 22px;
  font-weight: 600;
  color: var(--m-brand-primary);
  background: var(--m-brand-primary-soft);
  border-radius: 50%;
  overflow: hidden;
}

.empty-state__avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.empty-state__title {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.35;
  color: var(--m-text-primary);
}

.empty-state__sub {
  font-size: 13px;
  color: var(--m-text-soft);
}

.empty-state__suggest {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-width: 340px;
  margin-top: 14px;
}

.suggest-chip {
  display: block;
  width: 100%;
  padding: 12px 14px;
  font-size: 14px;
  color: var(--m-text-regular);
  text-align: left;
  cursor: pointer;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 14px;
  transition:
    background 0.15s ease,
    border-color 0.15s ease;
}

.suggest-chip:active {
  background: var(--m-brand-primary-soft);
  border-color: rgb(47 107 255 / 40%);
}

.slide-with-drawer {
  /* 位移距离应与抽屉宽度一致，此处为 80% */
  transform: translateX(70%);
}
</style>
