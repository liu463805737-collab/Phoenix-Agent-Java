<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { AclPvalueItem, AclRecord, AclTreeNode, PrivilegeRole } from '#/api';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useVbenForm } from '#/adapter/form';

import {
  ElButton,
  ElCard,
  ElCheckbox,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElTree,
} from 'element-plus';

import {
  createRoleApi,
  deleteRoleApi,
  getAclsByReleaseIdApi,
  getRoleAclsApi,
  getRolePageApi,
  saveAllAclApi,
  saveModuleAclApi,
  updateRoleApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<PrivilegeRole[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const [FilterForm] = useVbenForm({
  commonConfig: { componentProps: { clearable: true } },
  layout: 'inline',
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  actionButtonsReverse: true,
  submitButtonOptions: { content: '查询' },
  schema: [
    {
      fieldName: 'name',
      component: 'Input',
      label: '角色名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入角色名称' },
    },
    {
      fieldName: 'sn',
      component: 'Input',
      label: '角色标识',
      componentProps: { placeholder: '请输入角色标识' },
    },
  ],
  handleSubmit: (values) => {
    page.value = 1;
    loadData(values);
  },
  handleReset: () => {
    page.value = 1;
    loadData({});
  },
});

const formData = reactive<Record<string, any>>({
  name: '',
  sn: '',
});

const formRules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  sn: [{ required: true, message: '请输入角色标识', trigger: 'blur' }],
};

