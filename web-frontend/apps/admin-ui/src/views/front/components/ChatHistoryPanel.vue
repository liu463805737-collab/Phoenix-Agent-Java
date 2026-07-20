<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';
import { useUserStore } from '@vben/stores';
import { ElMessage, ElMessageBox, ElTooltip, ElIcon } from 'element-plus';
import { Fold, More } from '@element-plus/icons-vue';


import { useAuthStore } from '#/store';
import { getSessionMessagesApi } from '#/api/front/chat';

import SystemSettingsModal from './SystemSettingsModal.vue';

const emit = defineEmits(['collapse'])

const settingsModalRef = ref<InstanceType<typeof SystemSettingsModal>>();

const chat = useChatStore();
const agentStore = useAgentStore();
const { sessions, activeSessionId, activeSession, loadingSessions } = storeToRefs(chat);
const { agents } = storeToRefs(agentStore);
const { userInfo } = storeToRefs(useUserStore());
const authStore = useAuthStore();

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
const currentAgentName = computed(
  () => currentAgent.value?.name ?? '未选择智能体',
);
const currentAgentDesc = computed(() => currentAgent.value?.description ?? '');

const avatarError = ref(false);

function isImageUrl(val: string) {
  return val.startsWith('/') || val.startsWith('http');
}

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
  let list = sessions.value.map((s) => ({
    id: s.id,
    title: s.title || '',
    preview: s.preview || '',
    isPinned: !!s.isPinned,
    updatedAt: formatUpdatedAt(
      ((s as any).updateTime ?? s.updatedAt ?? Date.now()) as number,
    ),
  }));
  if (keyword) {
    list = list.filter(
      (item) =>
        item.title.toLowerCase().includes(keyword) ||
        item.preview.toLowerCase().includes(keyword),
    );
  }
  list.sort((a, b) => {
    if (a.isPinned !== b.isPinned) return a.isPinned ? -1 : 1;
    return 0;
  });
  return list;
});

// 三点菜单
const activeMenuSessionId = ref<string | null>(null);
const editingSessionId = ref<string | null>(null);
const editingTitle = ref('');
const renameInputRef = ref<HTMLInputElement | null>(null);

function toggleMenu(id: string) {
  activeMenuSessionId.value = activeMenuSessionId.value === id ? null : id;
}

function startRename(id: string, currentTitle: string) {
  activeMenuSessionId.value = null;
  editingSessionId.value = id;
  editingTitle.value = currentTitle;
  nextTick(() => {
    renameInputRef.value?.focus();
    renameInputRef.value?.select();
  });
}

async function saveRename(id: string) {
  const title = editingTitle.value.trim();
  if (!title) {
    cancelRename();
    return;
  }
  try {
    await chat.renameSession(id, title);
    ElMessage.success('会话已重命名');
  } catch {
    ElMessage.error('重命名失败');
  }
  editingSessionId.value = null;
  editingTitle.value = '';
}

function cancelRename() {
  editingSessionId.value = null;
  editingTitle.value = '';
}

async function togglePin(id: string) {
  activeMenuSessionId.value = null;
  try {
    await chat.pinSession(id, !sessions.value.find((s) => s.id === id)?.isPinned);
  } catch {
    ElMessage.error('置顶操作失败');
  }
}

async function removeSession(id: string) {
  activeMenuSessionId.value = null;
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
      confirmButtonClass: 'el-button--danger',
    });
    await chat.deleteSession(id);
    ElMessage.success('会话已删除');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除会话失败');
    }
  }
}

async function handleSelect(id: string) {
  activeMenuSessionId.value = null;
  if (editingSessionId.value) {
    cancelRename();
  }
  await chat.switchSession(id);
  try {
    const apiMessages = await getSessionMessagesApi(id);
    const mapped = apiMessages.map((m) => ({
      id: String(m.id ?? `${Date.now()}-${Math.random()}`),
      role: (m.role === 'user' ? 'user' : 'assistant') as 'user' | 'assistant',
      content: m.content,
      createdAt: m.createTime ? new Date(m.createTime).getTime() : Date.now(),
      messageType: m.messageType ?? 'text',
      metadata: m.metadata,
    }));
    chat.messagesByS = { ...chat.messagesByS, [id]: mapped };
  } catch {
    // keep store default
  }
}

async function handleNewChat() {
  const agentId = currentAgent.value?.id ?? agents.value[0]?.id;
  if (!agentId) return;
  await chat.createSession(agentId);
}

