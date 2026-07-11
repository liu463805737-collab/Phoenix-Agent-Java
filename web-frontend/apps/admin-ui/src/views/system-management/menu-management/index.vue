<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { AclPvalueItem, PrivilegeModule } from '#/api';

import { onMounted, reactive, ref } from 'vue';

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
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElSelect,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
  ElTree,
} from 'element-plus';

import {
  createModuleApi,
  deleteModuleApi,
  getModuleTreeApi,
  getPvaluesBySystemApi,
  updateModuleApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<PrivilegeModule[]>([]);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const formData = reactive<Record<string, any>>({
  name: '',
  url: '',
  sn: '',
  component: '',
  image: '',
  orderNo: 0,
  type: '0',
  isShow: 1,
  pid: '',
  state: 15,
});

const formRules: FormRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  sn: [{ required: true, message: '请输入菜单标识', trigger: 'blur' }],
};

const pidDialogVisible = ref(false);
const pidTreeLoading = ref(false);
const selectedParentName = ref('');
const pidTreeRef = ref();

function findNodeById(
  nodes: PrivilegeModule[],
  id: string,
): PrivilegeModule | null {
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
  pidTreeLoading.value = true;
  try {
    const tree = tableData.value;
    selectedParentName.value = '';
    if (formData.pid) {
      const node = findNodeById(tree, formData.pid);
      if (node) {
        selectedParentName.value = node.name ?? '';
      }
    }
  } finally {
    pidTreeLoading.value = false;
  }
}

function handlePidNodeClick(data: PrivilegeModule) {
  if (data.id === formData.id) return;
  formData.pid = data.id ?? '';
  selectedParentName.value = data.name ?? '';
  pidDialogVisible.value = false;
}

function handleClearPid() {
  formData.pid = '';
  selectedParentName.value = '';
}

const assignDialogVisible = ref(false);
const assignModule = ref<PrivilegeModule | null>(null);
const assignPvalueList = ref<AclPvalueItem[]>([]);
const assignPvalueLoading = ref(false);

const formPvalueList = ref<AclPvalueItem[]>([]);
const formPvalueLoading = ref(false);

async function loadFormPvalues(state?: string) {
  formPvalueLoading.value = true;
  try {
    const res = (await getPvaluesBySystemApi()) as any;
    const list = (res?.data || res || []) as AclPvalueItem[];
    formPvalueList.value = list;
    initPvalueState(list, state);
  } catch {
    formPvalueList.value = [];
  } finally {
    formPvalueLoading.value = false;
  }
}

function calcAclState(pvalues: AclPvalueItem[]): string {
  let state = 0;
  for (let i = 0; i < pvalues.length; i++) {
    if (pvalues[i].enabled) {
      state |= 1 << i;
    }
  }
  return String(state);
}

function initPvalueState(pvalues: AclPvalueItem[], state?: string) {
  if (!state) {
    pvalues.forEach((pv) => (pv.enabled = false));
    return;
  }
  const stateNum = Number(state);
  for (let i = 0; i < pvalues.length; i++) {
    pvalues[i].enabled = !!(stateNum & (1 << i));
  }
}

async function handleAssignModule(row: PrivilegeModule) {
  assignModule.value = row;
  assignDialogVisible.value = true;
  assignPvalueLoading.value = true;
  try {
    const res = (await getPvaluesBySystemApi()) as any;
    const list = (res?.data || res || []) as AclPvalueItem[];
    assignPvalueList.value = list;
    initPvalueState(list, row.state);
  } catch {
    assignPvalueList.value = [];
  } finally {
    assignPvalueLoading.value = false;
  }
}

function handleAssignPvalueToggle(pv: AclPvalueItem) {
  pv.enabled = !pv.enabled;
}

async function handleConfirmAssign() {
  if (!assignModule.value?.id) return;
  const state = calcAclState(assignPvalueList.value);
  try {
    await updateModuleApi({ id: assignModule.value.id, state });
    ElMessage.success('权限分配成功');
    assignDialogVisible.value = false;
    loadData();
  } catch {
    ElMessage.error('权限分配失败');
  }
}

