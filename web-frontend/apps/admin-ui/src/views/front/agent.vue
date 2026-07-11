<script setup lang="ts">
import type { Agent } from '#/api/front/agent';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { getAgentListApi } from '#/api/front/agent';

const router = useRouter();
const loading = ref(true);
const searchKeyword = ref('');
const agents = ref<Agent[]>([]);

const filteredAgents = computed(() => {
  let list = agents.value ?? [];
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase();
    list = list.filter(
      (a) =>
        a.name?.toLowerCase().includes(kw) ||
        a.description?.toLowerCase().includes(kw),
    );
  }
  return list;
});

onMounted(async () => {
  try {
    agents.value = await getAgentListApi('published');
  } finally {
    loading.value = false;
  }
});

function goToChat(agent: Agent) {
  if (agent.id) {
    router.push({ name: 'Chat', query: { agentId: agent.id } });
  }
}
</script>

<template>
  <div class="max-w-[960px] mx-auto px-6 py-12 text-foreground">
    <header class="mb-8 text-center">
      <h1 class="m-0 mb-2 text-[28px] font-bold text-foreground">智能体</h1>
      <p class="m-0 text-sm text-muted-foreground">选择一个智能体开始对话</p>
    </header>

    <div class="relative max-w-[480px] mx-auto mb-8">
      <svg
        class="absolute top-1/2 left-[14px] w-5 h-5 text-muted-foreground -translate-y-1/2"
        viewBox="0 0 24 24"
        aria-hidden="true"
      >
        <path
          d="M15.5 14h-.79l-.28-.27a6.5 6.5 0 0 0 1.48-5.34c-.47-2.78-2.79-5-5.59-5.34a6.5 6.5 0 0 0-7.27 7.27c.34 2.8 2.56 5.12 5.34 5.59a6.5 6.5 0 0 0 5.34-1.48l.27.28v.79l4.25 4.25c.41.41 1.08.41 1.49 0 .41-.41.41-1.08 0-1.49zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14"
          fill="currentColor"
        />
      </svg>
      <input
        v-model="searchKeyword"
        class="w-full py-3 pr-4 pl-[44px] text-sm outline-none bg-background border border-border rounded-xl transition-colors placeholder:text-muted-foreground focus:border-primary focus:ring-4 focus:ring-primary/10"
        placeholder="搜索智能体名称或描述..."
        type="text"
      />
    </div>

    <div v-if="loading" class="flex flex-col items-center justify-center py-20 text-sm text-muted-foreground">
      <div class="w-8 h-8 mb-3 border-2 border-border border-t-primary rounded-full animate-spin" />
      <span>加载中...</span>
    </div>

    <div v-else-if="filteredAgents.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
      <div class="mb-4 text-5xl">🤖</div>
      <p class="m-0 text-sm">{{ searchKeyword ? '没有匹配的智能体' : '暂无可用的智能体' }}</p>
    </div>

    <div v-else class="grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-4">
      <div
        v-for="agent in filteredAgents"
        :key="agent.id"
        class="flex gap-4 items-center p-5 cursor-pointer bg-card border border-border rounded-[14px] transition-all duration-200 hover:border-primary hover:shadow-[0_8px_24px_hsl(var(--primary)_/_12%)] hover:-translate-y-0.5"
        @click="goToChat(agent)"
      >
        <div class="flex shrink-0 items-center justify-center w-12 h-12 text-lg font-semibold text-foreground bg-accent rounded-xl">
          {{ agent.avatar || agent.name?.charAt(0) || 'A' }}
        </div>
        <div class="min-w-0">
          <div class="mb-1 text-[15px] font-semibold text-foreground">{{ agent.name }}</div>
          <div class="truncate text-[13px] text-muted-foreground">{{ agent.description || '暂无描述' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

