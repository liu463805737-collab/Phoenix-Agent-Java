<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import {
  useAgentStore,
  useAuthStore,
  useChatStore,
} from '@phoenix/chat-shared';
import { showConfirmDialog } from 'vant';

import { useActionMenu } from '../useActionMenu';

interface Props {
  show: boolean;
}
const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:show', v: boolean): void;
  (e: 'select', id: string): void;
  (e: 'new-chat'): void;
  (e: 'goto-me'): void;
}>();

const chat = useChatStore();
const agentStore = useAgentStore();
const auth = useAuthStore();
const { sessions, activeSessionId } = storeToRefs(chat);
const { agents } = storeToRefs(agentStore);
const { user } = storeToRefs(auth);

const keyword = ref('');

watch(
  () => props.show,
  (v) => {
    if (!v) keyword.value = '';
  },
);

const displayName = computed(
  () => user.value?.displayName || user.value?.username || '未登录',
);
const avatarChar = computed(() => {
  const ch = [...displayName.value][0] ?? 'U';
  return ch.toUpperCase();
});

function getAgentName(id: string) {
  return agents.value.find((a) => a.id === id)?.name ?? '助手';
}

function formatGroupLabel(ts: number) {
  const d = new Date(ts);
  const now = new Date();
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate();
  if (sameDay) return '今天';
  const yest = new Date(now);
  yest.setDate(now.getDate() - 1);
  const isYest =
    d.getFullYear() === yest.getFullYear() &&
    d.getMonth() === yest.getMonth() &&
    d.getDate() === yest.getDate();
  if (isYest) return '昨天';
  const diffDays = Math.floor((now.getTime() - ts) / (24 * 3600 * 1000));
  if (diffDays <= 7) return '本周';
  if (diffDays <= 30) return '本月';
  return '更早';
}

const grouped = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  const list = sessions.value.filter((s) =>
    !kw
      ? true
      : s.title.toLowerCase().includes(kw) ||
        s.preview.toLowerCase().includes(kw),
  );
  const order = ['今天', '昨天', '本周', '本月', '更早'];
  const buckets = new Map<string, typeof list>();
  for (const s of list) {
    const k = formatGroupLabel(s.updatedAt);
    if (!buckets.has(k)) buckets.set(k, [] as typeof list);
    buckets.get(k)!.push(s);
  }
  return order
    .filter((k) => buckets.has(k))
    .map((k) => ({ label: k, items: buckets.get(k)! }));
});

const menu = useActionMenu();

async function handleLongPress(id: string, title: string) {
  const r = await menu.open([
    { name: '重命名' },
    { name: '删除', color: '#b42318' },
  ]);
  if (!r) return;
  if (r.name === '重命名') {
    const next = window.prompt('新的会话名称', title);
    if (next && next.trim() && next.trim() !== title) {
      await chat.renameSession(id, next.trim());
    }
  } else if (r.name === '删除') {
    try {
      await showConfirmDialog({
        title: '删除会话',
        message: '删除后无法恢复，确认删除？',
      });
      await chat.deleteSession(id);
    } catch {
      /* canceled */
    }
  }
}

let pressTimer: number | null = null;
function startPress(id: string, title: string) {
  clearPress();
  pressTimer = window.setTimeout(() => handleLongPress(id, title), 450);
}
function clearPress() {
  if (pressTimer != null) {
    clearTimeout(pressTimer);
    pressTimer = null;
  }
}

function handleSelect(id: string) {
  emit('select', id);
  emit('update:show', false);
}
function handleNew() {
  emit('new-chat');
  emit('update:show', false);
}
function handleMe() {
  emit('goto-me');
  emit('update:show', false);
}
</script>