const userName = computed(
  () => userInfo.value?.realName || userInfo.value?.username || '未登录用户',
);
const userAvatar = computed(() => {
  const name = userName.value;
  const ch = [...name][0] ?? 'U';
  return ch.toUpperCase();
});

const userMenuOpen = ref(false);
const userMenuRef = ref<HTMLElement | null>(null);

function toggleUserMenu() {
  userMenuOpen.value = !userMenuOpen.value;
}

function handleOutsideClick(event: MouseEvent) {
  if (userMenuOpen.value) {
    const root = userMenuRef.value;
    if (root && !root.contains(event.target as Node)) {
      userMenuOpen.value = false;
    }
  }
  if (activeMenuSessionId.value) {
    const target = event.target as HTMLElement;
    if (!target.closest('.history-item')) {
      activeMenuSessionId.value = null;
    }
  }
}

function handleEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    userMenuOpen.value = false;
    activeMenuSessionId.value = null;
  }
}

function handleSystemSettings() {
  userMenuOpen.value = false;
  settingsModalRef.value?.open();
}

async function handleLogout() {
  userMenuOpen.value = false;
  try {
    await ElMessageBox.confirm('确认退出登录？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    await authStore.logout(true);
  } catch {
    // cancelled
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleOutsideClick);
  document.addEventListener('keydown', handleEscape);
});

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleOutsideClick);
  document.removeEventListener('keydown', handleEscape);
});

function handleCloseChatHistoryPanel() {
  emit('collapse');
}

defineExpose({
  handleNewChat
})
</script>

<template>
  <div class="history-panel " >
    <header class="history-panel__header">
      <div class="history-panel__agent">
        <div class="history-panel__avatar">
          <img
            v-if="
              currentAgent?.avatar &&
              isImageUrl(currentAgent.avatar) &&
              !avatarError
            "
            :src="currentAgent.avatar"
            :alt="currentAgent.name"
            class="history-panel__avatar-img"
            @error="avatarError = true"
          />
          <span v-else>{{
            (currentAgent?.name && currentAgent.name.charAt(0)) || '·'
          }}</span>
        </div>
        <div class="history-panel__agent-meta">
          <div class="history-panel__agent-name flex justify-between">
            <div class="flex-1 flex items-center">
              {{ currentAgentName }}
            </div>
            <div class="history-panel__header__actions" >
              <el-tooltip content="收起历史面板" placement="bottom" :show-after="300">
                <button type="button" class="history-panel__agent-collapse" @click="handleCloseChatHistoryPanel">
                  <el-icon :size="16">
                    <Fold />
                  </el-icon>
                </button>
              </el-tooltip>
            </div>
          </div>
          <div class="history-panel__agent-sub">
            {{ currentAgentDesc || '当前智能体' }}
          </div>
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
      <div v-if="loadingSessions" class="history-panel__loading">
        <span class="history-panel__loading-spinner"></span>
        <span>加载中...</span>
      </div>
      <div
        v-for="item in filteredHistory"
        v-else
        :key="item.id"
        class="history-item"
        :class="{
          'is-active': item.id === activeSessionId,
          'is-pinned': item.isPinned,
        }"
        @click="handleSelect(item.id)"
      >
        <div class="history-item__body">
          <div class="history-item__title-row">
            <template v-if="editingSessionId === item.id">
              <input
                ref="renameInputRef"
                v-model="editingTitle"
                class="history-item__edit-input"
                @blur="saveRename(item.id)"
                @keyup.enter="saveRename(item.id)"
                @keyup.esc="cancelRename"
                @click.stop
              />
            </template>
            <template v-else>
              <el-tooltip
                v-if="item.title"
                :content="item.title"
                placement="top"
                :show-after="300"
              >
                <div class="history-item__title">{{ item.title }}</div>
              </el-tooltip>
            </template>
            <div class="history-item__time">{{ item.updatedAt }}</div>
          </div>
<!--          <div class="history-item__preview">{{ item.preview }}</div>-->
        </div>
        <div
          v-if="editingSessionId !== item.id"
          class="history-item__actions"
          @click.stop
        >
          <button
            type="button"
            class="history-item__dots-btn"
            @click="toggleMenu(item.id)"
          >
            <el-icon>
              <More />
            </el-icon>
