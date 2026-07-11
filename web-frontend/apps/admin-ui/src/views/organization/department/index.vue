<script lang="ts" setup>
import {nextTick, onMounted, reactive, ref} from 'vue';

import {Page} from '@vben/common-ui';
import {IconifyIcon} from '@vben/icons';

import type {FormInstance, FormRules} from 'element-plus';
import {
  ElButton,
  ElCard,
  ElDialog,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
  ElTree,
} from 'element-plus';

import {
  createDepartmentApi,
  deleteDepartmentApi,
  getCompanyPageApi,
  getDeptTreeApi,
  type PrivilegeCompany,
  type PrivilegeDepartment,
  updateDepartmentApi
} from '#/api';

const companiesLoading = ref(false);
const deptLoading = ref(false);
const companies = ref<PrivilegeCompany[]>([]);
const tableData = ref<PrivilegeDepartment[]>([]);
const selectedCompany = ref<PrivilegeCompany | null>(null);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const formData = reactive<Record<string, any>>({
  name: '',
  code: '',
  companyId: undefined,
  pid: undefined,
  orderNo: 0,
  status: 0,
  nature: 0,
});

const formRules: FormRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
};

const pidDialogVisible = ref(false);
const selectedParentName = ref('');
const pidTreeRef = ref();

function findNodeById(
  nodes: PrivilegeDepartment[],
  id: string | undefined,
): PrivilegeDepartment | null {
  if (!id) return null;
  for (const node of nodes) {
    if (node.id === id) return node;
    if (node.children) {
      const found = findNodeById(node.children, id);
      if (found) return found;
    }
  }
  return null;
}

function handleOpenPidTree() {
  pidDialogVisible.value = true;
  nextTick(() => {
    if (pidTreeRef.value && formData.pid) {
      pidTreeRef.value.setCurrentKey(formData.pid);
    }
  });
}

function handlePidNodeClick(data: PrivilegeDepartment) {
  if (data.id === formData.id) return;
  formData.pid = data.id;
  selectedParentName.value = data.name ?? '';
  pidDialogVisible.value = false;
}

function handleClearPid() {
  formData.pid = undefined;
  selectedParentName.value = '';
}

async function loadCompanies() {
  companiesLoading.value = true;
  try {
    const res = (await getCompanyPageApi(1, 999)) as any;
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

function showAddDialog() {
  if (!selectedCompany.value) {
    ElMessage.warning('请先选择一家公司');
    return;
  }
  isEditMode.value = false;
  delete formData.id;
  formData.name = '';
  formData.code = '';
  formData.companyId = selectedCompany.value.id;
  formData.pid = undefined;
  formData.orderNo = 0;
  formData.status = 0;
  formData.nature = 0;
  selectedParentName.value = '';
  dialogVisible.value = true;
}

function handleAddChild(row: PrivilegeDepartment) {
  if (!selectedCompany.value) {
    ElMessage.warning('请先选择一家公司');
    return;
  }
  isEditMode.value = false;
  delete formData.id;
  formData.name = '';
  formData.code = '';
  formData.companyId = selectedCompany.value.id;
  formData.pid = row.id;
  formData.orderNo = 0;
  formData.status = 0;
  formData.nature = 0;
  selectedParentName.value = row.name ?? '';
  dialogVisible.value = true;
}

function handleEdit(row: PrivilegeDepartment) {
  debugger;
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    companyId: row.companyId,
    pid: row.pid,
    orderNo: row.orderNo,
    status: row.status ?? 0,
    nature: row.nature ?? 0,
  });
  selectedParentName.value = row.parentName ?? '';
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updateDepartmentApi({ ...formData });
      ElMessage.success('部门更新成功');
    } else {
      await createDepartmentApi({ ...formData });
      ElMessage.success('部门创建成功');
    }
    dialogVisible.value = false;
    loadDepartments();
  } catch {
    // validation or API error
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: string, name: string) {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门 "${name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteDepartmentApi(id);
    ElMessage.success('部门删除成功');
    loadDepartments();
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  loadCompanies();
});
</script>