<template>
  <van-popup
    :show="props.show"
    position="left"
    :style="{ width: '84%', height: '100%' }"
    :overlay="true"
    @update:show="(v: boolean) => emit('update:show', v)"
  >
    <div class="drawer">
      <div class="drawer__top">
        <button type="button" class="drawer__new" @click="handleNew">
          <span class="drawer__new-icon">+</span>
          新建对话
        </button>

        <label class="drawer__search">
          <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
            <circle
              cx="7"
              cy="7"
              r="4.5"
              fill="none"
              stroke="currentColor"
              stroke-width="1.4"
            />
            <path
              d="m10.5 10.5 3 3"
              stroke="currentColor"
              stroke-width="1.4"
              stroke-linecap="round"
            />
          </svg>
          <input
            v-model="keyword"
            class="drawer__search-input"
            type="search"
            placeholder="搜索"
          />
        </label>
      </div>

      <div class="drawer__list">
        <div v-if="grouped.length === 0" class="drawer__empty">
          没有匹配的会话
        </div>
        <div v-for="g in grouped" :key="g.label" class="drawer__group">
          <div class="drawer__group-label">{{ g.label }}</div>
          <button
            v-for="item in g.items"
            :key="item.id"
            type="button"
            class="drawer-item"
            :class="{ 'is-active': item.id === activeSessionId }"
            @click="handleSelect(item.id)"
            @touchstart.passive="startPress(item.id, item.title)"
            @touchend="clearPress"
            @touchcancel="clearPress"
            @touchmove="clearPress"
          >
            <div class="drawer-item__title">{{ item.title }}</div>
            <div class="drawer-item__meta">
              {{ getAgentName(item.agentId) }}
            </div>
          </button>
        </div>
      </div>

      <button type="button" class="drawer__user" @click="handleMe">
        <span class="drawer__user-avatar">{{ avatarChar }}</span>
        <span class="drawer__user-name">{{ displayName }}</span>
        <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
          <path
            d="m6 4 4 4-4 4"
            fill="none"
            stroke="currentColor"
            stroke-width="1.4"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>

    <van-action-sheet
      v-model:show="menu.state.show"
      :actions="menu.state.actions"
      :cancel-text="menu.state.cancelText"
      close-on-click-action
      @select="menu.onSelect"
      @cancel="menu.onCancel"
    />
  </van-popup>
</template>

<style lang="scss" scoped>
.drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-top: var(--m-safe-top);
  padding-bottom: var(--m-safe-bottom);
  background: var(--m-bg);
}

.drawer__top {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 14px 8px;
}

.drawer__new {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 40px;
  padding: 0 14px;
  font-size: 14px;
  color: var(--m-text-primary);
  cursor: pointer;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 10px;
}

.drawer__new-icon {
  font-size: 16px;
  line-height: 1;
  color: var(--m-brand-primary);
}

.drawer__search {
  position: relative;
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 12px 0 32px;
  color: var(--m-text-muted);
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 10px;
}

.drawer__search > svg {
  position: absolute;
  left: 10px;
}

.drawer__search-input {
  width: 100%;
  height: 100%;
  font-size: 14px;
  color: var(--m-text-primary);
  outline: none;
  background: transparent;
  border: none;
}

.drawer__search-input::placeholder {
  color: var(--m-text-muted);
}

.drawer__list {
  flex: 1 1 auto;
  min-height: 0;
  padding: 6px 8px 8px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.drawer__empty {
  padding: 48px 0;
  font-size: 13px;
  color: var(--m-text-muted);
  text-align: center;
}

.drawer__group + .drawer__group {
  margin-top: 12px;
}

.drawer__group-label {
  padding: 6px 10px;
  font-size: 11px;
  color: var(--m-text-muted);
  letter-spacing: 0.5px;
}

.drawer-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 10px;
  transition: background 0.15s ease;
}

.drawer-item:active,
.drawer-item.is-active {
  background: var(--m-brand-primary-soft);
}

.drawer-item.is-active .drawer-item__title {
  color: var(--m-brand-primary);
}

.drawer-item__title {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  font-weight: 500;
  color: var(--m-text-primary);
  white-space: nowrap;
}

.drawer-item__meta {
  font-size: 12px;
  color: var(--m-text-muted);
}

.drawer__user {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
  margin: 8px 12px 4px;
  cursor: pointer;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border-soft);
  border-radius: 12px;
}

.drawer__user-avatar {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #4079ff 0%, #2f6bff 60%, #1d4ed8 100%);
  border-radius: 50%;
}

.drawer__user-name {
  flex: 1 1 auto;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  color: var(--m-text-primary);
  text-align: left;
  white-space: nowrap;
}
</style>