function filterTree(
  nodes: PrivilegeModule[],
  keyword: string,
): PrivilegeModule[] {
  if (!keyword) return nodes;
  return nodes
    .map((node) => {
      const matchedChildren = node.children
        ? filterTree(node.children, keyword)
        : [];
      if (
        matchedChildren.length > 0 ||
        (node.name && node.name.includes(keyword)) ||
        (node.sn && node.sn.includes(keyword))
      ) {
        return {
          ...node,
          children:
            matchedChildren.length > 0 ? matchedChildren : node.children,
        };
      }
      return null;
    })
    .filter(Boolean) as PrivilegeModule[];
}

const filteredTableData = ref<PrivilegeModule[]>([]);

const [FilterForm] = useVbenForm({
  commonConfig: { componentProps: { clearable: true } },
  layout: 'inline',
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
  actionButtonsReverse: true,
  submitButtonOptions: { content: '查询' },
  schema: [
    {
      fieldName: 'keyword',
      component: 'Input',
      label: '搜索',
      labelWidth: 40,
      componentProps: { placeholder: '输入菜单名称或标识筛选' },
    },
  ],
  handleSubmit: (values) => {
    const keyword = values.keyword || '';
    filteredTableData.value = keyword
      ? filterTree(tableData.value, keyword)
      : tableData.value;
  },
  handleReset: () => {
    filteredTableData.value = tableData.value;
  },
});

async function loadData() {
  loading.value = true;
  try {
    const res = (await getModuleTreeApi()) as any;
    const tree = res?.data || res || [];
    tableData.value = tree;
    filteredTableData.value = tree;
  } catch {
    tableData.value = [];
    filteredTableData.value = [];
  } finally {
    loading.value = false;
  }
}

function showAddDialog() {
  isEditMode.value = false;
  delete formData.id;
  formData.name = '';
  formData.url = '';
  formData.sn = '';
  formData.component = '';
  formData.image = '';
  formData.orderNo = 0;
  formData.type = '0';
  formData.isShow = 1;
  formData.pid = '';
  formData.state = '';
  selectedParentName.value = '';
  dialogVisible.value = true;
  loadFormPvalues();
}

function handleAddChild(row: PrivilegeModule) {
  isEditMode.value = false;
  delete formData.id;
  formData.name = '';
  formData.url = '';
  formData.sn = '';
  formData.component = '';
  formData.image = '';
  formData.orderNo = 0;
  formData.type = '0';
  formData.isShow = 1;
  formData.pid = row.id ?? '';
  formData.state = '';
  selectedParentName.value = row.name ?? '';
  dialogVisible.value = true;
  loadFormPvalues();
}

