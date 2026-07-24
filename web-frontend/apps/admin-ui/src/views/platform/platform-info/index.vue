<script lang="ts" setup>
import type { PlatformInfo } from '#/api';

import { onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElCard,
  ElIcon,
  ElMessage,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { VbenTableAction } from '#/adapter/vxe-table';
import {
  deletePlatformInfoApi,
  getPlatformInfoPageApi,
} from '#/api/core/platform-info';
import { useColumns, useSearchFormSchema, typeLabels } from './data';
import Form from './form.vue';

const columns = useColumns();
const searchFormSchema = useSearchFormSchema();

const loading = ref(false);
const tableData = ref<PlatformInfo[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);

const [FilterForm] = useVbenForm({
  commonConfig: { componentProps: { clearable: true } },
  layout: 'inline',
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  actionButtonsReverse: true,
  submitButtonOptions: { content: '查询' },
  schema: searchFormSchema,
  handleSubmit: (values) => {
    page.value = 1;
    const params = Object.fromEntries(
        Object.entries(values).filter(([, v]) => v !== '' && v != null),
    );
    loadData(params);
  },
  handleReset: () => {
    page.value = 1;
    loadData({});
  },
});

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getPlatformInfoPageApi(
        page.value,
        pageSize.value,
        params,
    )) as any;
    const pageResult = res?.data || res;
    tableData.value = pageResult?.records || [];
    total.value = pageResult?.totalRow || 0;
  } catch {
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function onCreate() {
  formModalApi.setData({}).open();
}

function onEdit(row: PlatformInfo) {
  formModalApi.setData({ ...row }).open();
}

function getActions(row: PlatformInfo) {
  return [
    {
      text: '编辑',
      onClick: () => onEdit(row),
    },
    {
      text: '删除',
      danger: true,
      popConfirm: {
        title: `确定要删除【${row.name}】吗？`,
        confirm: () => handleDelete(row.id!, row.name!),
        okText: '确定',
        cancelText: '取消',
      },
    },
  ];
}

async function handleDelete(id: string, name: string) {
  try {
    await deletePlatformInfoApi(id);
    ElMessage.success('平台删除成功');
    loadData();
  } catch {
    ElMessage.error('删除失败');
  }
}

function handlePageChange(val: number) {
  page.value = val;
  loadData();
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  page.value = 1;
  loadData();
}

function refreshGrid() {
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page auto-content-height>
    <div class="page-container">
      <ElCard class="table-section" :body-style="{ padding: '20px', height: '100%', display: 'flex', flexDirection: 'column' }">
        <FilterForm />
        <div class="table-toolbar">
          <ElButton type="primary" @click="onCreate">
            <ElIcon><IconifyIcon icon="lucide:plus" /></ElIcon>
            新增
          </ElButton>
          <ElButton @click="loadData">
            <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
            刷新
          </ElButton>
        </div>

        <div class="table-wrapper">
          <ElTable
              :data="tableData"
              stripe
              height="100%"
              v-loading="loading"
              empty-text="暂无数据"
          >
            <template v-for="col in columns" :key="col.label">
              <ElTableColumn v-if="!col.slot" v-bind="col" />
              <ElTableColumn v-else v-bind="col">
                <template #default="scope">
                  <template v-if="col.slot === 'type'">
                    <ElTag type="primary" size="small">
                      {{ typeLabels[scope.row.type] || scope.row.type }}
                    </ElTag>
                  </template>
                  <template v-else-if="col.slot === 'status'">
                    <ElTag
                        :type="scope.row.status === '1' ? 'success' : 'danger'"
                        size="small"
                    >
                      {{ scope.row.status === '1' ? '启用' : '禁用' }}
                    </ElTag>
                  </template>
                  <template v-else-if="col.slot === 'secret'">
                    <span class="secret-text">{{ scope.row.corpsecret?.slice(0, 8) }}******</span>
                  </template>
                  <template v-else-if="col.slot === 'time'">
                    {{ scope.row.createTime?.replace('T', ' ')?.slice(0, 16) }}
                  </template>
                </template>
              </ElTableColumn>
            </template>
            <ElTableColumn label="操作" width="200">
              <template #default="{ row }">
                <VbenTableAction :actions="getActions(row as PlatformInfo)" />
              </template>
            </ElTableColumn>
          </ElTable>
        </div>

        <div class="pagination-wrapper">
          <ElPagination
              v-model:current-page="page"
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
      <FormModal @success="refreshGrid" />
    </div>
  </Page>
</template>

<style scoped>
.page-container {
  @apply bg-background-deep;

  height: 100%;
  display: flex;
  flex-direction: column;
}

.table-section {
  border-radius: 12px;
  flex: 1;
  min-height: 0;
}

.table-toolbar {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
  flex-shrink: 0;
}

.table-wrapper {
  flex: 1;
  min-height: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
  flex-shrink: 0;
}

.secret-text {
  font-family: monospace;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}
</style>
