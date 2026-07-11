<script lang="ts" setup>
import type { PrivilegeLoginLog } from '#/api';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useVbenForm } from '#/adapter/form';

import {
  ElButton,
  ElCard,
  ElIcon,
  ElPagination,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import { getLoginLogPageApi } from '#/api';

const loading = ref(false);
const tableData = ref<PrivilegeLoginLog[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);

const [FilterForm] = useVbenForm({
  commonConfig: { componentProps: { clearable: true } },
  layout: 'inline',
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  actionButtonsReverse: true,
  submitButtonOptions: { content: '查询' },
  schema: [
    {
      fieldName: 'operationUsername',
      component: 'Input',
      label: '操作人',
      componentProps: { placeholder: '请输入操作人账号' },
    },
  ],
  handleSubmit: (values) => {
    page.value = 1;
    loadData(values);
  },
  handleReset: () => {
    page.value = 1;
    loadData({});
  },
});

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getLoginLogPageApi(
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

function handlePageChange(val: number) {
  page.value = val;
  loadData();
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  page.value = 1;
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page>
    <div class="page-container">
      <FilterForm />

      <ElCard class="table-section" :body-style="{ padding: '20px' }">
        <div class="table-toolbar">
          <ElButton @click="loadData">
            <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
            刷新
          </ElButton>
        </div>

        <ElTable
          :data="tableData"
          style="width: 100%"
          stripe
          v-loading="loading"
          empty-text="暂无数据"
        >
          <ElTableColumn prop="operationPerson" label="操作人姓名" min-width="120" />
          <ElTableColumn prop="operationUsername" label="操作人账号" width="150" />
          <ElTableColumn prop="ip" label="访问IP" width="160" />
          <ElTableColumn prop="createTime" label="登录时间" width="180" />
          <ElTableColumn
            prop="operationContent"
            label="操作内容"
            min-width="300"
            show-overflow-tooltip
          />
        </ElTable>

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
    </div>
  </Page>
</template>

<style scoped>
.page-container {
  @apply bg-background-deep;
}

.table-section {
  border-radius: 12px;
}

.table-toolbar {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}
</style>
