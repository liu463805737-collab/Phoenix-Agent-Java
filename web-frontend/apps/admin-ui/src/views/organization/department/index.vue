<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { ColPage, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { useVbenVxeGrid, VbenTableAction } from '#/adapter/vxe-table';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { VbenFormProps } from '@vben/common-ui';

import { ElButton, ElEmpty, ElIcon, ElMessage, ElMessageBox, ElTag } from 'element-plus';

import {
  deleteDepartmentApi,
  getCompanyPageApi,
  getDeptTreeApi,
  syncAllApi,
  syncDepartmentsApi,
  syncSubDepartmentsApi,
  type PrivilegeCompany,
} from '#/api';

import Form from './form.vue';
import { useColumns, useSearchFormSchema } from './data';

const currentNode = ref<PrivilegeCompany | null>(null);
const companies = ref<PrivilegeCompany[]>([]);
const companiesLoading = ref(false);
const fullTreeData = ref<any[]>([]);

async function loadCompanies() {
  companiesLoading.value = true;
  try {
    const res = (await getCompanyPageApi({ page: 1, size: 999 })) as any;
    const pageResult = res?.data || res;
    companies.value = pageResult?.records || [];
    if (companies.value.length > 0) {
      handleSelect(companies.value[0]);
    }
  } catch {
    companies.value = [];
  } finally {
    companiesLoading.value = false;
  }
}

function handleSelect(company?: PrivilegeCompany) {
  if (!company) return;
  currentNode.value = company;
  gridApi.query();
}

const formOptions: VbenFormProps = {
  showCollapseButton: false,
  submitOnEnter: true,
  commonConfig: {
    labelWidth: 60,
  },
  wrapperClass: 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  actionWrapperClass: 'pl-2 !justify-end md:!justify-start',
  actionPosition: 'left',
  actionLayout: 'inline',
  schema: useSearchFormSchema(),
};

const gridOptions: VxeGridProps = {
  pagerConfig: {
    enabled: false,
  },
  treeConfig: {
    rowField: 'id',
    children: 'children',
  },
  columns: useColumns(),
  columnConfig: { resizable: true },
  height: 'auto',
  keepSource: true,
  border: false,
  stripe: true,
  showOverflow: true,
  proxyConfig: {
    ajax: {
      query: async ({ page: _page }, formValues) => {
        const companyId = currentNode.value?.id;
        if (!companyId) return { records: [] };
        const res = (await getDeptTreeApi(companyId)) as any;
        const data = res?.data || res || [];
        const treeData = Array.isArray(data) ? data : [];
        fullTreeData.value = treeData;
        const keyword = formValues?.keyword?.trim();
        if (!keyword) return { records: treeData };
        const kw = keyword.toLowerCase();
        function filterTree(nodes: any[]): any[] {
          return nodes.reduce((acc: any[], node) => {
            const selfMatch =
              (node.name?.toLowerCase() || '').includes(kw) ||
              (node.code?.toLowerCase() || '').includes(kw);
            const filteredChildren = node.children ? filterTree(node.children) : [];
            if (selfMatch || filteredChildren.length > 0) {
              acc.push({ ...node, children: filteredChildren.length > 0 ? filteredChildren : (selfMatch ? node.children : []) });
            }
            return acc;
          }, []);
        }
        return { records: filterTree(treeData) };
      },
    },
  },
};

const [Grid, gridApi] = useVbenVxeGrid({ formOptions, gridOptions });
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

function onCreate() {
  if (!currentNode.value) {
    ElMessage.warning('请先选择一家公司');
    return;
  }
  formModalApi.setData({
    row: { companyId: currentNode.value.id },
    tree: fullTreeData.value,
    parentName: '',
  }).open();
}

function onEdit(row: any) {
  formModalApi.setData({
    row: { ...row },
    tree: fullTreeData.value,
    parentName: row.parentName || '',
  }).open();
}

function handleAddChild(row: any) {
  formModalApi.setData({
    row: {
      companyId: currentNode.value?.id,
      pid: row.id,
    },
    tree: fullTreeData.value,
    parentName: row.name || '',
  }).open();
}

function onDelete(row: any) {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('有子节点，不能删除！');
    return;
  }
  deleteDepartmentApi(row.id).then(() => {
    ElMessage.success('删除成功');
    gridApi.query();
  });
}

