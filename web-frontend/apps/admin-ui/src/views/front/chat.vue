<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';
import type { Agent as ChatAgent, ChatSession } from '@phoenix/chat-shared';
import { Expand, Plus } from '@element-plus/icons-vue';
import { ElButton, ElIcon, ElTooltip } from 'element-plus';

import { getAgentListApi } from '#/api/front/agent';
import { useAuthStore } from '#/store';
import { getAgentSessionsApi } from '#/api/front/chat';

import AgentListPanel from './components/AgentListPanel.vue';
import ChatHistoryPanel from './components/ChatHistoryPanel.vue';
import ChatMessages from './components/ChatMessages.vue';
import ChatComposer from './components/ChatComposer.vue';
import { apiChatTransport } from './api-transport';

const route = useRoute();
const router = useRouter();
const agentStore = useAgentStore();
const chatStore = useChatStore();
const { agents } = storeToRefs(agentStore);
const { activeSession, sessions, activeMessages, sending } = storeToRefs(chatStore);
const chatHistoryPanelRef = ref(null);

const LS_KEY_AGENT = 'phoenix-chat-agent-panel-collapsed';
const LS_KEY_HISTORY = 'phoenix-chat-history-panel-collapsed';

const agentPanelCollapsed = ref(
  localStorage.getItem(LS_KEY_AGENT) === 'true',
);
const chatHistoryPanelCollapsed = ref(
  localStorage.getItem(LS_KEY_HISTORY) === 'true',
);

watch(agentPanelCollapsed, (v) => {
  localStorage.setItem(LS_KEY_AGENT, String(v));
});
watch(chatHistoryPanelCollapsed, (v) => {
  localStorage.setItem(LS_KEY_HISTORY, String(v));
});
const noAgents = ref(false);

const currentAgent = computed(() => {
  if (activeSession.value) {
    return (
      agents.value.find((a) => a.id === String(activeSession.value?.agentId)) ??
      null
    );
  }
  const activeId = agentStore.activeAgentId;
  if (activeId) {
    return agents.value.find((a) => a.id === activeId) ?? null;
  }
  return agents.value[0] ?? null;
});

function isImageUrl(val: string) {
  return val.startsWith('/') || val.startsWith('http');
}
const avatarError = ref(false);

function toggleAgentPanel() {
  agentPanelCollapsed.value = !agentPanelCollapsed.value;
}

async function init() {
  chatStore.setTransport(apiChatTransport);
  try {
    const apiAgents = await getAgentListApi('published');
    agentStore.agents = apiAgents.map((a) => ({
      id: String(a.id),
      name: a.name || '',
      sn: a.sn || '',
      description: a.description || '',
      avatar: a.avatar || (a.name || '').charAt(0) || 'A',
      type: a.type ?? '',
    })) as ChatAgent[];
  } catch {
    await agentStore.loadAll();
  }
  if (agents.value.length === 0) {
    noAgents.value = true;
    return;
  }
  let agentId = route.query.agentId as string | undefined;
  if (!agentId || !agents.value.some((a) => a.id === agentId)) {
    const first = agents.value[0];
    if (first) {
      agentId = first.id;
      router.replace({ query: { agentId } });
    } else {
      return;
    }
  }
  await loadAgentSessions(agentId);
  await agentStore.setActiveAgent(agentId);
  const existing = chatStore.sessions.find(
    (s: ChatSession) => s.agentId === agentId,
  );
  if (existing) {
    try {
      await chatStore.switchSession(existing.id);
    } catch {
      chatStore.messagesByS = { ...chatStore.messagesByS, [existing.id]: [] };
    }
  } else {
    chatStore.createSession(agentId);
  }
}

async function loadAgentSessions(agentId: string) {
  chatStore.loadingSessions = true;
  try {
    const list = await getAgentSessionsApi(agentId);
    chatStore.sessions = list.map((s: Record<string, any>) => ({
      id: String(s.id),
      title: s.title || '',
      preview: s.preview || '',
      agentId: String(s.agentId ?? agentId),
      updatedAt: s.updateTime ? new Date(s.updateTime).getTime() : Date.now(),
      isPinned: !!s.isPinned,
    })) as ChatSession[];
  } catch {
    chatStore.loadSessions();
  } finally {
    chatStore.loadingSessions = false;
  }
}