const assignDialogVisible = ref(false);
const aclTreeData = ref<AclTreeNode[]>([]);
const currentRole = ref<null | PrivilegeRole>(null);
const aclLoading = ref(false);
const treeRef = ref<InstanceType<typeof ElTree>>();
const existingAclMap = ref(new Map<string, AclRecord>());

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getRolePageApi(
      page.value,
      pageSize.value,
      params,
    )) as any;
    const pageResult = res?.data || res;
    tableData.value = pageResult?.records || [];
    total.value = pageResult?.totalRow || 0;
  } catch {
    tableData.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function showAddDialog() {
  isEditMode.value = false;
  formData.name = '';
  formData.sn = '';
  dialogVisible.value = true;
}

function handleEdit(row: PrivilegeRole) {
  isEditMode.value = true;
  formData.name = row.name;
  formData.sn = row.sn;
  formData.id = row.id;
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updateRoleApi({ ...formData });
      ElMessage.success('角色更新成功');
    } else {
      await createRoleApi({ ...formData });
      ElMessage.success('角色创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch {
    // validation or API error
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: string, name: string) {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteRoleApi(id);
    ElMessage.success('角色删除成功');
    loadData();
  } catch {
    // cancelled or error
  }
}

async function handleAssignMenu(row: PrivilegeRole) {
  currentRole.value = row;
  assignDialogVisible.value = true;
  aclLoading.value = true;
  try {
    const [treeRes, aclRes] = await Promise.all([
      getRoleAclsApi(row.id!),
      getAclsByReleaseIdApi(row.id!),
    ]);
    const tree = ((treeRes as any)?.data || treeRes || []) as AclTreeNode[];
    aclTreeData.value = tree;
    assignLevels(tree);
    const aclList = ((aclRes as any)?.data || aclRes || []) as AclRecord[];
    const map = new Map<string, AclRecord>();
    for (const item of aclList) {
      if (item.moduleId) map.set(item.moduleId, item);
    }
    existingAclMap.value = map;
  } catch {
    aclTreeData.value = [];
    ElMessage.error('获取菜单权限失败');
  } finally {
    aclLoading.value = false;
  }
}

const treeProps = {
  children: 'children',
  label: 'name',
};

function calcAclState(pvalues: AclPvalueItem[]): string {
  let state = 0;
  for (const pv of pvalues) {
    if (pv.enabled) {
      state |= 1 << pv.position;
    }
  }
  return String(state);
}

function updateNodeState(data: AclTreeNode) {
  data.state = calcAclState(data.pvalues);
}

function handlePvalueChange(data: AclTreeNode) {
  updateNodeState(data);
  const role = currentRole.value;
  if (!role?.id || !role?.sn) return;
  const aclState = Number(calcAclState(data.pvalues));
  saveModuleAclApi({
    releaseId: role.id,
    releaseSn: role.sn,
    moduleId: data.id,
    moduleSn: data.sn,
    aclState,
    status: aclState > 0 ? 'check' : 'uncheck',
  });
}

function assignLevels(nodes: AclTreeNode[], level = 0) {
  for (const node of nodes) {
    (node as any)._level = level;
    if (node.children?.length) {
      assignLevels(node.children, level + 1);
    }
  }
}

function traversePvalues(nodes: AclTreeNode[]): AclPvalueItem[] {
  const result: AclPvalueItem[] = [];
  for (const node of nodes) {
    if (node.pvalues?.length) result.push(...node.pvalues);
    if (node.children?.length) result.push(...traversePvalues(node.children));
  }
  return result;
}

const allPvalues = computed(() => traversePvalues(aclTreeData.value));

const headerAllSelected = computed(() => {
  const list = allPvalues.value;
  return list.length > 0 && list.every((pv) => pv.enabled);
});

const headerIndeterminate = computed(() => {
  const list = allPvalues.value;
  return list.some((pv) => pv.enabled) && !headerAllSelected.value;
});

function handleHeaderSelectAll(val: string | number | boolean) {
  const checked = !!val;
  function traverse(nodes: AclTreeNode[]) {
    for (const node of nodes) {
      if (node.pvalues?.length) {
        node.pvalues.forEach((pv) => {
          pv.enabled = checked;
        });
        updateNodeState(node);
      }
      if (node.children?.length) {
        traverse(node.children);
      }
    }
  }
  traverse(aclTreeData.value);
  const roleId = currentRole.value?.id;
  if (roleId) {
    saveAllAclApi(roleId, checked);
  }
}

async function handleSaveAcl() {
  assignDialogVisible.value = false;
}

async function handleCancelAcl() {
  assignDialogVisible.value = false;
}

function handlePageChange(val: number) {
  page.value = val;
  loadData();
}

function handleSizeChange(val: number) {
  pageSize.value = val;
  page.value = 1;
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page>
    <div class="page-container">
      <ElCard class="table-section" :body-style="{ padding: '20px' }">
        <FilterForm />

        <div class="table-toolbar">
          <ElButton type="primary" @click="showAddDialog">
            <ElIcon><IconifyIcon icon="lucide:plus" /></ElIcon>
            新增
          </ElButton>
          <ElButton @click="loadData">
            <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
            刷新
          </ElButton>
        </div>

        <ElTable
          :data="tableData"
          style="width: 100%"
          border
          stripe
          v-loading="loading"
          empty-text="暂无数据"
        >
          <ElTableColumn prop="name" label="角色名称" min-width="150" resizable />
          <ElTableColumn prop="sn" label="角色标识" width="150" resizable />
          <ElTableColumn prop="createTime" label="创建时间" width="180" resizable />
          <ElTableColumn label="操作" width="280" fixed="right">
            <template #default="scope">
              <ElButton
                type="primary"
                size="small"
                @click="handleAssignMenu(scope.row as PrivilegeRole)"
              >
                分配权限
              </ElButton>
              <ElButton
                type="warning"
                size="small"
                @click="handleEdit(scope.row as PrivilegeRole)"
              >
                编辑
              </ElButton>
              <ElButton
                type="danger"
                size="small"
                @click="
                  handleDelete(
                    (scope.row as PrivilegeRole).id!,
                    (scope.row as PrivilegeRole).name!,
                  )
                "
              >
                删除
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pagination-wrapper">
          <ElPagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </ElCard>

      <ElDialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑角色' : '新增角色'"
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
          <ElFormItem label="角色名称" prop="name">
            <ElInput v-model="formData.name" placeholder="请输入角色名称" />
          </ElFormItem>
          <ElFormItem label="角色标识" prop="sn">
            <ElInput v-model="formData.sn" placeholder="请输入角色标识" />
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
        v-model="assignDialogVisible"
        :title="`分配权限 - ${currentRole?.name || ''}`"
        width="1000px"
        :close-on-click-modal="false"
      >
        <div v-loading="aclLoading" class="acl-body">
          <div v-if="aclTreeData.length === 0 && !aclLoading" class="acl-empty">
            暂无菜单数据
          </div>
          <div v-else class="acl-tree-wrapper">
            <div class="acl-header">
              <div class="acl-header-col-name">菜单名称</div>
              <div class="acl-header-col-checkall">
                <ElCheckbox
                  :model-value="headerAllSelected"
                  :indeterminate="headerIndeterminate"
                  @change="handleHeaderSelectAll"
                >
                  {{ headerAllSelected ? '取消全选' : '全选' }}
                </ElCheckbox>
              </div>
              <div class="acl-header-col-pvalues">操作权限</div>
            </div>
            <ElTree
              ref="treeRef"
              :data="aclTreeData"
              :props="treeProps"
              node-key="id"
              :default-expand-all="true"
            >
            <template #default="{ data }: { data: AclTreeNode }">
              <div class="acl-node">
                <div class="acl-node-col-name" :style="{ paddingLeft: ((data as any)._level || 0) * 24 + 'px' }">
                  <ElIcon class="acl-node-icon">
                    <IconifyIcon
                      :icon="data.image || (data.type === '0' ? 'lucide:folder' : 'lucide:file-text')"
                    />
                  </ElIcon>
                  <span class="acl-node-name">{{ data.name }}</span>
                </div>
                <div v-if="data.pvalues?.length" class="acl-node-col-checkall">
                  <ElCheckbox
                    :model-value="data.pvalues.every((pv) => pv.enabled)"
                    :indeterminate="
                      data.pvalues.some((pv) => pv.enabled) &&
                      !data.pvalues.every((pv) => pv.enabled)
                    "
                    @click.stop
                    @change="(val: string | number | boolean) => {
                      data.pvalues.forEach((pv) => { pv.enabled = !!val; });
                      updateNodeState(data);
                      const role = currentRole.value;
                      if (!role?.id || !role?.sn) return;
                      const aclState = Number(calcAclState(data.pvalues));
                      saveModuleAclApi({
                        releaseId: role.id,
                        releaseSn: role.sn,
                        systemSn: '',
                        moduleId: data.id,
                        moduleSn: data.sn,
                        aclState,
                        status: aclState > 0 ? 'check' : 'uncheck',
                      });
                    }"
                  >
                    全选
                  </ElCheckbox>
                </div>
                <div v-else class="acl-node-col-checkall" />
                <div v-if="data.pvalues?.length" class="acl-node-col-pvalues">
                  <ElCheckbox
                    v-for="pv in data.pvalues"
                    :key="pv.pvalueId"
                    :model-value="pv.enabled"
                    @click.stop
                    @change="(val: string | number | boolean) => {
                      pv.enabled = !!val;
                      handlePvalueChange(data);
                    }"
                    class="acl-node-pvalue"
                  >
                    {{ pv.pvalueName || pv.name }}
                  </ElCheckbox>
                </div>
              </div>
            </template>
          </ElTree>
        </div>
        </div>
        <template #footer>
          <ElButton @click="handleCancelAcl">取消</ElButton>
          <ElButton type="primary" @click="handleSaveAcl">保存</ElButton>
        </template>
      </ElDialog>
    </div>
  </Page>
