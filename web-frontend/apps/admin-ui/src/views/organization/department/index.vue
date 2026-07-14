<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElCard,
  ElEmpty,
  ElIcon,
  ElMessage,
  ElMessageBox,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  deleteDepartmentApi,
  getCompanyPageApi,
  getDeptTreeApi,
  type PrivilegeCompany,
  type PrivilegeDepartment,
} from '#/api';

import Form from './form.vue';

const companiesLoading = ref(false);
const deptLoading = ref(false);
const companies = ref<PrivilegeCompany[]>([]);
const tableData = ref<PrivilegeDepartment[]>([]);
const selectedCompany = ref<PrivilegeCompany | null>(null);

async function loadCompanies() {
  companiesLoading.value = true;
  try {
    const res = (await getCompanyPageApi({ page: 1, size: 999 })) as any;
    const pageResult = res?.data || res;
    companies.value = pageResult?.records || [];
    if (companies.value.length > 0) {
      selectCompany(companies.value[0]);
    }
  } catch {
    companies.value = [];
  } finally {
    companiesLoading.value = false;
  }
}

async function loadDepartments() {
  if (!selectedCompany.value?.id) {
    tableData.value = [];
    return;
  }
  deptLoading.value = true;
  try {
    const res = (await getDeptTreeApi(selectedCompany.value.id)) as any;
    tableData.value = res?.data || res || [];
  } catch {
    tableData.value = [];
  } finally {
    deptLoading.value = false;
  }
}

function selectCompany(company: PrivilegeCompany) {
  selectedCompany.value = company;
  loadDepartments();
}

function onCreate() {
  if (!selectedCompany.value) {
    ElMessage.warning('请先选择一家公司');
    return;
  }
  formModalApi.setData({
    row: { companyId: selectedCompany.value.id },
    tree: tableData.value,
    parentName: '',
  }).open();
}

function onEdit(row: any) {
  formModalApi.setData({
    row: { ...row },
    tree: tableData.value,
    parentName: row.parentName || '',
  }).open();
}

function handleAddChild(row: any) {
  formModalApi.setData({
    row: {
      companyId: selectedCompany.value?.id,
      pid: row.id,
    },
    tree: tableData.value,
    parentName: row.name || '',
  }).open();
}

