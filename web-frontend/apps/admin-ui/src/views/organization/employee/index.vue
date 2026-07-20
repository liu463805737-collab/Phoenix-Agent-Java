<script lang="ts" setup>
import { ref } from 'vue';

import { ColPage, useVbenModal } from '@vben/common-ui';

import { useVbenVxeGrid, VbenTableAction } from '#/adapter/vxe-table';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { VbenFormProps } from '@vben/common-ui';

import { ElButton, ElIcon, ElMessage, ElMessageBox, ElTag } from 'element-plus';
import { IconifyIcon } from '@vben/icons';

import {
  deleteEmployeeApi,
  getEmployeePageApi,
  syncUserApi,
  syncUsersByDeptApi,
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

async function handleSyncUsersByDept() {
  if (!selectedDeptId.value) {
    ElMessage.warning('请先在左侧选择部门');
    return;
  }
  try {
    await ElMessageBox.confirm('确定要同步该部门下的人员数据吗？此操作将从三方平台拉取最新人员数据。', '同步确认', {
      confirmButtonText: '确定同步',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await syncUsersByDeptApi(selectedDeptId.value);
    ElMessage.success('同步部门人员成功');
    gridApi.query();
  } catch {
    // cancelled or error
  }
}

async function handleSyncUser(row: any) {
  const userId = row.thirdUserId || row.id;
  if (!userId) {
    ElMessage.warning('缺少用户标识，无法同步');
    return;
  }
  try {
    await ElMessageBox.confirm(`确定要同步【${row.empName}】的个人信息吗？`, '同步确认', {
      confirmButtonText: '确定同步',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await syncUserApi(userId);
    ElMessage.success('同步个人信息成功');
    gridApi.query();
  } catch {
    // cancelled or error
  }
}

function getActions(row: any) {
  return [
    {
      text: '同步信息',
      icon: 'lucide:refresh-cw',
      popConfirm: {
        title: `确定同步【${row.empName}】的个人信息？`,
        confirm: () => handleSyncUser(row),
        okText: '确定',
        cancelText: '取消',
      },
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
        <ElButton :disabled="!selectedDeptId" @click="handleSyncUsersByDept">
          <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
          同步指定部门下的人员
        </ElButton>
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
