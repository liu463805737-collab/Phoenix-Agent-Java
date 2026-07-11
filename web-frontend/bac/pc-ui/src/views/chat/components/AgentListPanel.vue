<script setup lang="ts">
interface Agent {
  id: string;
  name: string;
  description: string;
  avatar: string;
  tag?: string;
}

interface Props {
  collapsed?: boolean;
}

withDefaults(defineProps<Props>(), {
  collapsed: false,
});

const activeAgentId = 's-data';

const agents: Agent[] = [
  {
    id: 's-data',
    name: '数据洞察官',
    description: '帮你做查询、透视和可视化分析',
    avatar: '数',
    tag: '常用',
  },
  {
    id: 's-doc',
    name: '文档协作助手',
    description: '总结、改写、翻译长文与会议纪要',
    avatar: '文',
  },
  {
    id: 's-code',
    name: '代码副驾',
    description: '代码生成、解释、Code Review',
    avatar: '码',
    tag: '新',
  },
  {
    id: 's-ops',
    name: '运维巡检员',
    description: '排障建议、告警归因、SOP 引导',
    avatar: '运',
  },
  {
    id: 's-write',
    name: '文案策划师',
    description: '营销文案、活动方案、品牌叙事',
    avatar: '创',
  },
  {
    id: 's-plan',
    name: '出行规划官',
    description: '行程编排、景点推荐、预算估算',
    avatar: '行',
  },
];
</script>

<template>
  <div class="agent-panel" :class="{ 'is-collapsed': collapsed }">
    <header v-if="!collapsed" class="agent-panel__header">
      <div class="agent-panel__title">智能体</div>
      <div class="agent-panel__desc">挑一个最懂你工作场景的助手</div>
    </header>

    <div class="agent-panel__list">
      <button
        v-for="agent in agents"
        :key="agent.id"
        type="button"
        class="agent-item"
        :class="{
          'is-active': agent.id === activeAgentId,
          'is-collapsed': collapsed,
        }"
        :title="collapsed ? agent.name : undefined"
      >
        <span class="agent-item__avatar">{{ agent.avatar }}</span>
        <span v-if="!collapsed" class="agent-item__body">
          <span class="agent-item__title-row">
            <span class="agent-item__name">{{ agent.name }}</span>
            <span v-if="agent.tag" class="agent-item__tag">{{
              agent.tag
            }}</span>
          </span>
          <span class="agent-item__desc">{{ agent.description }}</span>
        </span>
      </button>
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
    border-bottom: 1px solid var(--pc-border-soft);
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--pc-text);
  }

  &__desc {
    font-size: 12px;
    color: var(--pc-text-soft);
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
  color: var(--pc-text);
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

  &.is-collapsed {
    align-items: center;
    justify-content: center;
    width: 44px;
    padding: 6px;
  }

  &__avatar {
    display: flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    margin-top: 2px;
    font-size: 13px;
    font-weight: 600;
    color: var(--pc-primary);
    background: var(--pc-primary-soft);
    border-radius: 50%;
  }

  &.is-collapsed &__avatar {
    margin-top: 0;
  }

  &.is-active &__avatar {
    color: #fff;
    background: var(--pc-primary);
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
    color: var(--pc-text);
    white-space: nowrap;
  }

  &__tag {
    flex: 0 0 auto;
    height: 16px;
    padding: 0 6px;
    font-size: 11px;
    line-height: 16px;
    color: var(--pc-primary);
    background: #fff;
    border: 1px solid rgb(37 99 235 / 30%);
    border-radius: 8px;
  }

  &__desc {
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    font-size: 12px;
    line-height: 1.5;
    color: var(--pc-text-soft);
    -webkit-box-orient: vertical;
  }
}
</style>