function onDelete(row: any) {
  ElMessageBox.confirm(
    `确定要删除部门 "${row.name}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    },
  ).then(() => {
    deleteDepartmentApi(row.id)
      .then(() => {
        ElMessage.success('部门删除成功');
        loadDepartments();
      });
  }).catch(() => {});
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

function refreshGrid() {
  loadDepartments();
}

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

onMounted(() => {
  loadCompanies();
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="refreshGrid" />
    <div class="page-container">
      <div class="split-layout">
        <div class="left-panel">
          <ElCard
            class="company-card"
            :body-style="{ padding: '0' }"
            :loading="companiesLoading"
          >
            <template #header>
              <div class="card-header">
                <ElIcon><IconifyIcon icon="lucide:building" /></ElIcon>
                <span>公司列表</span>
              </div>
            </template>
            <div
              v-if="companies.length === 0 && !companiesLoading"
              class="empty-list"
            >
              <ElEmpty description="暂无公司" />
            </div>
            <div v-else class="company-list">
              <div
                v-for="company in companies"
                :key="company.id"
                class="company-item"
                :class="{ active: selectedCompany?.id === company.id }"
                @click="selectCompany(company)"
              >
                <div class="company-name">
                  <ElIcon><IconifyIcon icon="lucide:building-2" /></ElIcon>
                  <span>{{ company.cname }}</span>
                </div>
                <div class="company-meta">
                  <ElTag size="small" type="info">
                    {{ company.code }}
                  </ElTag>
                  <ElTag
                    size="small"
                    :type="company.status === 1 ? 'success' : 'danger'"
                  >
                    {{ company.status === 1 ? '启用' : '禁用' }}
                  </ElTag>
                </div>
              </div>
            </div>
          </ElCard>
        </div>

        <div class="right-panel">
          <ElCard class="dept-card" :body-style="{ padding: '20px' }">
            <template #header>
              <div class="card-header">
                <ElIcon><IconifyIcon icon="lucide:network" /></ElIcon>
                <span>
                  {{
                    selectedCompany
                      ? `部门管理 - ${selectedCompany.cname}`
                      : '部门管理'
                  }}
                </span>
              </div>
            </template>

            <div v-if="!selectedCompany" class="no-company-tip">
              <ElEmpty description="请从左侧选择一家公司">
                <template #image>
                  <ElIcon size="60">
                    <IconifyIcon icon="lucide:building-2" />
                  </ElIcon>
                </template>
              </ElEmpty>
            </div>

            <div v-else class="dept-content">
              <div class="table-toolbar">
                <ElButton type="primary" @click="onCreate">
                  <ElIcon><IconifyIcon icon="lucide:plus" /></ElIcon>
                  新增
                </ElButton>
                <ElButton @click="loadDepartments">
                  <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
                  刷新
                </ElButton>
              </div>

              <ElTable
                :data="tableData"
                style="width: 100%; height: 100%"
                stripe
                v-loading="deptLoading"
                empty-text="暂无部门数据"
                row-key="id"
                default-expand-all
                :tree-props="{
                  children: 'children',
                  hasChildren: 'hasChildren',
                }"
              >
                <ElTableColumn prop="name" label="部门名称" min-width="180" />
                <ElTableColumn prop="code" label="部门编码" width="140" />
                <ElTableColumn prop="parentName" label="上级部门" width="140" />
                <ElTableColumn prop="orderNo" label="排序" width="70" />
                <ElTableColumn prop="status" label="状态" width="80">
                  <template #default="scope">
                    <ElTag
                      :type="scope.row.status === 0 ? 'success' : 'danger'"
                      size="small"
                    >
                      {{ scope.row.status === 0 ? '启用' : '禁用' }}
                    </ElTag>
                  </template>
                </ElTableColumn>
                <ElTableColumn prop="nature" label="部门性质" width="80">
                  <template #default="scope">
                    <ElTag
                      :type="scope.row.nature === 0 ? 'primary' : 'warning'"
                      size="small"
                    >
                      {{ scope.row.nature === 0 ? '部门' : '组' }}
                    </ElTag>
                  </template>
                </ElTableColumn>
                <ElTableColumn label="创建时间" width="165">
                  <template #default="scope">
                    {{ scope.row.createTime?.replace('T', ' ')?.slice(0, 16) }}
                  </template>
                </ElTableColumn>
                <ElTableColumn label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <div class="action-btns">
                      <ElButton
                        type="primary"
                        size="small"
                        @click="handleAddChild(scope.row as PrivilegeDepartment)"
                      >
                        新增
                      </ElButton>
                      <ElButton
                        type="warning"
                        size="small"
                        @click="onEdit(scope.row as PrivilegeDepartment)"
                      >
                        编辑
                      </ElButton>
                      <ElButton
                        type="danger"
                        size="small"
                        @click="
                          onDelete(scope.row as PrivilegeDepartment)
                        "
                      >
                        删除
                      </ElButton>
                    </div>
                  </template>
                </ElTableColumn>
              </ElTable>
            </div>
          </ElCard>
        </div>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.page-container {
  @apply bg-background-deep;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.split-layout {
  display: flex;
  flex: 1;
  gap: 1rem;
  overflow: hidden;
}

.left-panel {
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  width: 240px;
}

.right-panel {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.company-card {
  display: flex;
  flex: 1;
  flex-direction: column;
  border-radius: 12px;
}

.company-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  overflow: hidden;
}

.dept-card {
  display: flex;
  flex: 1;
  flex-direction: column;
  border-radius: 12px;
}

.dept-card :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  overflow: hidden;
}

.dept-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  overflow: hidden;
}

.dept-content .el-table {
  flex: 1;
}

.card-header {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  font-size: 0.95rem;
  font-weight: 600;
}

.company-list {
  flex: 1;
  overflow-y: auto;
}

.company-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.company-item:hover {
  background: #f5f7fa;
}

.company-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.company-name {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  font-weight: 500;
  color: #1f2937;
}

.company-meta {
  display: flex;
  gap: 0.5rem;
  padding-left: 1.6rem;
}

.empty-list {
  padding: 2rem 0;
}

.no-company-tip {
  padding: 3rem 0;
}

.table-toolbar {
  display: flex;
  flex-shrink: 0;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.action-btns {
  display: flex;
  gap: 0.5rem;
}
</style>