<!--            <svg width="14" height="14" viewBox="0 0 14 14" aria-hidden="true">
              <circle cx="7" cy="3" r="1.2" fill="currentColor" />
              <circle cx="7" cy="7" r="1.2" fill="currentColor" />
              <circle cx="7" cy="11" r="1.2" fill="currentColor" />
            </svg>-->
          </button>
          <div
            v-if="activeMenuSessionId === item.id"
            class="history-item__menu"
          >
            <button
              type="button"
              class="history-item__menu-item"
              @click="startRename(item.id, item.title)"
            >
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              重命名
            </button>
            <button
              type="button"
              class="history-item__menu-item"
              @click="togglePin(item.id)"
            >
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.77 5.82 22 7 14.14 2 9.27l6.91-1.01z"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linejoin="round"
                />
              </svg>
              {{ item.isPinned ? '取消置顶' : '置顶' }}
            </button>
            <button
              type="button"
              class="history-item__menu-item history-item__menu-item--danger"
              @click="removeSession(item.id)"
            >
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2m3 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6h14"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>
      <div v-if="!loadingSessions && filteredHistory.length === 0" class="history-panel__empty">
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
          <span class="user-card__sub">当前登录</span>
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
              <span class="user-card__menu-sub">当前登录</span>
            </div>
          </div>
          <button
            type="button"
            class="user-card__menu-item"
            role="menuitem"
            @click="handleSystemSettings"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true">
              <circle
                cx="12"
                cy="12"
                r="3"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
              />
              <path
                d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <span>系统设置</span>
          </button>
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

    <SystemSettingsModal ref="settingsModalRef" />
  </div>
</template>

