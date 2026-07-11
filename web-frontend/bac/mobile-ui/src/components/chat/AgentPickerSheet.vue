<script setup lang="ts">
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useAgentStore, useChatStore } from '@phoenix/chat-shared';

interface Props {
  show: boolean;
}
const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'update:show', v: boolean): void;
  (e: 'picked', agentId: string): void;
}>();

const agentStore = useAgentStore();
const chat = useChatStore();
const { agents } = storeToRefs(agentStore);
const { activeSession } = storeToRefs(chat);

const currentAgentId = computed(
  () => activeSession.value?.agentId ?? agentStore.activeAgentId,
);

const visible = computed(() => agents.value);

function pick(id: string) {
  emit('picked', id);
  emit('update:show', false);
}
</script>

<template>
  <van-popup
    :show="props.show"
    position="bottom"
    round
    :style="{ height: '72%' }"
    @update:show="(v: boolean) => emit('update:show', v)"
  >
    <div class="picker">
      <div class="picker__handle" />
      <div class="picker__title">选择智能体</div>

      <div class="picker__list">
        <button
          v-for="a in visible"
          :key="a.id"
          type="button"
          class="agent-row"
          :class="{ 'is-active': a.id === currentAgentId }"
          @click="pick(a.id)"
        >
          <span class="agent-row__avatar">{{ a.avatar }}</span>
          <span class="agent-row__body">
            <span class="agent-row__name">
              {{ a.name }}
              <span v-if="a.tag" class="agent-row__tag">{{ a.tag }}</span>
            </span>
            <span class="agent-row__desc">{{ a.description }}</span>
          </span>
          <span v-if="a.id === currentAgentId" class="agent-row__check">
            <svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true">
              <path
                d="m3.5 8.5 3 3 6-7"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </span>
        </button>
        <div v-if="visible.length === 0" class="picker__empty">暂无智能体</div>
      </div>
    </div>
  </van-popup>
</template>

<style lang="scss" scoped>
.picker {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-bottom: var(--m-safe-bottom);
  background: var(--m-bg-elevated);
}

.picker__handle {
  width: 36px;
  height: 4px;
  margin: 8px auto 4px;
  background: var(--m-border);
  border-radius: 2px;
}

.picker__title {
  padding: 6px 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--m-text-primary);
  text-align: center;
}

.picker__list {
  flex: 1 1 auto;
  min-height: 0;
  padding: 0 8px 12px;
  overflow-y: auto;
}

.picker__empty {
  padding: 48px 0;
  font-size: 13px;
  color: var(--m-text-muted);
  text-align: center;
}

.agent-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 12px;
  transition: background 0.15s ease;
}

.agent-row:active {
  background: var(--m-border-soft);
}

.agent-row.is-active {
  background: var(--m-brand-primary-soft);
}

.agent-row__avatar {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  font-size: 14px;
  font-weight: 600;
  color: var(--m-brand-primary);
  background: var(--m-brand-primary-soft);
  border-radius: 50%;
}

.agent-row.is-active .agent-row__avatar {
  color: #fff;
  background: var(--m-brand-primary);
}

.agent-row__body {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.agent-row__name {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: var(--m-text-primary);
}

.agent-row__tag {
  height: 16px;
  padding: 0 6px;
  font-size: 11px;
  line-height: 16px;
  color: var(--m-brand-primary);
  background: var(--m-bg-elevated);
  border: 1px solid rgb(47 107 255 / 30%);
  border-radius: 8px;
}

.agent-row__desc {
  font-size: 12px;
  line-height: 1.5;
  color: var(--m-text-soft);
}

.agent-row__check {
  flex: 0 0 auto;
  align-self: center;
  margin-left: 8px;
  color: var(--m-brand-primary);
}
</style>