async function handleSyncDepartments() {
  if (!currentNode.value) {
    ElMessage.warning('请先选择一家公司');
    return;
  }
  try {
    await ElMessageBox.confirm('确定要全链同步部门数据吗？此操作将从三方平台拉取最新部门数据。', '同步确认', {
      confirmButtonText: '确定同步',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await syncDepartmentsApi();
    ElMessage.success('全链同步部门成功');
    gridApi.query();
  } catch {
    // cancelled or error
  }
}

async function handleSyncAll() {
  try {
    await ElMessageBox.confirm('确定要全量同步部门和人员数据吗？此操作将从三方平台拉取所有部门及人员数据。', '同步确认', {
      confirmButtonText: '确定同步',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await syncAllApi();
    ElMessage.success('全量同步部门和人员成功');
    gridApi.query();
  } catch {
    // cancelled or error
  }
}

async function handleSyncSubDept(row: any) {
  try {
    await ElMessageBox.confirm(`确定要同步【${row.name}】的下级部门吗？`, '同步确认', {
      confirmButtonText: '确定同步',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await syncSubDepartmentsApi(row.id);
    ElMessage.success('同步下级部门成功');
    gridApi.query();
  } catch {
    // cancelled or error
  }
}

function refreshGrid() {
  gridApi.query();
}

function getActions(row: any) {
  return [
    {
      text: '新增',
      icon: 'lucide:plus',
      onClick: () => handleAddChild(row),
    },
    {
      text: '编辑',
      icon: 'lucide:edit',
      onClick: () => onEdit(row),
    },
    {
      text: '同步下级',
      icon: 'lucide:refresh-cw',
      popConfirm: {
        title: `确定同步【${row.name}】的下级部门？`,
        confirm: () => handleSyncSubDept(row),
        okText: '确定',
        cancelText: '取消',
      },
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

onMounted(() => {
  loadCompanies();
});
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
      <div class="flex h-full flex-col rounded-lg border bg-card">
        <div class="flex items-center gap-2 border-b border-border px-4 py-3 text-sm font-semibold">
          <ElIcon><IconifyIcon icon="lucide:building" /></ElIcon>
          <span>公司列表</span>
        </div>
        <div v-if="companies.length === 0 && !companiesLoading" class="py-8">
          <ElEmpty description="暂无公司" />
        </div>
        <div v-if="companiesLoading" class="py-8">
          <div class="text-center text-sm text-muted-foreground">加载中...</div>
        </div>
        <div v-else class="flex-1 overflow-y-auto">
          <div
            v-for="company in companies"
            :key="company.id"
            :class="[
              'cursor-pointer border-b border-border px-4 py-3 transition-colors hover:bg-accent',
              currentNode?.id === company.id ? 'border-l-4 border-l-primary bg-accent' : '',
            ]"
            @click="handleSelect(company)"
          >
            <div class="flex items-center gap-2 font-medium text-foreground">
              <ElIcon><IconifyIcon icon="lucide:building-2" /></ElIcon>
              <span>{{ company.cname }}</span>
            </div>
            <div class="flex gap-2 pl-7 pt-1">
              <ElTag size="small" type="info">{{ company.code }}</ElTag>
              <ElTag
                size="small"
                :type="company.status === 1 ? 'success' : 'danger'"
              >
                {{ company.status === 1 ? '启用' : '禁用' }}
              </ElTag>
            </div>
          </div>
        </div>
      </div>
    </template>
    <FormModal @success="refreshGrid" />
    <Grid table-title="部门管理">
      <template #toolbar-tools>
        <ElButton @click="handleSyncDepartments">
          <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
          全链同步部门
        </ElButton>
        <ElButton @click="handleSyncAll">
          <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
          全量同步部门和人员
        </ElButton>
        <ElButton type="primary" @click="onCreate">新增</ElButton>
      </template>
      <template #statusSlot="{ row }">
        <ElTag
          :type="row.status === 0 ? 'success' : 'danger'"
          size="small"
        >
          {{ row.status === 0 ? '启用' : '禁用' }}
        </ElTag>
      </template>
      <template #natureSlot="{ row }">
        <ElTag
          :type="row.nature === 0 ? 'primary' : 'warning'"
          size="small"
        >
          {{ row.nature === 0 ? '部门' : '组' }}
        </ElTag>
      </template>
      <template #action="{ row }">
        <VbenTableAction align="center" :actions="getActions(row)" />
      </template>
    </Grid>
  </ColPage>
</template>