<style lang="scss" scoped>
.history-panel {
  display: flex;
  flex-direction: column;
  width: 300px;
  height: 100%;
  min-height: 0;

  &__header {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px 16px 12px;
    border-bottom: 1px solid hsl(var(--border));

    &__actions {
      display: flex;
      gap: 6px;
    }
  }

  &__agent {
    display: flex;
    gap: 10px;
    align-items: center;
    width: 100%;
  }

  &__avatar {
    display: flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
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

  &__agent-meta {
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__agent-name {
    font-size: 14px;
    font-weight: 600;
    color: hsl(var(--foreground));
  }

  &__agent-sub {
    font-size: 12px;
    color: hsl(var(--muted-foreground));
  }

  &__new-chat {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    justify-content: center;
    height: 32px;
    padding: 0 14px;
    font-size: 13px;
    color: hsl(var(--foreground));
    cursor: pointer;
    background: hsl(var(--background));
    border: 1px solid hsl(var(--border));
    border-radius: 6px;
    transition:
      background 0.15s ease,
      border-color 0.15s ease,
      color 0.15s ease;

    &:hover {
      color: hsl(var(--primary));
      background: hsl(var(--primary) / 12%);
      border-color: hsl(var(--primary));
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
    color: hsl(var(--muted-foreground));
    text-align: left;
    letter-spacing: 0.4px;
    cursor: pointer;
    background: transparent;
    border: none;
    transition: color 0.15s ease;

    &:hover {
      color: hsl(var(--foreground));
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
    color: hsl(var(--muted-foreground));
    pointer-events: none;
  }

  &__search-input {
    width: 100%;
    height: 30px;
    padding: 0 10px 0 28px;
    font-family: inherit;
    font-size: 12px;
    color: hsl(var(--foreground));
    outline: none;
    background: hsl(var(--background));
    border: 1px solid hsl(var(--border));
    border-radius: 6px;
    transition:
      border-color 0.15s ease,
      box-shadow 0.15s ease;

    &::placeholder {
      color: hsl(var(--muted-foreground));
    }

    &:focus {
      border-color: hsl(var(--primary));
      box-shadow: 0 0 0 3px hsl(var(--primary) / 12%);
    }
  }

  &__empty {
    padding: 16px 12px;
    font-size: 12px;
    color: hsl(var(--muted-foreground));
    text-align: center;
  }

  &__loading {
    display: flex;
    gap: 8px;
    align-items: center;
    justify-content: center;
    padding: 24px 12px;
    font-size: 13px;
    color: hsl(var(--muted-foreground));

    &-spinner {
      width: 16px;
      height: 16px;
      border: 2px solid hsl(var(--border));
      border-top-color: hsl(var(--primary));
      border-radius: 50%;
      animation: history-spin 0.6s linear infinite;
    }
  }

  @keyframes history-spin {
    to { transform: rotate(360deg); }
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
  position: relative;
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
    background: hsl(var(--accent));
    border-color: hsl(var(--border));
  }

  &.is-active {
    background: hsl(var(--primary) / 12%);
    border-color: hsl(var(--primary) / 25%);
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
    color: hsl(var(--foreground));
    white-space: nowrap;
  }

  &__time {
    flex: 0 0 auto;
    font-size: 11px;
    color: hsl(var(--muted-foreground));
  }

  &__preview {
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 1;
    font-size: 12px;
    line-height: 1.55;
    color: hsl(var(--muted-foreground));
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
  border-top: 1px solid hsl(var(--border));

  &__trigger {
    display: flex;
    flex: 1 1 auto;
    gap: 10px;
    align-items: center;
    min-width: 0;
    padding: 8px 10px;
    font-family: inherit;
    color: hsl(var(--foreground));
    cursor: pointer;
    background: transparent;
    border: 1px solid transparent;
    border-radius: 10px;
    transition:
      background 0.15s ease,
      border-color 0.15s ease,
      box-shadow 0.15s ease;

    &:hover {
      background: hsl(var(--accent));
    }
  }

  &.is-open &__trigger {
    background: hsl(var(--accent));
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
    color: hsl(var(--primary-foreground));
    background: hsl(var(--primary));
    border-radius: 50%;

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
    color: hsl(var(--foreground));
    white-space: nowrap;
  }

  &__sub {
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 11px;
    color: hsl(var(--muted-foreground));
    white-space: nowrap;
  }

  &__caret {
    flex: 0 0 auto;
    color: hsl(var(--muted-foreground));
    transition: transform 0.2s ease;

    &.is-open {
      color: hsl(var(--primary));
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
    background: hsl(var(--card));
    border: 1px solid hsl(var(--border));
    border-radius: 12px;
    box-shadow: 0 12px 28px hsl(var(--foreground) / 12%);
  }

  &__menu-header {
    display: flex;
    gap: 10px;
    align-items: center;
    padding: 4px 6px 10px;
    margin-bottom: 6px;
    border-bottom: 1px solid hsl(var(--border));
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
    color: hsl(var(--foreground));
    white-space: nowrap;
  }

  &__menu-sub {
    font-size: 11px;
    color: hsl(var(--muted-foreground));
  }

  &__menu-item {
    display: flex;
    gap: 8px;
    align-items: center;
    width: 100%;
    padding: 8px;
    font-family: inherit;
    font-size: 13px;
    color: hsl(var(--foreground));
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
      color: hsl(var(--primary));
      background: hsl(var(--primary) / 12%);
    }

    &--danger {
      color: hsl(var(--destructive));

      &:hover {
        color: hsl(var(--destructive));
        background: hsl(var(--destructive) / 10%);
      }
    }
  }
}

.history-item.is-pinned {
  border-left: 3px solid hsl(var(--primary));
}

.history-item__actions {
  position: absolute;
  top: 4px;
  right: 4px;
  z-index: 2;
  display: none;
}

.history-item:hover .history-item__actions {
  display: flex;
}

.history-item__dots-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  color: hsl(var(--primary) / 90%);
  cursor: pointer;
  background: hsl(var(--background) / 80%);
  border: 1px solid transparent;
  border-radius: 9999px;
  transition: all 0.15s ease;
}

.history-item__dots-btn:hover {
  color: hsl(var(--primary));
  border-color: hsl(var(--primary) / 90%);
}

.history-item__menu {
  position: absolute;
  top: 28px;
  right: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 130px;
  padding: 6px;
  background: hsl(var(--card));
  border: 1px solid hsl(var(--border));
  border-radius: 10px;
  box-shadow: 0 8px 20px hsl(var(--foreground) / 12%);
}

.history-item__menu-item {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
  padding: 7px 10px;
  font-family: inherit;
  font-size: 13px;
  color: hsl(var(--foreground));
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 6px;
  transition:
    background 0.15s ease,
    color 0.15s ease;

  svg {
    flex: 0 0 auto;
  }

  &:hover {
    color: hsl(var(--primary));
    background: hsl(var(--primary) / 12%);
  }

  &--danger {
    color: hsl(var(--destructive));

    &:hover {
      color: hsl(var(--destructive));
      background: hsl(var(--destructive) / 10%);
    }
  }
}

.history-item__edit-input {
  flex: 1 1 auto;
  min-width: 0;
  height: 24px;
  padding: 0 6px;
  font-family: inherit;
  font-size: 13px;
  color: hsl(var(--foreground));
  outline: none;
  background: hsl(var(--background));
  border: 1px solid hsl(var(--primary));
  border-radius: 4px;
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

.history-panel__agent-collapse {
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
</style>
