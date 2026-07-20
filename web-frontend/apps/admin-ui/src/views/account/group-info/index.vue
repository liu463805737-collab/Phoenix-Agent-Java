<script lang="ts" setup>
import { Page, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid, VbenTableAction } from '#/adapter/vxe-table';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { VbenFormProps } from '@vben/common-ui';

import { ElButton, ElMessage, ElMessageBox } from 'element-plus';

import { deleteGroupInfoApi, getGroupInfoPageApi } from '#/api';

import AssignAgentForm from './assign-agent-form.vue';
import AssignPeopleForm from './assign-people-form.vue';
import Form from './form.vue';
import { useColumns, useSearchFormSchema } from './data';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [AssignPeopleModal, assignPeopleModalApi] = useVbenModal({
  connectedComponent: AssignPeopleForm,
  destroyOnClose: true,
});
const [AssignAgentModal, assignAgentModalApi] = useVbenModal({
  connectedComponent: AssignAgentForm,
  destroyOnClose: true,
});

const formOptions: VbenFormProps = {
  showCollapseButton: false,
  submitOnEnter: true,
  commonConfig: { labelWidth: 60 },
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  actionWrapperClass: 'pl-2 !justify-end md:!justify-start',
  actionPosition: 'left',
  actionLayout: 'inline',
  schema: useSearchFormSchema(),
};

const gridOptions: VxeGridProps = {
  columns: useColumns(),
  columnConfig: { resizable: true },
  height: 'auto',
  keepSource: true,
  border: false,
  stripe: true,
  showOverflow: true,
  proxyConfig: {
    ajax: {
      query: async ({ page }, formValues) => {
        const res = (await getGroupInfoPageApi(
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
  ElMessageBox.confirm(
    `确定要禁用组 "${row.name}" 吗？此操作不可恢复。`,
    '删除禁用',
    {
      confirmButtonText: '确定禁用',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    },
  )
    .then(() => {
      deleteGroupInfoApi(row.id).then(() => {
        ElMessage.success('组禁用成功');
        refreshGrid();
      });
    })
    .catch(() => {});
}

function getActions(row: any) {
  return [
    {
      text: '分配人员',
      icon: 'lucide:users',
      onClick: () => assignPeopleModalApi.setData({ ...row }).open(),
    },
    {
      text: '分配智能体',
      icon: 'lucide:bot',
      onClick: () => assignAgentModalApi.setData({ ...row }).open(),
    },
    {
      text: '编辑',
      icon: 'lucide:edit',
      onClick: () => onEdit(row),
    },
    {
      text: '禁用',
      icon: 'lucide:ban',
      danger: true,
      popConfirm: {
        title: `确定要禁用组【${row.name}】吗？`,
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
    <AssignPeopleModal @success="refreshGrid" />
    <AssignAgentModal @success="refreshGrid" />
    <Grid table-title="组管理">
      <template #toolbar-tools>
        <ElButton type="primary" @click="onCreate">新增</ElButton>
      </template>
      <template #action="{ row }">
        <VbenTableAction align="center" :actions="getActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
