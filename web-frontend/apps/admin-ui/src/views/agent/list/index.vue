<script lang="ts" setup>
import type { Agent } from '#/api/core/agent';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  ElAvatar,
  ElButton,
  ElCard,
  ElCol,
  ElEmpty,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElRadioButton,
  ElRadioGroup,
  ElRow,
  ElSkeleton,
  ElTag,
} from 'element-plus';

import {
  deleteAgentApi,
  getAgentListApi,
  offlineAgentApi,
  publishAgentApi,
} from '#/api';

import AgentFormDrawer from './agent-create-drawer.vue';

const router = useRouter();

const loading = ref(true);
const activeFilter = ref('all');
const searchKeyword = ref('');
const agents = ref<Agent[]>([]);

function goToRunPage(agent: Agent) {
  if (agent.id) {
    router.push(`/agent/${agent.id}/run`);
  }
}

const publishedCount = computed(
  () => agents.value.filter((a) => a.status === 'published').length,
);
const draftCount = computed(
  () =>
    agents.value.filter(
      (a) => a.status === 'draft' || a.status === 'draft-pending',
    ).length,
);
const offlineCount = computed(
  () => agents.value.filter((a) => a.status === 'offline').length,
);

const filteredAgents = computed(() => {
  let filtered = (agents.value ?? []).filter((a): a is Agent => a != null);
  if (activeFilter.value !== 'all') {
    filtered = filtered.filter((agent) => {
      if (activeFilter.value === 'draft') {
        return agent.status === 'draft' || agent.status === 'draft-pending';
      }
      return agent.status === activeFilter.value;
    });
  }
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(
      (agent) =>
        agent.name?.toLowerCase().includes(keyword) ||
        agent.description?.toLowerCase().includes(keyword) ||
        agent.id?.toString().includes(keyword),
    );
  }
  return filtered;
});

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: AgentFormDrawer,
  destroyOnClose: true,
});

async function loadAgents() {
  loading.value = true;
  try {
    agents.value = (await getAgentListApi()).filter((a): a is Agent => a != null);
  } catch {
    ElMessage.error('获取智能体列表失败');
    agents.value = [];
  } finally {
    loading.value = false;
  }
}

function goToCreateAgent() {
  formDrawerApi.setData({}).open();
}

function openEditDrawer(agent: Agent) {
  formDrawerApi.setData(agent).open();
}

function getStatusText(status?: string) {
  const map: Record<string, string> = {
    published: '已发布',
    draft: '草稿',
    'draft-pending': '草稿',
    offline: '已下线',
  };
  return status ? map[status] || status : '';
}

function getTypeText(type?: string) {
  const map: Record<string, string> = {
    sql: '数据智能体',
    agent: '智能体',
    workflow: '流程智能体',
  };
  return type ? map[type] || type : '';
}

function getStatusTagType(status?: string) {
  const map: Record<string, 'info' | 'success' | 'warning'> = {
    published: 'success',
    draft: 'warning',
    'draft-pending': 'warning',
    offline: 'info',
  };
  return status ? (map[status] ?? 'info') : undefined;
}

async function handleDelete(agent: Agent) {
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体 "${agent.name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    const id = agent.id;
    if (!id) return;
    await deleteAgentApi(id);
    ElMessage.success('智能体删除成功');
    agents.value = agents.value.filter((a) => a.id !== agent.id);
  } catch {
    // cancelled
  }
}

async function handlePublish(agent: Agent) {
  try {
    await ElMessageBox.confirm(
      `确定要发布智能体 "${agent.name}" 吗？`,
      '发布确认',
      {
        confirmButtonText: '确定发布',
        cancelButtonText: '取消',
        type: 'info',
      },
    );
    const id = agent.id;
    if (!id) return;
    await publishAgentApi(id);
    ElMessage.success('智能体发布成功');
    await loadAgents();
  } catch {
    // cancelled
  }
}