<template>
  <Page auto-content-height>
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
                <ElButton type="primary" @click="showAddDialog">
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
                      @click="handleEdit(scope.row as PrivilegeDepartment)"
                    >
                      编辑
                    </ElButton>
                    <ElButton
                      type="danger"
                      size="small"
                      @click="
                        handleDelete(
                          (scope.row as PrivilegeDepartment).id!,
                          (scope.row as PrivilegeDepartment).name!,
                        )
                      "
                    >
                      删除
                    </ElButton>
                  </template>
                </ElTableColumn>
              </ElTable>
            </div>
          </ElCard>
        </div>
      </div>

      <ElDialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑部门' : '新增部门'"
        width="500px"
        :close-on-click-modal="false"
      >
        <ElForm
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          label-position="left"
        >
          <ElFormItem label="部门名称" prop="name">
            <ElInput v-model="formData.name" placeholder="请输入部门名称" />
          </ElFormItem>
          <ElFormItem label="部门编码" prop="code">
            <ElInput v-model="formData.code" placeholder="请输入部门编码" />
          </ElFormItem>
          <ElFormItem label="上级部门">
            <ElInput
              :model-value="selectedParentName || ''"
              placeholder="点击选择上级部门"
              readonly
              clearable
              class="pid-trigger"
              @click="handleOpenPidTree"
              @clear="handleClearPid"
            >
              <template #prepend>
                <ElIcon><IconifyIcon icon="lucide:folder-tree" /></ElIcon>
              </template>
              <template #append>
                <ElButton @click.stop="handleOpenPidTree">选择</ElButton>
              </template>
            </ElInput>
          </ElFormItem>
          <ElFormItem label="排序号">
            <ElInputNumber
              v-model="formData.orderNo"
              :min="0"
              :max="999"
              style="width: 100%"
            />
          </ElFormItem>
          <ElFormItem label="状态">
            <ElSelect v-model="formData.status" placeholder="请选择状态">
              <ElOption :value="0" label="启用" />
              <ElOption :value="1" label="禁用" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="部门性质">
            <ElSelect v-model="formData.nature" placeholder="请选择部门性质">
              <ElOption :value="0" label="部门" />
              <ElOption :value="1" label="组" />
            </ElSelect>
          </ElFormItem>
        </ElForm>
        <template #footer>
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEditMode ? '更新' : '创建' }}
          </ElButton>
        </template>
      </ElDialog>

      <ElDialog
        v-model="pidDialogVisible"
        title="选择上级部门"
        width="480px"
        :close-on-click-modal="false"
        class="pid-dialog"
      >
        <div class="pid-tree-container">
          <ElTree
            ref="pidTreeRef"
            :data="tableData"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handlePidNodeClick"
          >
            <template #default="{ node }">
              <div class="pid-node">
                <ElIcon class="pid-node-icon">
                  <IconifyIcon icon="lucide:folder-tree" />
                </ElIcon>
                <span class="pid-node-label">{{ node.label }}</span>
              </div>
            </template>
          </ElTree>
        </div>
      </ElDialog>
    </div>
  </Page>
</template>

<style scoped>
.page-container {
  @apply bg-background-deep;

  display: flex;
  flex-direction: column;
  /*height: calc(100vh - 100px);*/
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

.pid-dialog :deep(.el-dialog) {
  overflow: hidden;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgb(0 0 0 / 12%);
}

.pid-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin: 0;
}

.pid-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.pid-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

.pid-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  font-size: 16px;
  color: hsl(var(--muted-foreground));
}

.pid-dialog :deep(.el-dialog__body) {
  padding: 16px 24px;
}

.pid-tree-container {
  max-height: 340px;
  overflow-y: auto;
}

.pid-tree-container::-webkit-scrollbar {
  width: 4px;
}

.pid-tree-container::-webkit-scrollbar-thumb {
  background: hsl(var(--border));
  border-radius: 2px;
}

.pid-tree-container :deep(.el-tree-node__content) {
  height: 36px;
  padding: 0 8px;
  border-radius: 6px;
  transition: background 0.15s;
}

.pid-tree-container :deep(.el-tree-node__content:hover) {
  background: hsl(var(--accent));
}

.pid-tree-container :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: hsl(var(--primary) / 10%);
}

.pid-node {
  display: flex;
  gap: 6px;
  align-items: center;
  width: 100%;
}

.pid-node-icon {
  flex-shrink: 0;
  font-size: 15px;
  color: hsl(var(--primary) / 65%);
}

.pid-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  color: hsl(var(--foreground));
  white-space: nowrap;
}

.pid-tree-container :deep(.el-tree-node.is-current > .el-tree-node__content .pid-node-label) {
  font-weight: 500;
  color: hsl(var(--primary));
}

.table-toolbar {
  display: flex;
  flex-shrink: 0;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.pid-trigger {
  cursor: pointer;
}

.pid-trigger :deep(.el-input__wrapper) {
  cursor: pointer;
  border-radius: 8px 0 0 8px;
}

.pid-trigger :deep(.el-input__inner) {
  cursor: pointer;
}

.pid-trigger :deep(.el-input-group__prepend) {
  background: transparent;
  border: none;
  padding: 0 0 0 12px;
  color: hsl(var(--muted-foreground));
  font-size: 15px;
}

.pid-trigger :deep(.el-input-group__append) {
  background: hsl(var(--primary));
  border-color: hsl(var(--primary));
  border-radius: 0 8px 8px 0;
  padding: 0 16px;
}

.pid-trigger :deep(.el-input-group__append .el-button) {
  color: hsl(var(--primary-foreground));
  border: none;
  background: transparent;
  font-size: 13px;
  margin: 0;
  padding: 0;
}
</style>