function handleEdit(row: PrivilegeModule) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    url: row.url,
    sn: row.sn,
    component: row.component,
    image: row.image,
    orderNo: row.orderNo,
    type: row.type,
    isShow: row.isShow,
    pid: row.pid,
    state: row.state,
  });
  selectedParentName.value = row.pid
    ? (findNodeById(tableData.value, row.pid)?.name ?? '')
    : '';
  dialogVisible.value = true;
  loadFormPvalues(row.state);
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    const data = {
      ...formData,
      state: calcAclState(formPvalueList.value),
    };
    if (isEditMode.value) {
      await updateModuleApi(data);
      ElMessage.success('菜单更新成功');
    } else {
      await createModuleApi(data);
      ElMessage.success('菜单创建成功');
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
      `确定要删除菜单 "${name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteModuleApi(id);
    ElMessage.success('菜单删除成功');
    loadData();
  } catch {
    // cancelled or error
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page auto-content-height>
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
          :data="filteredTableData"
          style="width: 100%; height: auto"
          stripe
          v-loading="loading"
          empty-text="暂无数据"
          row-key="id"
          default-expand-all
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        >
          <ElTableColumn prop="name" label="菜单名称" min-width="150" />
          <ElTableColumn prop="sn" label="菜单标识" width="140" />
          <ElTableColumn
            prop="url"
            label="路由地址"
            width="200"
            show-overflow-tooltip
          />
          <ElTableColumn
            prop="component"
            label="组件路径"
            width="220"
            show-overflow-tooltip
          />
          <ElTableColumn prop="image" label="图标" width="100">
            <template #default="scope">
              <ElIcon v-if="(scope.row as PrivilegeModule).image">
                <IconifyIcon
                  :icon="(scope.row as PrivilegeModule).image!"
                  style="font-size: 18px"
                />
              </ElIcon>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="orderNo" label="排序" width="70" />
          <ElTableColumn label="类型" width="80">
            <template #default="scope">
              <ElTag
                :type="
                  (scope.row as PrivilegeModule).type === '0'
                    ? 'primary'
                    : 'success'
                "
                size="small"
              >
                {{
                  (scope.row as PrivilegeModule).type === '0' ? '目录' : '菜单'
                }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="是否显示" width="90">
            <template #default="scope">
              <ElTag
                :type="
                  (scope.row as PrivilegeModule).isShow === 1
                    ? 'success'
                    : 'danger'
                "
                size="small"
              >
                {{
                  (scope.row as PrivilegeModule).isShow === 1 ? '显示' : '隐藏'
                }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="状态" width="80">
            <template #default="scope">
              <ElTag
                :type="
                  (scope.row as PrivilegeModule).status === 1
                    ? 'success'
                    : 'danger'
                "
                size="small"
              >
                {{
                  (scope.row as PrivilegeModule).status === 1 ? '启用' : '禁用'
                }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="权限状态" width="200">
            <template #default="scope">
              <div v-if="(scope.row as PrivilegeModule).pvalues?.length" class="acl-state-cell">
                <ElTag
                  v-for="pv in (scope.row as PrivilegeModule).pvalues!"
                  :key="pv.pvalueId"
                  :type="pv.enabled ? 'success' : 'info'"
                  size="small"
                >
                  {{ pv.name || pv.pvalueName }}
                </ElTag>
              </div>
              <span v-else class="acl-state-empty">-</span>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="200" fixed="right">
            <template #default="scope">
              <ElButton
                type="primary"
                size="small"
                @click="handleAddChild(scope.row as PrivilegeModule)"
              >
                新增
              </ElButton>
              <ElButton
                type="warning"
                size="small"
                @click="handleEdit(scope.row as PrivilegeModule)"
              >
                编辑
              </ElButton>
              <ElButton
                type="danger"
                size="small"
                @click="
                  handleDelete(
                    (scope.row as PrivilegeModule).id!,
                    (scope.row as PrivilegeModule).name!,
                  )
                "
              >
                删除
              </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>

      <ElDialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑菜单' : formData.pid ? '新增下级菜单' : '新增菜单'"
        width="600px"
        :close-on-click-modal="false"
      >
        <ElForm
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="100px"
          label-position="left"
        >
          <ElFormItem label="菜单名称" prop="name">
            <ElInput v-model="formData.name" placeholder="请输入菜单名称" />
          </ElFormItem>
          <ElFormItem label="菜单标识" prop="sn">
            <ElInput
              v-model="formData.sn"
              placeholder="请输入菜单标识，如 MenuName"
            />
          </ElFormItem>
          <ElFormItem label="路由地址">
            <ElInput v-model="formData.url" placeholder="请输入路由地址" />
          </ElFormItem>
          <ElFormItem label="组件路径">
            <ElInput
              v-model="formData.component"
              placeholder="如 #/views/xxx/index.vue"
            />
          </ElFormItem>
          <ElFormItem label="上级菜单">
            <div class="pid-selector" @click="handleOpenPidTree">
              <ElInput
                :model-value="selectedParentName || (formData.pid ? formData.pid : '')"
                placeholder="点击选择上级菜单"
                :readonly="true"
              />
              <ElIcon
                v-if="formData.pid"
                class="pid-clear"
                @click.stop="handleClearPid"
              >
                <IconifyIcon icon="lucide:x" />
              </ElIcon>
            </div>
          </ElFormItem>
          <ElFormItem label="图标">
            <ElInput v-model="formData.image" placeholder="如 lucide:menu" />
          </ElFormItem>
          <ElFormItem label="权限值">
            <div v-loading="formPvalueLoading" class="pvalue-group">
              <div v-if="formPvalueList.length === 0 && !formPvalueLoading" class="assign-empty">
                暂无权限数据
              </div>
              <ElCheckbox
                v-if="formPvalueList.length > 1"
                :model-value="formPvalueList.every((pv) => pv.enabled)"
                :indeterminate="
                  formPvalueList.some((pv) => pv.enabled) &&
                  !formPvalueList.every((pv) => pv.enabled)
                "
                @change="(val) => {
                  formPvalueList.forEach((pv) => { pv.enabled = !!val; });
                }"
              >
                全选
              </ElCheckbox>
              <ElCheckbox
                v-for="pv in formPvalueList"
                :key="pv.pvalueId"
                :model-value="pv.enabled"
                @change="pv.enabled = !pv.enabled"
              >
                {{ pv.name || pv.pvalueName }}
              </ElCheckbox>
            </div>
          </ElFormItem>
          <ElFormItem label="排序号">
            <ElInputNumber
              v-model="formData.orderNo"
              :min="0"
              :max="999"
              style="width: 100%"
            />
          </ElFormItem>
          <ElFormItem label="类型">
            <ElSelect v-model="formData.type" placeholder="请选择类型">
              <ElOption label="目录" value="0" />
              <ElOption label="菜单" value="1" />
              <ElOption label="按钮" value="2" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="是否显示">
            <ElSwitch
              v-model="formData.isShow"
              :active-value="1"
              :inactive-value="0"
            />
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
        title="选择上级菜单"
        width="400px"
        :close-on-click-modal="false"
      >
        <div v-loading="pidTreeLoading" class="pid-tree-body">
          <ElTree
            ref="pidTreeRef"
            :data="tableData"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handlePidNodeClick"
          />
        </div>
      </ElDialog>

      <ElDialog
        v-model="assignDialogVisible"
        title="分配权限"
        width="500px"
        :close-on-click-modal="false"
      >
        <div v-loading="assignPvalueLoading" class="assign-body">
          <div v-if="assignPvalueList.length === 0 && !assignPvalueLoading" class="assign-empty">
            暂无权限数据
          </div>
          <div v-else class="pvalue-group">
            <ElCheckbox
              v-if="assignPvalueList.length > 1"
              :model-value="assignPvalueList.every((pv) => pv.enabled)"
              :indeterminate="
                assignPvalueList.some((pv) => pv.enabled) &&
                !assignPvalueList.every((pv) => pv.enabled)
              "
              @change="(val: boolean) => {
                assignPvalueList.forEach((pv) => { pv.enabled = val; });
              }"
            >
              全选
            </ElCheckbox>
            <ElCheckbox
              v-for="pv in assignPvalueList"
              :key="pv.pvalueId"
              :model-value="pv.enabled"
              @change="handleAssignPvalueToggle(pv)"
            >
              {{ pv.name || pv.pvalueName }}
            </ElCheckbox>
          </div>
        </div>
        <template #footer>
          <ElButton @click="assignDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleConfirmAssign">确认</ElButton>
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

.pvalue-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem 1rem;
  align-items: center;
  padding: 0.5rem 0;
}

.acl-state-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.acl-state-empty {
  color: #999;
}

.assign-body {
  min-height: 80px;
}

.assign-empty {
  padding: 2rem 0;
  color: #999;
  text-align: center;
}

.pid-selector {
  position: relative;
  width: 100%;
  cursor: pointer;
}

.pid-selector :deep(.el-input__wrapper) {
  cursor: pointer;
}

.pid-selector :deep(.el-input__inner) {
  cursor: pointer;
}

.pid-clear {
  position: absolute;
  top: 50%;
  right: 8px;
  z-index: 2;
  color: #999;
  cursor: pointer;
  transform: translateY(-50%);
}

.pid-clear:hover {
  color: #666;
}

.pid-tree-body {
  max-height: 400px;
  overflow-y: auto;
}
</style>
