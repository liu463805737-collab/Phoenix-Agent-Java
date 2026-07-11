<script setup lang="ts">
import { ref } from 'vue';

import AgentListPanel from './components/AgentListPanel.vue';
import ChatHistoryPanel from './components/ChatHistoryPanel.vue';
import ChatMessages from './components/ChatMessages.vue';
import ChatComposer from './components/ChatComposer.vue';

const agentPanelCollapsed = ref(false);

function toggleAgentPanel() {
  agentPanelCollapsed.value = !agentPanelCollapsed.value;
}
</script>

<template>
  <div class="chat-layout">
    <aside class="chat-layout__history">
      <ChatHistoryPanel />
    </aside>

    <section class="chat-layout__main">
      <div class="chat-layout__messages">
        <ChatMessages />
      </div>
      <div class="chat-layout__composer">
        <ChatComposer />
      </div>

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
    </section>

    <aside
      class="chat-layout__agents"
      :class="{ 'is-collapsed': agentPanelCollapsed }"
    >
      <AgentListPanel :collapsed="agentPanelCollapsed" />
    </aside>
  </div>
</template>

<style lang="scss" scoped>
.chat-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr) 300px;
  gap: 12px;
  height: 100vh;
  padding: 12px;
  background: var(--pc-bg);

  &__history,
  &__agents {
    min-height: 0;
    overflow: hidden;
    background: var(--pc-panel);
    border: 1px solid var(--pc-border);
    border-radius: 12px;
    box-shadow: var(--pc-shadow);
  }

  &__main {
    position: relative;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    background: var(--pc-panel);
    border: 1px solid var(--pc-border);
    border-radius: 12px;
    box-shadow: var(--pc-shadow);
  }

  &__messages {
    flex: 1 1 auto;
    min-height: 0;
    overflow: hidden;
  }

  &__composer {
    flex: 0 0 auto;
    background: #fbfbfd;
    border-top: 1px solid var(--pc-border-soft);
  }

  &__toggle {
    position: absolute;
    top: 50%;
    right: -1px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 48px;
    padding: 0;
    color: var(--pc-text-soft);
    cursor: pointer;
    background: #fff;
    border: 1px solid var(--pc-border);
    border-right: none;
    border-radius: 6px 0 0 6px;
    transform: translateY(-50%);
    transition: background 0.15s ease;

    &:hover {
      color: var(--pc-primary);
      background: var(--pc-primary-soft);
    }
  }

  &__toggle-icon {
    font-size: 14px;
    line-height: 1;
  }

  &__agents {
    transition: width 0.2s ease;
  }

  &:has(.chat-layout__agents.is-collapsed) {
    grid-template-columns: 260px minmax(0, 1fr) 64px;
  }
}
</style>
