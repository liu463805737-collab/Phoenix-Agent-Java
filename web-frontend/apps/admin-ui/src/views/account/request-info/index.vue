<script lang="ts" setup>
import { ref, onMounted } from 'vue';

import { Page } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';

import {
  ElButton,
  ElCard,
  ElDialog,
  ElIcon,
  ElMessageBox,
  ElPagination,
  ElSegmented,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  getAgentRequestPageApi,
  getAgentRequestAgentStatsApi,
  getAgentRequestUserStatsApi,
} from '#/api/core/platform-agent-request';

import type { ChatSessionRequestVO, ChatSessionAgentStatVO, ChatSessionUserStatVO } from '#/api/core/platform-agent-request';

import { useSearchFormSchema, useColumns, useAgentStatColumns, useUserStatColumns } from './data';

const loading = ref(false);
const tableData = ref<ChatSessionRequestVO[]>([]);
const agentStatData = ref<ChatSessionAgentStatVO[]>([]);
const userStatData = ref<ChatSessionUserStatVO[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const currentView = ref<'all' | 'agentStat' | 'userStat'>('all');
const contentDialogVisible = ref(false);
const currentContent = ref('');
const agentDetailDialogVisible = ref(false);
const agentDetailData = ref<ChatSessionRequestVO[]>([]);
const agentDetailLoading = ref(false);
const agentDetailTotal = ref(0);
const agentDetailPageNum = ref(1);
const agentDetailPageSize = ref(10);
const currentAgentId = ref<number | null>(null);

const userDetailDialogVisible = ref(false);
const userDetailData = ref<ChatSessionRequestVO[]>([]);
const userDetailLoading = ref(false);
const userDetailTotal = ref(0);
const userDetailPageNum = ref(1);
const userDetailPageSize = ref(10);
const currentUserDetailId = ref<string | null>(null);

const [FilterForm] = useVbenForm({
  commonConfig: { componentProps: { clearable: true } },
  layout: 'inline',
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  submitButtonOptions: { content: '查询' },
  resetButtonOptions: { plain: true },
  schema: useSearchFormSchema(),
  handleSubmit: (values) => {
    pageNum.value = 1;
    if (currentView.value === 'all') {
      loadRequestPage(values);
    } else if (currentView.value === 'agentStat') {
      loadAgentStats(values);
    } else {
      loadUserStats(values);
    }
  },
  handleReset: () => {
    pageNum.value = 1;
    if (currentView.value === 'all') {
      loadRequestPage({});
    } else if (currentView.value === 'agentStat') {
      loadAgentStats({});
    } else {
      loadUserStats({});
    }
  },
});

async function loadRequestPage(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getAgentRequestPageApi({
      ...params,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    })) as any;
    if (Array.isArray(res)) {
      tableData.value = res;
      total.value = 0;
    } else if (res?.records) {
      tableData.value = res.records;
      total.value = res.totalRow || 0;
    } else {
      tableData.value = [];
      total.value = 0;
    }
  } catch {
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

async function loadAgentStats(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getAgentRequestAgentStatsApi({ ...params })) as any;
    agentStatData.value = Array.isArray(res) ? res : [];
  } catch {
    agentStatData.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadUserStats(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getAgentRequestUserStatsApi({ ...params })) as any;
    userStatData.value = Array.isArray(res) ? res : [];
  } catch {
    userStatData.value = [];
  } finally {
    loading.value = false;
  }
}

function handleViewChange(val: 'all' | 'agentStat' | 'userStat') {
  currentView.value = val;
  pageNum.value = 1;
  if (val === 'agentStat') {
    loadAgentStats({});
  } else if (val === 'userStat') {
    loadUserStats({});
  }
}

function handlePageChange(val: number) {
  pageNum.value = val;
  loadRequestPage();
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  pageNum.value = 1;
  loadRequestPage();
}

async function loadAgentDetail(agentId: number) {
  currentAgentId.value = agentId;
  agentDetailPageNum.value = 1;
  agentDetailLoading.value = true;
  try {
    const res = (await getAgentRequestPageApi({
      agentId,
      pageNum: agentDetailPageNum.value,
      pageSize: agentDetailPageSize.value,
    })) as any;
    if (Array.isArray(res)) {
      agentDetailData.value = res;
      agentDetailTotal.value = 0;
    } else if (res?.records) {
      agentDetailData.value = res.records;
      agentDetailTotal.value = res.totalRow || 0;
    } else {
      agentDetailData.value = [];
      agentDetailTotal.value = 0;
    }
  } catch {
    agentDetailData.value = [];
    agentDetailTotal.value = 0;
  } finally {
    agentDetailLoading.value = false;
  }
}

function handleAgentDetailPageChange(val: number) {
  agentDetailPageNum.value = val;
  if (currentAgentId.value) {
    loadAgentDetail(currentAgentId.value);
  }
}

