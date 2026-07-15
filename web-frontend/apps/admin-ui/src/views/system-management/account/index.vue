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
  deleteUserApi,
  getUserPageApi,
} from '#/api';
import DeptTreeSidebar from '#/components/dept/DeptTreeSidebar.vue';

import Form from './form.vue';
import PasswordForm from './password-form.vue';
import RoleForm from './role-form.vue';
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
        const res = (await getUserPageApi(
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
const [PasswordModal, passwordModalApi] = useVbenModal({
  connectedComponent: PasswordForm,
  destroyOnClose: true,
});
const [RoleModal, roleModalApi] = useVbenModal({
  connectedComponent: RoleForm,
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
      deleteUserApi(row.id).then(() => {
        ElMessage.success('删除成功');
        gridApi.query();
      });
    })
    .catch(() => {});
}

function handleSetPassword(row: any) {
  passwordModalApi.setData({ id: row.id }).open();
}

function handleAssignRole(row: any) {
  roleModalApi.setData({ ...row }).open();
}

function getActions(row: any) {
  return [
    {
      text: '分配权限',
      icon: 'lucide:shield',
      onClick: () => handleAssignRole(row),
    },
    {
      text: '设置密码',
      icon: 'lucide:key',
      onClick: () => handleSetPassword(row),
    },
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
    <PasswordModal @success="refreshGrid" />
    <RoleModal @success="refreshGrid" />
    <Grid table-title="账号列表">
      <template #toolbar-tools>
        <ElButton type="primary" @click="onCreate">新增</ElButton>
      </template>
      <template #userTypeSlot="{ row }">
        <ElTag
          :type="row.userType === 1 ? 'primary' : 'info'"
          size="small"
        >
          {{ row.userType === 1 ? 'idm用户' : '自建用户' }}
        </ElTag>
      </template>
      <template #statusSlot="{ row }">
        <ElTag
          :type="row.status === 0 ? 'success' : 'danger'"
          size="small"
        >
          {{ row.status === 0 ? '启用' : '禁用' }}
        </ElTag>
      </template>
      <template #rolesSlot="{ row }">
        <span>{{
          (row.roles || []).map((r: any) => r.name).join('、')
        }}</span>
      </template>
      <template #action="{ row }">
        <VbenTableAction align="center" :actions="getActions(row)" />
      </template>
    </Grid>
  </ColPage>
</template>
