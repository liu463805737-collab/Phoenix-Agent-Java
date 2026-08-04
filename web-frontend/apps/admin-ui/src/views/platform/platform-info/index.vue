<script lang="ts" setup>
import { Page, useVbenModal } from '@vben/common-ui';
import type { VbenFormProps } from '@vben/common-ui';
import { useVbenVxeGrid, VbenTableAction } from '#/adapter/vxe-table';
import type { VxeGridProps } from '#/adapter/vxe-table';

import { ElButton, ElMessage, ElTag } from 'element-plus';

import {
  deletePlatformInfoApi,
  getPlatformInfoPageApi,
} from '#/api/core/platform-info';

import { useColumns, useSearchFormSchema, typeLabels } from './data';
import Form from './form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const formOptions: VbenFormProps = {
  showCollapseButton: false,
  submitOnEnter: true,
  commonConfig: {
    labelWidth: 60,
  },
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  actionWrapperClass: 'pl-2 !justify-end md:!justify-start',
  actionPosition: 'left',
  actionLayout: 'inline',
  submitButtonOptions: { content: '查询' },
  resetButtonOptions: { plain: true },
  schema: useSearchFormSchema(),
};

const gridOptions: VxeGridProps = {
  columns: useColumns(),
  columnConfig: { resizable: true },
  height: 'auto',
  keepSource: true,
  border: false,
  stripe: true,
  showOverflow: false,
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues) => {
        const res = (await getPlatformInfoPageApi(
          page.currentPage,
          page.pageSize,
          formValues,
        )) as any;
        const data = res?.data || res;
        return { records: data?.records || [], totalRow: data?.totalRow || 0 };
      },
    },
  },
  pagerConfig: {
    pageSize: 10,
    pageSizes: [10, 20, 50, 100],
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ formOptions, gridOptions });

function onCreate() {
  formModalApi.setData({}).open();
}

function onEdit(row: any) {
  formModalApi.setData({ ...row }).open();
}

function refreshGrid() {
  gridApi.query();
}

function onDelete(row: any) {
  deletePlatformInfoApi(row.id)
    .then(() => {
      ElMessage.success('平台删除成功');
      refreshGrid();
    })
    .catch(() => {
      ElMessage.error('删除失败');
    });
}

function getActions(row: any) {
  return [
    {
      text: '编辑',
      icon: 'lucide:edit',
      onClick: () => onEdit(row),
    },
    {
      text: '删除',
      icon: 'lucide:trash-2',
      danger: true,
      popConfirm: {
        title: `确定要删除【${row.name}】吗？`,
        confirm: () => onDelete(row),
        okText: '确定',
        cancelText: '取消',
      },
    },
  ];
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="三方平台列表">
      <template #toolbar-tools>
        <ElButton type="primary" @click="onCreate">新增</ElButton>
      </template>

      <template #typeSlot="{ row }">
        <ElTag type="primary" size="small">
          {{ typeLabels[row.type] || row.type }}
        </ElTag>
      </template>

      <template #statusSlot="{ row }">
        <ElTag
          :type="row.status === '1' ? 'success' : 'danger'"
          size="small"
        >
          {{ row.status === '1' ? '启用' : '禁用' }}
        </ElTag>
      </template>

      <template #secretSlot="{ row }">
        <span class="secret-text">{{ row.corpsecret?.slice(0, 8) }}******</span>
      </template>

      <template #timeSlot="{ row }">
        {{ row.createTime?.replace('T', ' ')?.slice(0, 16) }}
      </template>

      <template #action="{ row }">
        <VbenTableAction
          align="center"
          :actions="getActions(row)"
        />
      </template>
    </Grid>
  </Page>
</template>

<style scoped>
.secret-text {
  font-family: monospace;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}
</style>