function handleAgentDetailSizeChange(val: number) {
  agentDetailPageSize.value = val;
  agentDetailPageNum.value = 1;
  if (currentAgentId.value) {
    loadAgentDetail(currentAgentId.value);
  }
}

async function loadUserDetailRequests(userId: string) {
  currentUserDetailId.value = userId;
  userDetailPageNum.value = 1;
  userDetailLoading.value = true;
  try {
    const res = (await getAgentRequestPageApi({
      userId,
      pageNum: userDetailPageNum.value,
      pageSize: userDetailPageSize.value,
    })) as any;
    if (Array.isArray(res)) {
      userDetailData.value = res;
      userDetailTotal.value = 0;
    } else if (res?.records) {
      userDetailData.value = res.records;
      userDetailTotal.value = res.totalRow || 0;
    } else {
      userDetailData.value = [];
      userDetailTotal.value = 0;
    }
  } catch {
    userDetailData.value = [];
    userDetailTotal.value = 0;
  } finally {
    userDetailLoading.value = false;
  }
}

function handleUserDetailPageChange(val: number) {
  userDetailPageNum.value = val;
  if (currentUserDetailId.value) {
    loadUserDetailRequests(currentUserDetailId.value);
  }
}

function handleUserDetailSizeChange(val: number) {
  userDetailPageSize.value = val;
  userDetailPageNum.value = 1;
  if (currentUserDetailId.value) {
    loadUserDetailRequests(currentUserDetailId.value);
  }
}

function viewUserRequests(userId: string) {
  currentView.value = 'all';
  pageNum.value = 1;
  loadRequestPage({ userId, pageNum: 1, pageSize: 10 });
}

const columns = useColumns();
const agentStatCols = useAgentStatColumns();
const userStatCols = useUserStatColumns();

import { marked } from 'marked';
import DOMPurify from 'dompurify';

function openContentDialog(content: string) {
  const html = marked.parse(content || '') as string;
  currentContent.value = DOMPurify.sanitize(html);
  contentDialogVisible.value = true;
}

onMounted(() => {
  loadRequestPage({});
});
</script>