function handleNewChat(){
  chatHistoryPanelRef.value?.handleNewChat();
}

function handleExpandChatHistoryPanel() {
  chatHistoryPanelCollapsed.value = false;
}
function handleCloseChatHistoryPanel() {
  chatHistoryPanelCollapsed.value = true;
}

init();
</script>

<template>
  <div v-if="noAgents" class="chat-layout__no-agents">
    <img class="chat-layout__no-agents-logo" src="/public/imgs/logo.png" alt="logo" />
    <span class="chat-layout__no-agents-text">暂无可用的智能体，请联系管理员！</span>
    <ElButton type="primary" plain @click="useAuthStore().logout(true)">
      退出登录
    </ElButton>
  </div>
  <div v-else class="chat-layout">
    <aside class="chat-layout__history" :class="{'hidden': chatHistoryPanelCollapsed}">
      <ChatHistoryPanel ref="chatHistoryPanelRef" @collapse="handleCloseChatHistoryPanel"/>
    </aside>

    <section class="chat-layout__main relative">
      <div v-if="sessions.length === 0 && !sending" class="chat-layout__empty">
        <div class="chat-layout__empty-content">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="chat-layout__empty-icon">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <p class="chat-layout__empty-text">暂无会话消息</p>
          <button type="button" class="chat-layout__empty-btn" @click="handleNewChat">
            开启新会话
          </button>
        </div>
      </div>
      <template v-else>
      <div v-if="chatHistoryPanelCollapsed" class="collapsed-bar">
        <el-tooltip :content="currentAgent?.name || ''" placement="bottom" :show-after="300">
          <div class="collapsed-bar__avatar" @click="handleExpandChatHistoryPanel">
            <img
              v-if="
                currentAgent?.avatar &&
                isImageUrl(currentAgent.avatar) &&
                !avatarError
              "
              :src="currentAgent.avatar"
              :alt="currentAgent.name"
              class="collapsed-bar__avatar-img"
              @error="avatarError = true"
            />
            <span v-else>{{
              (currentAgent?.name && currentAgent.name.charAt(0)) || '·'
            }}</span>
          </div>
        </el-tooltip>
        <div class="collapsed-bar__actions">
          <el-tooltip content="展开历史面板" placement="bottom" :show-after="300">
            <button
              type="button"
              class="collapsed-bar__btn"
              @click="handleExpandChatHistoryPanel"
            >
              <el-icon><Expand /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="开启新对话" placement="bottom" :show-after="300">
            <button
              type="button"
              class="collapsed-bar__btn"
              @click="handleNewChat"
            >
              <el-icon><Plus /></el-icon>
            </button>
          </el-tooltip>
        </div>
      </div>
      <div class="chat-layout__messages">
        <ChatMessages />
      </div>
      <div class="chat-layout__composer">
        <ChatComposer />
      </div>

      <el-tooltip :content="agentPanelCollapsed ? '展开智能体列表' : '收起智能体列表'" placement="left" :show-after="300">
        <button
            type="button"
            class="chat-layout__toggle"
            :class="{ 'is-collapsed': agentPanelCollapsed }"
            :aria-label="agentPanelCollapsed ? '展开智能体列表' : '收起智能体列表'"
            @click="toggleAgentPanel"
        >
        <span class="chat-layout__toggle-icon">
          {{ agentPanelCollapsed ? '‹' : '›' }}
        </span>
        </button>
      </el-tooltip>
      </template>
    </section>

    <aside
      class="chat-layout__agents"
      :class="{ 'is-collapsed': agentPanelCollapsed, 'chat-history-is-collapsed': chatHistoryPanelCollapsed }"
    >
      <AgentListPanel :collapsed="agentPanelCollapsed" />
    </aside>
  </div>
</template>

