<script lang="ts" setup>
import { Page, useVbenModal } from '@vben/common-ui';
import { useVbenVxeGrid, VbenTableAction } from '#/adapter/vxe-table';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { VbenFormProps } from '@vben/common-ui';
import { ElButton, ElMessage } from 'element-plus';

import { useColumns, useSearchFormSchema } from './data';
import Form from './form.vue';

import {
  deletePvalueApi,
  getPvaluePageApi,
} from '#/api';

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
  schema: useSearchFormSchema(),
};

const gridOptions: VxeGridProps = {
  checkboxConfig: {
    highlight: true,
    labelField: 'name',
  },
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
        return await getPvaluePageApi({
          page: page.currentPage,
          size: page.pageSize,
          ...formValues,
        });
      },
    },
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ formOptions, gridOptions });

function onCreate() {
  formModalApi.setData(null).open();
}

function onEdit(row: any) {
  formModalApi.setData(row).open();
}

function refreshGrid() {
  gridApi.query();
}

function onDelete(row: any) {
  deletePvalueApi(row.id)
    .then(() => {
      ElMessage.success('删除成功');
      refreshGrid();
    });
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <Grid table-title="权限值列表">
      <template #toolbar-tools>
        <ElButton type="primary" @click="onCreate">新建</ElButton>
      </template>

      <template #action="{ row }">
        <VbenTableAction
          align="center"
          :actions="[
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
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