<template>
  <Page>
    <div class="page-container">
      <FilterForm />

      <div class="view-toolbar">
        <ElSegmented v-model="currentView" :options="[
          { label: '全部请求', value: 'all' },
          { label: '按智能体', value: 'agentStat' },
          { label: '按人员', value: 'userStat' },
        ]" @change="handleViewChange" />
        <ElButton class="refresh-btn" @click="loadRequestPage({})">
          <ElIcon><i class="lucide:refresh-cw"></i></ElIcon>
          刷新
        </ElButton>
      </div>

      <!-- 全部请求视图 -->
      <ElCard v-if="currentView === 'all'" class="table-section" :body-style="{ padding: '20px' }">
        <ElTable
          :data="tableData"
          style="width: 100%"
          stripe
          v-loading="loading"
          empty-text="暂无数据"
          border
        >
          <ElTableColumn prop="createTime" label="时间" width="180" align="center" />
          <ElTableColumn prop="username" label="用户名" width="120" />
          <ElTableColumn prop="realName" label="真实姓名" width="100" />
          <ElTableColumn prop="agentName" label="智能体" width="150" />
          <ElTableColumn prop="title" label="会话标题" minWidth="160" show-overflow-tooltip />
          <ElTableColumn prop="role" label="角色" width="90" align="center" />
          <ElTableColumn prop="messageType" label="消息类型" width="100" align="center" />
          <ElTableColumn label="内容" minWidth="200">
            <template #default="{ row }">
              <div class="content-cell">
                <span class="content-preview">{{ (row.content || '').replace(/<[^>]*>/g, '').substring(0, 50) }}...</span>
                <ElButton size="small" type="primary" link @click="openContentDialog(row.content)">查看</ElButton>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="sessionId" label="会话ID" width="120" show-overflow-tooltip />
        </ElTable>

        <div class="pagination-wrapper">
          <ElPagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </ElCard>

      <!-- 内容详情弹窗 -->
      <ElDialog v-model="contentDialogVisible" title="消息内容详情" width="70%" >
        <div class="content-detail" v-html="currentContent" />
      </ElDialog>

      <!-- 按智能体统计视图 -->
      <ElCard v-if="currentView === 'agentStat'" class="table-section" :body-style="{ padding: '20px' }">
        <ElTable
          :data="agentStatData"
          style="width: 100%"
          stripe
          v-loading="loading"
          empty-text="暂无数据"
          border
        >
          <ElTableColumn v-for="col in agentStatCols" :key="col.field" v-bind="col" />
          <ElTableColumn
            #default="{ row }"
            label="操作"
            width="120"
            align="center"
            fixed="right"
          >
            <ElButton size="small" type="primary" @click="currentAgentId = row.agentId; agentDetailDialogVisible = true; loadAgentDetail(row.agentId)">
              查看请求
            </ElButton>
          </ElTableColumn>
        </ElTable>
      </ElCard>

      <!-- 按人员统计视图 -->
      <ElCard v-if="currentView === 'userStat'" class="table-section" :body-style="{ padding: '20px' }">
        <ElTable
          :data="userStatData"
          style="width: 100%"
          stripe
          v-loading="loading"
          empty-text="暂无数据"
          border
        >
          <ElTableColumn v-for="col in userStatCols" :key="col.field" v-bind="col" />
          <ElTableColumn
            #default="{ row }"
            label="操作"
            width="120"
            align="center"
            fixed="right"
          >
            <ElButton size="small" type="primary" @click="currentUserDetailId = row.userId; userDetailDialogVisible = true; loadUserDetailRequests(row.userId)">
              查看请求
            </ElButton>
          </ElTableColumn>
        </ElTable>
      </ElCard>

      <!-- 智能体请求明细弹窗 -->
      <ElDialog
        v-model="agentDetailDialogVisible"
        :title="'智能体请求明细'"
        width="80%"
        top="5vh"
        @closed="agentDetailData = []"
      >
        <div v-loading="agentDetailLoading">
          <ElTable
            :data="agentDetailData"
            style="width: 100%"
            stripe
            empty-text="暂无数据"
            border
            max-height="60vh"
          >
            <ElTableColumn prop="createTime" label="时间" width="180" align="center" />
            <ElTableColumn prop="username" label="用户名" width="120" />
            <ElTableColumn prop="realName" label="真实姓名" width="100" />
            <ElTableColumn prop="title" label="会话标题" minWidth="160" show-overflow-tooltip />
            <ElTableColumn prop="role" label="角色" width="90" align="center" />
            <ElTableColumn prop="messageType" label="消息类型" width="100" align="center" />
            <ElTableColumn label="内容" minWidth="200">
              <template #default="{ row: detailRow }">
                <div class="content-cell">
                  <span class="content-preview">{{ (detailRow.content || '').replace(/<[^>]*>/g, '').substring(0, 50) }}...</span>
                  <ElButton size="small" type="primary" link @click="openContentDialog(detailRow.content)">查看</ElButton>
                </div>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="sessionId" label="会话ID" width="120" show-overflow-tooltip />
          </ElTable>
          <div class="pagination-wrapper">
            <ElPagination
              v-model:current-page="agentDetailPageNum"
              v-model:page-size="agentDetailPageSize"
              :total="agentDetailTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              small
              @current-change="handleAgentDetailPageChange"
              @size-change="handleAgentDetailSizeChange"
            />
          </div>
        </div>
</ElDialog>

      <!-- 用户请求明细弹窗 -->
      <ElDialog
        v-model="userDetailDialogVisible"
        title="用户请求明细"
        width="80%"
        top="5vh"
        @closed="userDetailData = []; userDetailLoading = false"
      >
        <div v-loading="userDetailLoading">
          <ElTable
            :data="userDetailData"
            style="width: 100%"
            stripe
            empty-text="暂无数据"
            border
            max-height="60vh"
          >
            <ElTableColumn prop="createTime" label="时间" width="180" align="center" />
            <ElTableColumn prop="agentName" label="智能体" width="150" />
            <ElTableColumn prop="title" label="会话标题" minWidth="160" show-overflow-tooltip />
            <ElTableColumn prop="role" label="角色" width="90" align="center" />
            <ElTableColumn prop="messageType" label="消息类型" width="100" align="center" />
            <ElTableColumn label="内容" minWidth="200">
              <template #default="{ row: detailRow }">
                <div class="content-cell">
                  <span class="content-preview">{{ (detailRow.content || '').replace(/<[^>]*>/g, '').substring(0, 50) }}...</span>
                  <ElButton size="small" type="primary" link @click="openContentDialog(detailRow.content)">查看</ElButton>
                </div>
              </template>
            </ElTableColumn>
          </ElTable>
          <div class="pagination-wrapper">
            <ElPagination
              v-model:current-page="userDetailPageNum"
              v-model:page-size="userDetailPageSize"
              :total="userDetailTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              small
              @current-change="handleUserDetailPageChange"
              @size-change="handleUserDetailSizeChange"
            />
          </div>
        </div>
      </ElDialog>

    </div>
  </Page>
</template>
<style scoped>
.page-container {
  @apply bg-background-deep;
}

.view-toolbar {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.refresh-btn {
  margin-left: auto;
}

.table-section {
  border-radius: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}

.content-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.content-preview {
  color: #606266;
  font-size: 13px;
}

.content-detail {
  max-height: 70vh;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
}
</style>