</template>

<style scoped>
.page-container {
  @apply bg-background-deep;
}

.table-section {
  border-radius: 12px;
}

.table-toolbar {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}

.acl-body {
  max-height: 600px;
  overflow-y: auto;
}

.acl-header {
  display: grid;
  grid-template-columns: 220px 100px 1fr;
  align-items: center;
  padding: 0.5rem 0.5rem 0.5rem 1.5rem;
  margin-bottom: 0.25rem;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  border-bottom: 1px solid #e5e7eb;
}

.acl-header-col-checkall {
  text-align: left;
}

.acl-empty {
  padding: 2rem 0;
  color: #999;
  text-align: center;
}

.acl-body :deep(.el-tree-node) {
  padding-left: 0 !important;
}

.acl-body :deep(.el-tree-node__content) {
  height: auto;
  padding: 0.35rem 0.5rem;
  padding-left: 8px !important;
}

.acl-node {
  display: grid;
  grid-template-columns: 220px 100px 1fr;
  gap: 0.5rem;
  align-items: center;
  width: 100%;
  min-width: 0;
}

.acl-node-col-name {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  min-width: 0;
}

.acl-node-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  font-size: 16px;
}

.acl-node-name {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.acl-node-col-checkall {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.acl-node-col-pvalues {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 0.75rem;
  align-items: center;
}

.acl-node-pvalue {
  font-size: 13px;
}
</style>