async function handleOffline(agent: Agent) {
  try {
    await ElMessageBox.confirm(
      `确定要下线智能体 "${agent.name}" 吗？下线后将无法在外部使用。`,
      '下线确认',
      {
        confirmButtonText: '确定下线',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    const id = agent.id;
    if (!id) return;
    await offlineAgentApi(id);
    ElMessage.success('智能体已下线');
    await loadAgents();
  } catch {
    // cancelled
  }
}

function formatTime(time?: string) {
  if (!time) return '';
  return time.replace('T', ' ').slice(0, 16);
}

onMounted(loadAgents);
</script>

<template>
  <Page>
    <!-- 内容头部 -->
    <div class="mb-4 flex items-center justify-between px-1">
      <div>
        <h1 class="text-lg font-semibold">智能体管理中心</h1>
        <p class="text-sm text-muted-foreground">创建和管理您的AI智能体，让数据分析更智能</p>
      </div>
      <div class="flex items-center gap-8">
          <div class="text-center">
            <div class="text-2xl font-bold leading-none text-primary">{{ agents.length }}</div>
            <div class="mt-0.5 text-xs text-muted-foreground">总数量</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold leading-none text-success">{{ publishedCount }}</div>
            <div class="mt-0.5 text-xs text-muted-foreground">已发布</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold leading-none text-warning">{{ draftCount }}</div>
            <div class="mt-0.5 text-xs text-muted-foreground">草稿</div>
          </div>
          <div class="text-center">
            <div class="text-2xl font-bold leading-none text-muted-foreground">{{ offlineCount }}</div>
            <div class="mt-0.5 text-xs text-muted-foreground">已下线</div>
          </div>
        </div>
    </div>

    <!-- 过滤和搜索区域 -->
    <ElCard class="mb-4">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <ElRadioGroup v-model="activeFilter" size="large">
          <ElRadioButton value="all">
            <div class="flex items-center">
              <ElIcon><IconifyIcon icon="lucide:grid-3x3" /></ElIcon>
              <span>全部智能体</span>
              <span class="tab-count">{{ agents.length }}</span>
            </div>
          </ElRadioButton>
          <ElRadioButton value="published">
            <div class="flex items-center">
              <ElIcon><IconifyIcon icon="lucide:check" /></ElIcon>
              <span>已发布</span>
              <span class="tab-count">{{ publishedCount }}</span>
            </div>
          </ElRadioButton>
          <ElRadioButton value="draft">
            <div class="flex items-center">
              <ElIcon><IconifyIcon icon="lucide:pencil" /></ElIcon>
              <span>草稿</span>
              <span class="tab-count">{{ draftCount }}</span>
            </div>
          </ElRadioButton>
          <ElRadioButton value="offline">
            <div class="flex items-center">
              <ElIcon><IconifyIcon icon="lucide:square" /></ElIcon>
              <span>已下线</span>
              <span class="tab-count">{{ offlineCount }}</span>
            </div>
          </ElRadioButton>
        </ElRadioGroup>
        <div class="flex items-center gap-3">
          <ElInput
            v-model="searchKeyword"
            placeholder="搜索智能体名称、ID或描述..."
            size="large"
            clearable
            style="width: 350px; max-width: 100%"
          >
            <template #prefix>
              <ElIcon><IconifyIcon icon="lucide:search" /></ElIcon>
            </template>
          </ElInput>
          <ElButton @click="loadAgents" size="large">
            <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
            刷新
          </ElButton>
          <ElButton type="primary" @click="goToCreateAgent" size="large">
            <ElIcon><IconifyIcon icon="lucide:plus" /></ElIcon>
            创建智能体
          </ElButton>
        </div>
      </div>
    </ElCard>

    <!-- 智能体网格 -->
    <div class="agents-grid" v-if="!loading">
      <ElRow :gutter="20">
        <ElCol
          v-for="agent in filteredAgents"
          :key="agent.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <ElCard
            class="agent-card"
            shadow="hover"
            :body-style="{ padding: '20px' }"
            @click="openEditDrawer(agent)"
          >
            <div class="agent-content">
              <div class="delete-button" @click.stop="handleDelete(agent)">
                <ElIcon><IconifyIcon icon="lucide:trash-2" /></ElIcon>
              </div>

              <div class="agent-avatar">
                <ElAvatar :size="48" style="font-size:20px;font-weight:600;color:#fff;background:#2f6bff">
                  {{ agent.name?.charAt(0) || 'A' }}
                </ElAvatar>
              </div>

              <div class="agent-info">
                <h3 class="agent-name">{{ agent.name }}</h3>
                <div class="agent-meta">
                  <span class="agent-id">ID: {{ agent.id }}</span>
                  <ElTag type="info" size="small" effect="plain">
                    {{ getTypeText(agent.type) }}
                  </ElTag>
                  <span class="agent-time">{{
                    formatTime(agent.updateTime)
                  }}</span>
                </div>
              </div>
              <div class="agent-status">
                <ElTag
                  :type="getStatusTagType(agent.status)"
                  size="small"
                  effect="light"
                >
                  {{ getStatusText(agent.status) }}
                </ElTag>
              </div>
              <div class="agent-actions">
                <ElButton size="small" type="primary" round @click.stop="goToRunPage(agent)">
                  <ElIcon><IconifyIcon icon="lucide:play" /></ElIcon>
                  运行
                </ElButton>
                <ElButton size="small" round @click.stop="openEditDrawer(agent)">
                  <ElIcon><IconifyIcon icon="lucide:settings" /></ElIcon>
                  编辑
                </ElButton>
                <ElButton
                  v-if="agent.status === 'draft' || agent.status === 'draft-pending' || agent.status === 'offline'"
                  size="small"
                  type="success"
                  round
                  @click.stop="handlePublish(agent)"
                >
                  <ElIcon><IconifyIcon icon="lucide:upload" /></ElIcon>
                  发布
                </ElButton>
                <ElButton
                  v-if="agent.status === 'published'"
                  size="small"
                  type="warning"
                  round
                  @click.stop="handleOffline(agent)"
                >
                  <ElIcon><IconifyIcon icon="lucide:download" /></ElIcon>
                  下线
                </ElButton>
              </div>
            </div>
          </ElCard>
        </ElCol>
      </ElRow>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <ElSkeleton :rows="6" animated />
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredAgents.length === 0" class="empty-state">
      <ElEmpty description="暂无智能体">
        <ElButton type="primary" @click="goToCreateAgent">
          <ElIcon><IconifyIcon icon="lucide:plus" /></ElIcon>
          创建智能体
        </ElButton>
      </ElEmpty>
    </div>

    <FormDrawer @success="loadAgents" />

  </Page>
</template>

<style scoped>
.tab-count {
  padding: 0.15rem 0.4rem;
  margin-left: 0.3rem;
  font-size: 0.7rem;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  border-radius: 4px;
}

.agents-grid {
  margin-bottom: 1.5rem;
}

.agent-card {
  margin-bottom: 1rem;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.agent-card:hover {
  box-shadow: 0 4px 12px rgb(0 0 0 / 15%);
  transform: translateY(-2px);
}

.agent-content {
  position: relative;
}

.agent-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.agent-info {
  margin-bottom: 1rem;
  text-align: center;
}

.agent-name {
  margin: 0 0 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
}

.agent-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.75rem;
  color: #9ca3af;
}

.agent-status {
  position: absolute;
  top: 0;
  right: 0;
}

.agent-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  margin-top: 0.75rem;
}

.delete-button {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  color: white;
  cursor: pointer;
  background: rgb(239 68 68 / 90%);
  border-radius: 50%;
  opacity: 0;
  transition: all 0.2s ease;
}

.delete-button:hover {
  background: rgb(220 38 38 / 90%);
  transform: scale(1.1);
}

.agent-card:hover .delete-button {
  opacity: 1;
}

.loading-state {
  padding: 3rem 1.5rem;
}

.empty-state {
  padding: 3rem 1.5rem;
}
</style>
