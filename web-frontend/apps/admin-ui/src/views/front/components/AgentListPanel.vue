<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';
import type { ChatSession } from '@phoenix/chat-shared';
import {
  ElTooltip,
} from 'element-plus';

import { getAgentSessionsApi } from '#/api/front/chat';

interface Props {
  collapsed?: boolean;
}

withDefaults(defineProps<Props>(), {
  collapsed: false,
});

const agentStore = useAgentStore();
const chat = useChatStore();
const { agents } = storeToRefs(agentStore);

const route = useRoute();
const router = useRouter();

const selectedAgentId = ref<string | null>(null);
const avatarError = ref<Record<string, boolean>>({});

watch(
  () => route.query.agentId,
  (id) => {
    selectedAgentId.value = (id as string) || agents.value[0]?.id || null;
  },
  { immediate: true },
);

async function handlePick(id: string) {
  selectedAgentId.value = id;
  agentStore.setActiveAgent(id);
  router.replace({ query: { ...route.query, agentId: id } });
  chat.loadingSessions = true;
  try {
    const list = await getAgentSessionsApi(id);
    chat.sessions = list.map((s: Record<string, any>) => ({
      id: String(s.id),
      title: s.title || '',
      preview: s.preview || '',
      agentId: String(s.agentId ?? id),
      updatedAt: s.updateTime ? new Date(s.updateTime).getTime() : Date.now(),
    })) as ChatSession[];
  } catch {
    await chat.loadSessions();
  } finally {
    chat.loadingSessions = false;
  }
  const exist = chat.sessions.find((s) => s.agentId === id);
  await (exist ? chat.switchSession(exist.id) : chat.createSession(id));
}

function isImageUrl(val: string) {
  return val.startsWith('/') || val.startsWith('http');
}
</script>

<template>
  <div class="agent-panel" :class="{ 'is-collapsed': collapsed }">
    <header class="agent-panel__header">
      <div class="agent-panel__title">
        <img class="agent-panel__logo" src="/public/imgs/logo.png" alt="logo" />
        <span v-if="!collapsed">智能体</span>
      </div>
      <div v-if="!collapsed" class="agent-panel__desc">
        挑一个最懂你工作场景的助手
      </div>
    </header>

    <div class="agent-panel__list">
      <template v-for="agent in agents" :key="agent.id">
        <el-tooltip
          v-if="collapsed"
          :content="agent.name"
          placement="right"
          :show-after="300"
        >
          <button
            type="button"
            class="agent-item is-collapsed"
            :class="{ 'is-active': agent.id === selectedAgentId }"
            @click="handlePick(agent.id)"
          >
            <span class="agent-item__avatar">
              <img
                v-if="isImageUrl(agent.avatar) && !avatarError[agent.id]"
                :src="agent.avatar"
                :alt="agent.name"
                class="agent-item__avatar-img"
                @error="avatarError[agent.id] = true"
              />
              <span v-else>{{ (agent.name && agent.name.charAt(0)) || 'A' }}</span>
            </span>
          </button>
        </el-tooltip>
        <button
          v-else
          type="button"
          class="agent-item"
          :class="{ 'is-active': agent.id === selectedAgentId }"
          @click="handlePick(agent.id)"
        >
          <span class="agent-item__avatar">
            <img
              v-if="isImageUrl(agent.avatar) && !avatarError[agent.id]"
              :src="agent.avatar"
              :alt="agent.name"
              class="agent-item__avatar-img"
              @error="avatarError[agent.id] = true"
            />
            <span v-else>{{ (agent.name && agent.name.charAt(0)) || 'A' }}</span>
          </span>
          <span class="agent-item__body">
            <span class="agent-item__title-row">
              <span class="agent-item__name">{{ agent.name }}</span>
              <span v-if="agent.tag" class="agent-item__tag">{{
                agent.tag
              }}</span>
            </span>
            <span class="agent-item__desc">{{ agent.description }}</span>
          </span>
        </button>
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.agent-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;

  &__header {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 16px;
    border-bottom: 1px solid hsl(var(--border));
  }

  &__title {
    display: flex;
    gap: 8px;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
    color: hsl(var(--foreground));
  }

  &__logo {
    flex-shrink: 0;
    width: 24px;
    height: 24px;
  }

  &__desc {
    font-size: 12px;
    color: hsl(var(--muted-foreground));
  }

  &__list {
    display: flex;
    flex: 1 1 auto;
    flex-direction: column;
    gap: 4px;
    min-height: 0;
    padding: 8px;
    overflow-y: auto;
  }

  &.is-collapsed {
    align-items: center;

    .agent-panel__list {
      gap: 6px;
      align-items: center;
      padding: 12px 6px;
    }
  }
}

.agent-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  width: 100%;
  padding: 10px;
  color: hsl(var(--foreground));
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  transition:
    background 0.15s ease,
    border-color 0.15s ease;

  &:hover {
    background: hsl(var(--accent));
    border-color: hsl(var(--border));
  }

  &.is-active {
    background: hsl(var(--primary) / 12%);
    border-color: hsl(var(--primary) / 25%);
  }

  &.is-collapsed {
    align-items: center;
    justify-content: center;
    width: 44px;
    padding: 6px;
  }

  &__avatar {
    position: relative;
    display: flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    margin-top: 2px;
    overflow: hidden;
    font-size: 13px;
    font-weight: 600;
    color: hsl(var(--primary));
    background: hsl(var(--primary) / 12%);
    border-radius: 50%;

    &-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &.is-collapsed &__avatar {
    margin-top: 0;
  }

  &.is-active &__avatar {
    color: hsl(var(--primary-foreground));
    background: hsl(var(--primary));
  }

  &__body {
    display: flex;
    flex: 1 1 auto;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  &__title-row {
    display: flex;
    gap: 6px;
    align-items: center;
    min-width: 0;
  }

  &__name {
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    font-weight: 500;
    color: hsl(var(--foreground));
    white-space: nowrap;
  }

  &__tag {
    flex: 0 0 auto;
    height: 16px;
    padding: 0 6px;
    font-size: 11px;
    line-height: 16px;
    color: hsl(var(--primary));
    background: hsl(var(--background));
    border: 1px solid hsl(var(--primary) / 30%);
    border-radius: 8px;
  }

  &__desc {
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    font-size: 12px;
    line-height: 1.5;
    color: hsl(var(--muted-foreground));
    -webkit-box-orient: vertical;
  }
}
</style>
