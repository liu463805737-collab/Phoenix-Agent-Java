<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import {
  useAgentStore,
  useAuthStore,
  useChatStore,
} from '@phoenix/chat-shared';

const chat = useChatStore();
const agentStore = useAgentStore();
const { sessions, activeSessionId, activeSession } = storeToRefs(chat);
const { agents } = storeToRefs(agentStore);

const currentAgent = computed(() => {
  if (activeSession.value) {
    return (
      agents.value.find((a) => a.id === activeSession.value!.agentId) ?? null
    );
  }
  return agents.value[0] ?? null;
});
const currentAgentName = computed(
  () => currentAgent.value?.name ?? '未选择智能体',
);
const currentAgentAvatar = computed(() => currentAgent.value?.avatar ?? '·');

function formatUpdatedAt(ts: number): string {
  const d = new Date(ts);
  const now = new Date();
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate();
  const yest = new Date(now);
  yest.setDate(now.getDate() - 1);
  const isYesterday =
    d.getFullYear() === yest.getFullYear() &&
    d.getMonth() === yest.getMonth() &&
    d.getDate() === yest.getDate();
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  if (sameDay) return `今天 ${hh}:${mm}`;
  if (isYesterday) return '昨天';
  const mo = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${mo}-${dd}`;
}

const historyCollapsed = ref(false);
const historyKeyword = ref('');

function toggleHistory() {
  historyCollapsed.value = !historyCollapsed.value;
}

const filteredHistory = computed(() => {
  const keyword = historyKeyword.value.trim().toLowerCase();
  const list = sessions.value.map((s) => ({
    id: s.id,
    title: s.title,
    preview: s.preview,
    updatedAt: formatUpdatedAt(s.updatedAt),
  }));
  if (!keyword) return list;
  return list.filter((item) => {
    return (
      item.title.toLowerCase().includes(keyword) ||
      item.preview.toLowerCase().includes(keyword)
    );
  });
});

async function handleSelect(id: string) {
  await chat.switchSession(id);
}

async function handleNewChat() {
  const agentId = currentAgent.value?.id ?? agents.value[0]?.id;
  if (!agentId) return;
  await chat.createSession(agentId);
}

const router = useRouter();
const auth = useAuthStore();

const userName = computed(
  () => auth.user?.displayName || auth.user?.username || '未登录用户',
);
const userAvatar = computed(() => {
  const name = userName.value;
  // 取首个可见字符作为头像字符，兼容中英文
  const ch = [...name][0] ?? 'U';
  return ch.toUpperCase();
});
const userSubtitle = computed(() => {
  if (!auth.user?.loginAt) return '本地体验账号';
  const d = new Date(auth.user.loginAt);
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  return `本次登录 ${hh}:${mm}`;
});

const userMenuOpen = ref(false);
const userMenuRef = ref<HTMLElement | null>(null);

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value;
}

function handleOutsideClick(event: MouseEvent) {
  if (!userMenuOpen.value) return;
  const root = userMenuRef.value;
  if (root && !root.contains(event.target as Node)) {
    userMenuOpen.value = false;
  }
}

function handleEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    userMenuOpen.value = false;
  }
}

async function handleLogout() {
  userMenuOpen.value = false;
  auth.logout();
  await router.replace('/login');
}

onMounted(() => {
  document.addEventListener('mousedown', handleOutsideClick);
  document.addEventListener('keydown', handleEscape);
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleOutsideClick);
  document.removeEventListener('keydown', handleEscape);
});
</script>

<template>
  <div class="history-panel">
    <header class="history-panel__header">
      <div class="history-panel__agent">
        <div class="history-panel__avatar">{{ currentAgentAvatar }}</div>
        <div class="history-panel__agent-meta">
          <div class="history-panel__agent-name">{{ currentAgentName }}</div>
          <div class="history-panel__agent-sub">当前智能体</div>
        </div>
      </div>

      <button
        type="button"
        class="history-panel__new-chat"
        @click="handleNewChat"
      >
        <span class="history-panel__new-icon">+</span>
        开启新会话
      </button>
    </header>

    <button
      type="button"
      class="history-panel__section-title"
      :aria-expanded="!historyCollapsed"
      @click="toggleHistory"
    >
      <span>历史会话</span>
      <svg
        class="history-panel__chevron"
        :class="{ 'is-collapsed': historyCollapsed }"
        width="12"
        height="12"
        viewBox="0 0 12 12"
        aria-hidden="true"
      >
        <path
          d="M2.5 4.25 6 7.75l3.5-3.5"
          fill="none"
          stroke="currentColor"
          stroke-width="1.4"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </button>

    <div v-if="!historyCollapsed" class="history-panel__search">
      <svg
        class="history-panel__search-icon"
        width="14"
        height="14"
        viewBox="0 0 14 14"
        aria-hidden="true"
      >
        <circle
          cx="6"
          cy="6"
          r="4"
          fill="none"
          stroke="currentColor"
          stroke-width="1.4"
        />
        <path
          d="m9.2 9.2 3 3"
          stroke="currentColor"
          stroke-width="1.4"
          stroke-linecap="round"
        />
      </svg>
      <input
        v-model="historyKeyword"
        class="history-panel__search-input"
        type="search"
        placeholder="搜索历史会话"
      />
    </div>

    <div v-show="!historyCollapsed" class="history-panel__list">
      <button
        v-for="item in filteredHistory"
        :key="item.id"
        type="button"
        class="history-item"
        :class="{ 'is-active': item.id === activeSessionId }"
        @click="handleSelect(item.id)"
      >
        <div class="history-item__body">
          <div class="history-item__title-row">
            <div class="history-item__title">{{ item.title }}</div>
            <div class="history-item__time">{{ item.updatedAt }}</div>
          </div>
          <div class="history-item__preview">{{ item.preview }}</div>
        </div>
      </button>
      <div v-if="filteredHistory.length === 0" class="history-panel__empty">
        没有匹配的会话
      </div>
    </div>

    <footer
      ref="userMenuRef"
      class="user-card"
      :class="{ 'is-open': userMenuOpen }"
    >
      <button
        type="button"
        class="user-card__trigger"
        :aria-expanded="userMenuOpen"
        aria-haspopup="menu"
        @click="toggleUserMenu"
      >
        <span class="user-card__avatar">{{ userAvatar }}</span>
        <span class="user-card__meta">
          <span class="user-card__name">{{ userName }}</span>
          <span class="user-card__sub">{{ userSubtitle }}</span>
        </span>
        <svg
          class="user-card__caret"
          :class="{ 'is-open': userMenuOpen }"
          width="12"
          height="12"
          viewBox="0 0 12 12"
          aria-hidden="true"
        >
          <path
            d="M2.5 4.25 6 7.75l3.5-3.5"
            fill="none"
            stroke="currentColor"
            stroke-width="1.4"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>

      <transition name="user-menu">
        <div v-if="userMenuOpen" class="user-card__menu" role="menu">
          <div class="user-card__menu-header">
            <span class="user-card__avatar user-card__avatar--lg">{{
              userAvatar
            }}</span>
            <div class="user-card__menu-meta">
              <span class="user-card__menu-name">{{ userName }}</span>
              <span class="user-card__menu-sub">{{ userSubtitle }}</span>
            </div>
          </div>
          <button
            type="button"
            class="user-card__menu-item user-card__menu-item--danger"
            role="menuitem"
            @click="handleLogout"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M10 17l-5-5 5-5M5 12h12"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <span>退出登录</span>
          </button>
        </div>
      </transition>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.history-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;

  &__header {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px 16px 12px;
    border-bottom: 1px solid var(--pc-border-soft);
  }

  &__agent {
    display: flex;
    gap: 10px;
    align-items: center;
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
    color: var(--pc-primary);
    background: var(--pc-primary-soft);
    border-radius: 50%;
  }

  &__agent-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__agent-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--pc-text);
  }

  &__agent-sub {
    font-size: 12px;
    color: var(--pc-text-muted);
  }

  &__new-chat {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    justify-content: center;
    height: 32px;
    padding: 0 14px;
    font-size: 13px;
    color: var(--pc-text);
    cursor: pointer;
    background: #fff;
    border: 1px solid var(--pc-border);
    border-radius: 6px;
    transition:
      background 0.15s ease,
      border-color 0.15s ease,
      color 0.15s ease;

    &:hover {
      color: var(--pc-primary);
      background: var(--pc-primary-soft);
      border-color: var(--pc-primary);
    }
  }

  &__new-icon {
    font-size: 14px;
    line-height: 1;
  }

  &__section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding: 12px 16px 6px;
    font-family: inherit;
    font-size: 12px;
    color: var(--pc-text-muted);
    text-align: left;
    letter-spacing: 0.4px;
    cursor: pointer;
    background: transparent;
    border: none;
    transition: color 0.15s ease;

    &:hover {
      color: var(--pc-text-soft);
    }
  }

  &__chevron {
    color: currentcolor;
    transform: rotate(0deg);
    transition: transform 0.2s ease;

    &.is-collapsed {
      transform: rotate(-90deg);
    }
  }

  &__search {
    position: relative;
    display: flex;
    align-items: center;
    margin: 4px 12px 8px;
  }

  &__search-icon {
    position: absolute;
    left: 9px;
    color: var(--pc-text-muted);
    pointer-events: none;
  }

  &__search-input {
    width: 100%;
    height: 30px;
    padding: 0 10px 0 28px;
    font-family: inherit;
    font-size: 12px;
    color: var(--pc-text);
    outline: none;
    background: #fff;
    border: 1px solid var(--pc-border);
    border-radius: 6px;
    transition:
      border-color 0.15s ease,
      box-shadow 0.15s ease;

    &::placeholder {
      color: var(--pc-text-muted);
    }

    &:focus {
      border-color: var(--pc-primary);
      box-shadow: 0 0 0 3px rgb(37 99 235 / 12%);
    }
  }

  &__empty {
    padding: 16px 12px;
    font-size: 12px;
    color: var(--pc-text-muted);
    text-align: center;
  }

  &__list {
    display: flex;
    flex: 1 1 auto;
    flex-direction: column;
    gap: 2px;
    min-height: 0;
    padding: 0 8px 8px;
    overflow-y: auto;
  }
}

.history-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  padding: 10px;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 8px;
  transition:
    background 0.15s ease,
    border-color 0.15s ease;

  &:hover {
    background: #f5f7fb;
    border-color: var(--pc-border-soft);
  }

  &.is-active {
    background: var(--pc-primary-soft);
    border-color: rgb(37 99 235 / 25%);
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: 4px;
    min-width: 0;
  }

  &__title-row {
    display: flex;
    gap: 8px;
    align-items: center;
    min-width: 0;
  }

  &__title {
    flex: 1 1 auto;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    font-weight: 500;
    color: var(--pc-text);
    white-space: nowrap;
  }

  &__time {
    flex: 0 0 auto;
    font-size: 11px;
    color: var(--pc-text-muted);
  }

  &__preview {
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 1;
    font-size: 12px;
    line-height: 1.55;
    color: var(--pc-text-soft);
    -webkit-box-orient: vertical;
  }
}

.user-card {
  position: relative;
  display: flex;
  flex: 0 0 auto;
  gap: 6px;
  align-items: center;
  padding: 8px 8px 10px;
  border-top: 1px solid var(--pc-border-soft);

  &__trigger {
    display: flex;
    flex: 1 1 auto;
    gap: 10px;
    align-items: center;
    min-width: 0;
    padding: 8px 10px;
    font-family: inherit;
    color: var(--pc-text);
    cursor: pointer;
    background: transparent;
    border: 1px solid transparent;
    border-radius: 10px;
    transition:
      background 0.15s ease,
      border-color 0.15s ease,
      box-shadow 0.15s ease;

    &:hover {
      background: #f5f7fb;
    }
  }

  &.is-open &__trigger {
    background: #f5f7fb;
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
    color: #fff;
    background: linear-gradient(135deg, #4079ff 0%, #2f6bff 60%, #1d4ed8 100%);
    border-radius: 50%;
    box-shadow: 0 4px 10px rgb(47 107 255 / 25%);

    &--lg {
      width: 40px;
      height: 40px;
      font-size: 15px;
    }
  }

  &__meta {
    display: flex;
    flex: 1 1 auto;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
    text-align: left;
  }

  &__name {
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    font-weight: 600;
    color: var(--pc-text);
    white-space: nowrap;
  }

  &__sub {
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 11px;
    color: var(--pc-text-muted);
    white-space: nowrap;
  }

  &__caret {
    flex: 0 0 auto;
    color: var(--pc-text-muted);
    transition: transform 0.2s ease;

    &.is-open {
      color: var(--pc-primary);
      transform: rotate(180deg);
    }
  }

  &__menu {
    position: absolute;
    right: 8px;
    bottom: calc(100% - 6px);
    left: 8px;
    z-index: 5;
    padding: 10px;
    background: #fff;
    border: 1px solid var(--pc-border);
    border-radius: 12px;
    box-shadow: 0 12px 28px rgb(15 23 42 / 12%);
  }

  &__menu-header {
    display: flex;
    gap: 10px;
    align-items: center;
    padding: 4px 6px 10px;
    margin-bottom: 6px;
    border-bottom: 1px solid var(--pc-border-soft);
  }

  &__menu-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__menu-name {
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    font-weight: 600;
    color: var(--pc-text);
    white-space: nowrap;
  }

  &__menu-sub {
    font-size: 11px;
    color: var(--pc-text-muted);
  }

  &__menu-item {
    display: flex;
    gap: 8px;
    align-items: center;
    width: 100%;
    padding: 8px;
    font-family: inherit;
    font-size: 13px;
    color: var(--pc-text);
    cursor: pointer;
    background: transparent;
    border: none;
    border-radius: 8px;
    transition:
      background 0.15s ease,
      color 0.15s ease;

    svg {
      flex: 0 0 auto;
    }

    &:hover {
      color: var(--pc-primary);
      background: var(--pc-primary-soft);
    }

    &--danger {
      color: #b42318;

      &:hover {
        color: #b42318;
        background: #fef3f2;
      }
    }
  }
}

.user-menu-enter-active,
.user-menu-leave-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}

.user-menu-enter-from,
.user-menu-leave-to {
  opacity: 0;
  transform: translateY(4px);
}
</style>
