<script lang="ts" setup>
import { ref } from 'vue';

import { ColPage, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid, VbenTableAction } from '#/adapter/vxe-table';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { VbenFormProps } from '@vben/common-ui';

import { ElButton, ElMessage, ElMessageBox, ElTag } from 'element-plus';

import {
  deleteEmployeeApi,
  getEmployeePageApi,
} from '#/api';
import DeptTreeSidebar from '#/components/dept/DeptTreeSidebar.vue';

import Form from './form.vue';
import { useColumns, useSearchFormSchema } from './data';

const selectedDeptId = ref<string | undefined>(undefined);

const formOptions: VbenFormProps = {
  showCollapseButton: false,
  submitOnEnter: true,
  commonConfig: { labelWidth: 60 },
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
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
        const params: any = { ...formValues };
        if (selectedDeptId.value) {
          params.deptId = selectedDeptId.value;
        }
        const res = (await getEmployeePageApi(
          page.currentPage,
          page.pageSize,
          params,
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
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

function handleDeptClick() {
  gridApi.query();
}

function onCreate() {
  formModalApi.setData({}).open();
}

function onEdit(row: any) {
  formModalApi.setData({ ...row }).open();
}

function onDelete(row: any) {
  ElMessageBox.confirm(
    `确定要删除人员 "${row.empName}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    },
  )
    .then(() => {
      deleteEmployeeApi(row.id).then(() => {
        ElMessage.success('删除成功');
        gridApi.query();
      });
    })
    .catch(() => {});
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
        title: `确定要删除【${row.empName}】吗？`,
        confirm: () => onDelete(row),
        okText: '确定',
        cancelText: '取消',
      },
    },
  ];
}

function refreshGrid() {
  gridApi.query();
}
</script>

<template>
  <ColPage
    :left-max-width="50"
    :left-min-width="10"
    :left-width="15"
    :split-handle="false"
    :split-line="false"
    :resizable="true"
    :left-collapsible="false"
    auto-content-height
  >
    <template #left>
      <DeptTreeSidebar
        v-model="selectedDeptId"
        no-card
        @select="handleDeptClick"
      />
    </template>
    <FormModal @success="refreshGrid" />
    <Grid table-title="人员管理">
      <template #toolbar-tools>
        <ElButton type="primary" @click="onCreate">新增</ElButton>
      </template>
      <template #statusSlot="{ row }">
        <ElTag
          :type="row.status === 1 ? 'success' : 'danger'"
          size="small"
        >
          {{ row.status === 1 ? '在职' : '离职' }}
        </ElTag>
      </template>
      <template #enableFlagSlot="{ row }">
        <ElTag
          :type="row.enableFlag === 1 ? 'success' : 'info'"
          size="small"
        >
          {{ row.enableFlag === 1 ? '是' : '否' }}
        </ElTag>
      </template>
      <template #action="{ row }">
        <VbenTableAction align="center" :actions="getActions(row)" />
      </template>
    </Grid>
  </ColPage>
</template>
