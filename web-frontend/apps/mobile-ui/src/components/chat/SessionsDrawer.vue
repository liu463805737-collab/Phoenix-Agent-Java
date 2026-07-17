<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import {
  useAgentStore,
  useAuthStore,
  useChatStore,
} from '@phoenix/chat-shared';
import {
  showConfirmDialog,
  showFailToast,
  showSuccessToast,
} from 'vant';

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
  const list = sessions.value
    .filter((s) =>
      !kw
        ? true
        : s.title.toLowerCase().includes(kw) ||
          s.preview.toLowerCase().includes(kw),
    )
    .sort((a, b) => {
      if (a.isPinned !== b.isPinned) return a.isPinned ? -1 : 1;
      return b.updatedAt - a.updatedAt;
    });
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

// --- Popover state ---
const popoverVisible = ref(false);
const popoverSessionId = ref('');
const popoverSessionTitle = ref('');
const popoverIsPinned = ref(false);
const popoverAnchorX = ref(0);
const popoverAnchorY = ref(0);

// --- Rename dialog ---
const renameDialogShow = ref(false);
const renameDialogText = ref('');

function handleRename() {
  popoverVisible.value = false;
  renameDialogText.value = popoverSessionTitle.value;
  renameDialogShow.value = true;
}

async function handleRenameConfirm() {
  const next = renameDialogText.value.trim();
  if (!next || next === popoverSessionTitle.value) {
    renameDialogShow.value = false;
    return;
  }
  try {
    await chat.renameSession(popoverSessionId.value, next);
    showSuccessToast('重命名成功');
  } catch {
    showFailToast('重命名失败');
  }
  renameDialogShow.value = false;
}

async function handleTogglePin() {
  popoverVisible.value = false;
  try {
    await chat.pinSession(popoverSessionId.value, !popoverIsPinned.value);
  } catch {
    showFailToast('操作失败');
  }
}

async function handleDelete() {
  popoverVisible.value = false;
  const id = popoverSessionId.value;
  try {
    await showConfirmDialog({
      title: '删除会话',
      message: '删除后无法恢复，确认删除？',
    });
    await chat.deleteSession(id);
    showSuccessToast('已删除');
  } catch {
    /* canceled or error */
  }
}

// --- Long-press ---
let pressTimer: number | null = null;
let pressEl: HTMLElement | null = null;
let pressId = '';
let pressTitle = '';
let pressIsPinned = false;

function startPress(id: string, title: string, isPinned: boolean, event: TouchEvent) {
  clearPress();
  const touch = event.touches[0];
  if (!touch) return;
  const touchX = touch.clientX;
  const touchY = touch.clientY;
  pressEl = event.currentTarget as HTMLElement | null;
  if (!pressEl) return;
  pressId = id;
  pressTitle = title;
  pressIsPinned = isPinned;
  pressTimer = window.setTimeout(() => {
    if (!pressEl) return;

    const POPOVER_W = 140;
    const POPOVER_H = 150;
    const GAP = 12;

    let ax = touchX;
    let ay = touchY + GAP;

    if (touchX + POPOVER_W > window.innerWidth) {
      ax = window.innerWidth - POPOVER_W;
    }
    if (touchY + GAP + POPOVER_H > window.innerHeight) {
      ay = touchY - POPOVER_H;
    }

    popoverAnchorX.value = ax;
    popoverAnchorY.value = ay;
    popoverSessionId.value = pressId;
    popoverSessionTitle.value = pressTitle;
    popoverIsPinned.value = pressIsPinned;
    popoverVisible.value = true;
  }, 450);
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
    :style="{ width: '70%', height: '100%' }"
    :overlay="true"
    @update:show="(v: boolean) => emit('update:show', v)"
  >
    <div class="drawer">
      <div class="drawer__top">
        <button type="button" class="drawer__new" @click="handleNew">
          <van-icon class="drawer__new-icon" name="plus" />
          <span>新建对话</span>
        </button>

        <label class="drawer__search">
          <van-icon name="search" />
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
            @touchstart.passive="startPress(item.id, item.title, !!item.isPinned, $event)"
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
        <van-icon name="ellipsis" />
      </button>
    </div>

    <teleport to="body">
      <div
        v-if="popoverVisible"
        class="popover-overlay"
        @click="popoverVisible = false"
        @touchmove.prevent
      >
        <div
          class="popover-menu"
          :style="{
            left: popoverAnchorX + 'px',
            top: popoverAnchorY + 'px',
          }"
          @click.stop
          @touchmove.stop
        >
          <button class="popover-menu__item" @click="handleRename">
            <van-icon name="edit" />
            重命名
          </button>
          <button class="popover-menu__item" @click="handleTogglePin">
            <van-icon :name="popoverIsPinned ? 'star-filled' : 'star-o'" />
            {{ popoverIsPinned ? '取消置顶' : '置顶' }}
          </button>
          <button class="popover-menu__item popover-menu__item--danger" @click="handleDelete">
            <van-icon name="delete" />
            删除
          </button>
        </div>
      </div>
    </teleport>

  </van-popup>

  <teleport to="body">
    <van-dialog
      v-model:show="renameDialogShow"
      title="重命名会话"
      show-cancel-button
      @confirm="handleRenameConfirm"
      @cancel="renameDialogText = ''"
    >
      <div style="padding: 16px">
        <van-field
          v-model="renameDialogText"
          placeholder="请输入会话名称"
          :maxlength="100"
          autofocus
        />
      </div>
    </van-dialog>
  </teleport>
</template>

<style lang="scss" scoped>
.drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-top: var(--m-safe-top);
  padding-bottom: var(--m-safe-bottom);
  background: var(--m-bg);
  user-select: none;
  -webkit-user-select: none;
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
  height: 36px;
  padding: 0 12px;
  font-size: 14px;
  color: var(--m-text-primary);
  cursor: pointer;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 10px;
  line-height: 1;
}

.drawer__new-icon {
  font-size: 16px;
  line-height: 1;
  color: var(--m-brand-primary);
  display: flex;
  align-items: center;
}

.drawer__search {
  position: relative;
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 12px 0 12px;
  color: var(--m-text-muted);
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 36px;
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
  user-select: none;
  -webkit-user-select: none;
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
  padding: 6px 6px;
  margin: 4px 12px 4px;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 12px;
  user-select: none;
  -webkit-user-select: none;
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

.popover-overlay {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 3000;
  width: 100%;
  height: 100%;
}

.popover-menu {
  position: fixed;
  z-index: 3001;
  min-width: 130px;
  padding: 6px;
  background: var(--m-bg-elevated, #fff);
  border: 1px solid var(--m-border, #e5e7eb);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  user-select: none;
  -webkit-user-select: none;
}

.popover-menu__item {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  color: var(--m-text-primary, #333);
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 8px;
}

.popover-menu__item:active {
  background: var(--m-bg-soft, #f3f4f6);
}

.popover-menu__item--danger {
  color: #b42318;
}


</style>
