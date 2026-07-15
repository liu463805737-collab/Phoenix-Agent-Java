<script lang="ts" setup>
import { ref } from 'vue';

import { ColPage, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid, VbenTableAction } from '#/adapter/vxe-table';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { VbenFormProps } from '@vben/common-ui';

import {
  ElButton,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import {
  deleteAccountInfoApi,
  getAccountInfoPageApi,
} from '#/api';
import DeptTreeSidebar from '#/components/dept/DeptTreeSidebar.vue';

import Form from './form.vue';
import GroupForm from './group-form.vue';
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
        const res = (await getAccountInfoPageApi(
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
const [GroupModal, groupModalApi] = useVbenModal({
  connectedComponent: GroupForm,
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
    `确定要删除账号 "${row.username}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    },
  )
    .then(() => {
      deleteAccountInfoApi(row.id).then(() => {
        ElMessage.success('删除成功');
        gridApi.query();
      });
    })
    .catch(() => {});
}

function openAssignGroupDialog(row: any) {
  groupModalApi.setData({ ...row }).open();
}

function getActions(row: any) {
  return [
    {
      text: '编辑',
      icon: 'lucide:edit',
      onClick: () => onEdit(row),
    },
    {
      text: '分配组',
      icon: 'lucide:users',
      onClick: () => openAssignGroupDialog(row),
    },
    {
      text: '删除',
      icon: 'lucide:trash-2',
      danger: true,
      popConfirm: {
        title: `确定要删除【${row.username}】吗？`,
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
    <GroupModal @success="refreshGrid" />
    <Grid table-title="账号列表">
      <template #toolbar-tools>
        <ElButton type="primary" @click="onCreate">新增</ElButton>
      </template>
      <template #genderSlot="{ row }">
        <ElTag
          :type="row.gender === '1' ? 'primary' : row.gender === '0' ? 'success' : 'info'"
          size="small"
        >
          {{ row.gender === '1' ? '男' : row.gender === '0' ? '女' : '未知' }}
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
      <template #groupsSlot="{ row }">
        <span>{{
          (row.groups || []).map((g: any) => g.groupName).filter(Boolean).join('、') || '-'
        }}</span>
      </template>
      <template #action="{ row }">
        <VbenTableAction align="center" :actions="getActions(row)" />
      </template>
    </Grid>
  </ColPage>
</template>