<style lang="scss" scoped>
.chat-layout {
  --pc-chat-content-width: 900px;

  display: grid;
  grid-template-columns: auto minmax(0, 1fr) 300px;
  gap: 12px;
  width: 100%;
  height: 100%;
  padding: 12px;
  background: hsl(var(--background-deep));

  &:has(.chat-layout__history.hidden) {
    grid-template-columns: minmax(0, 1fr) 300px;
    gap: 0 12px;
  }

  &__history,
  &__agents {
    min-height: 0;
    overflow: hidden;
    background: hsl(var(--card));
    border: 1px solid hsl(var(--border));
    border-radius: 12px;
    box-shadow: 0 1px 2px hsl(var(--foreground) / 6%);
  }

  &__main {
    position: relative;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    background: hsl(var(--card));
    border: 1px solid hsl(var(--border));
    border-radius: 12px;
    box-shadow: 0 1px 2px hsl(var(--foreground) / 6%);
  }

  &__messages {
    flex: 1 1 auto;
    min-height: 0;
    overflow: hidden;
  }

  &__composer {
    flex: 0 0 auto;
    background: hsl(var(--background));
    border-top: 1px solid hsl(var(--border));
  }

  &__toggle {
    position: absolute;
    top: 50%;
    right: -1px;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 48px;
    padding: 0;
    color: hsl(var(--muted-foreground));
    cursor: pointer;
    background: hsl(var(--card) / 60%);
    border: 1px solid hsl(var(--border));
    border-right: none;
    border-radius: 6px 0 0 6px;
    transform: translateY(-50%);
    transition: background 0.15s ease;

    &:hover {
      color: hsl(var(--primary));
      background: hsl(var(--primary) / 20%);
    }
  }

  &__toggle-icon {
    font-size: 14px;
    line-height: 1;
  }

  &__agents {
  }

  &:has(.chat-layout__agents.is-collapsed) {
    grid-template-columns: auto minmax(0, 1fr) 64px;
  }

  &:has(.chat-layout__agents.is-collapsed.chat-history-is-collapsed) {
    grid-template-columns: minmax(0, 1fr) 64px !important;
  }

  &__no-agents {
    display: flex;
    flex-direction: column;
    gap: 20px;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;

    &-logo {
      width: 60px;
      height: auto;
    }

    &-text {
      font-size: 15px;
      color: hsl(var(--muted-foreground));
    }
  }
}

.chat-layout__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;

  &-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }

  &-icon {
    color: hsl(var(--muted-foreground));
    opacity: 0.5;
  }

  &-text {
    font-size: 14px;
    color: hsl(var(--muted-foreground));
  }

  &-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 36px;
    padding: 0 20px;
    font-family: inherit;
    font-size: 14px;
    color: hsl(var(--primary-foreground));
    cursor: pointer;
    background: hsl(var(--primary));
    border: none;
    border-radius: 8px;
    transition: opacity 0.15s ease;

    &:hover {
      opacity: 0.9;
    }
  }
}

.collapsed-bar {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
  display: flex;
  gap: 8px;
  align-items: center;

  &__avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    overflow: hidden;
    font-size: 13px;
    font-weight: 600;
    color: hsl(var(--primary));
    cursor: pointer;
    background: hsl(var(--primary) / 12%);
    border-radius: 50%;
    transition: opacity 0.15s ease;

    &:hover {
      opacity: 0.8;
    }

    &-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &__actions {
    display: flex;
    gap: 8px;
    align-items: center;
    padding: 4px;
    background: hsl(var(--card));
    border: 1px solid hsl(var(--border));
    border-radius: 9999px;
    box-shadow: 0 2px 8px hsl(var(--foreground) / 8%);
  }

  &__btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    padding: 0;
    color: hsl(var(--muted-foreground));
    cursor: pointer;
    background: transparent;
    border: 1px solid transparent;
    border-radius: 9999px;
    transition: all 0.15s ease;

    &:hover {
      color: hsl(var(--primary));
      background: hsl(var(--primary) / 10%);
      border-color: hsl(var(--primary) / 20%);
    }
  }
}
</style>
