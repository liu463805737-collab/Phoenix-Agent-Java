<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';
import type {
  AccountInfo,
  GroupAgentAssignment,
  GroupAgentInfo,
  GroupInfo,
} from '#/api';

import { nextTick, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useVbenForm } from '#/adapter/form';

import {
  ElButton,
  ElCard,
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
  ElTag,
} from 'element-plus';

import {
  createAccountGroupInfoApi,
  createGroupAgentInfoApi,
  createGroupInfoApi,
  deleteGroupAgentInfoByGroupAndAgentApi,
  deleteGroupInfoApi,
  getGroupAgentInfoByGroupApi,
  getGroupAgentInfoListApi,
  getGroupInfoPageApi,
  getUnGroupPageByGroupId,
  updateGroupInfoApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<GroupInfo[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);

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
      label: '组名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入组名称' },
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

const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);
const formData = reactive<Record<string, any>>({
  sn: '',
  name: '',
  description: '',
});

const formRules: FormRules = {
  sn: [{ required: true, message: '请输入组编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入组名称', trigger: 'blur' }],
};

const assignPeopleDialogVisible = ref(false);
const assignAgentDialogVisible = ref(false);
const currentGroupId = ref('');
const currentGroupName = ref('');

const allAccounts = ref<AccountInfo[]>([]);
const allAgents = ref<GroupAgentInfo[]>([]);
const selectedAccountIds = ref<string[]>([]);
const selectedAgentIds = ref<string[]>([]);
const assignedAgentIds = ref<Set<string>>(new Set());
const assignLoading = ref(false);
const peopleTableRef = ref();
const agentTableRef = ref();

const assignPeoplePage = ref(1);
const assignPeoplePageSize = ref(10);
const assignPeopleTotal = ref(0);
const assignPeopleName = ref('');
const assignPeopleCode = ref('');
const assignPeopleAccountMap = ref<Map<string, AccountInfo>>(new Map());

const removeAgentDialogVisible = ref(false);
const removeAgentLoading = ref(false);
const removeAgentGroupId = ref('');
const removeAgentGroupName = ref('');
const assignedAgents = ref<GroupAgentAssignment[]>([]);
const selectedRemoveAgentIds = ref<string[]>([]);

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getGroupInfoPageApi(
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
  formData.sn = '';
  formData.name = '';
  formData.description = '';
  delete formData.id;
  dialogVisible.value = true;
}

function handleEdit(row: GroupInfo) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    sn: row.sn,
    name: row.name,
    description: row.description,
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updateGroupInfoApi({ ...formData });
      ElMessage.success('组更新成功');
    } else {
      await createGroupInfoApi({ ...formData });
      ElMessage.success('组创建成功');
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
      `确定要禁用组 "${name}" 吗？此操作不可恢复。`,
      '删除禁用',
      {
        confirmButtonText: '确定禁用',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteGroupInfoApi(id);
    ElMessage.success('组禁用成功');
    loadData();
  } catch {
    // cancelled or error
  }
}

async function loadAssignPeople() {
  assignLoading.value = true;
  try {
    const params: Record<string, any> = { groupId: currentGroupId.value };
    if (assignPeopleName.value) params.realName = assignPeopleName.value;
    if (assignPeopleCode.value) params.code = assignPeopleCode.value;
    const res = (await getUnGroupPageByGroupId(
      assignPeoplePage.value,
      assignPeoplePageSize.value,
      params,
    )) as any;
    const pageResult = res?.data || res;
    allAccounts.value = pageResult?.records || [];
    assignPeopleTotal.value = pageResult?.totalRow || 0;
    for (const row of allAccounts.value) {
      if (row.id) assignPeopleAccountMap.value.set(row.id, row);
    }
    await nextTick();
    peopleTableRef.value?.clearSelection();
  } catch {
    allAccounts.value = [];
  } finally {
    assignLoading.value = false;
  }
}

async function openAssignPeople(groupId: string, groupName: string) {
  currentGroupId.value = groupId;
  currentGroupName.value = groupName;
  assignPeopleName.value = '';
  assignPeopleCode.value = '';
  assignPeopleAccountMap.value = new Map();
  assignPeoplePage.value = 1;
  assignPeoplePageSize.value = 10;
  assignPeopleTotal.value = 0;
  assignPeopleDialogVisible.value = true;
  selectedAccountIds.value = [];
  await loadAssignPeople();
}

async function openAssignAgent(groupId: string, groupName: string) {
  currentGroupId.value = groupId;
  currentGroupName.value = groupName;
  assignLoading.value = true;
  assignAgentDialogVisible.value = true;
  try {
    const res = (await getGroupAgentInfoListApi({ status: 'published' })) as any;
    allAgents.value = res?.data || res || [];
    const assignRes = (await getGroupAgentInfoByGroupApi(groupId)) as any;
    const assignments: GroupAgentAssignment[] =
      assignRes?.data || assignRes || [];
    assignedAgentIds.value = new Set(
      assignments
        .map((a) => a.agentId)
        .filter((id): id is string => id != null),
    );
    selectedAgentIds.value = [...assignedAgentIds.value];
    await nextTick();
    if (agentTableRef.value) {
      agentTableRef.value.clearSelection();
      allAgents.value.forEach((row) => {
        if (
          assignedAgentIds.value.has(row.id ?? '') &&
          row.id != null
        ) {
          agentTableRef.value.toggleRowSelection(row, true);
        }
      });
    }
  } catch {
    allAgents.value = [];
    assignedAgentIds.value = new Set();
    selectedAgentIds.value = [];
  } finally {
    assignLoading.value = false;
  }
}

async function confirmAssignPeople() {
  assignLoading.value = true;
  try {
    const groupId = currentGroupId.value;
    const groupName = currentGroupName.value;
    for (const accountId of selectedAccountIds.value) {
      const account = assignPeopleAccountMap.value.get(accountId);
      await createAccountGroupInfoApi({
        groupId,
        accountId,
        groupName,
        accountName: account?.realName ?? '',
      });
    }
    ElMessage.success('人员分配成功');
    assignPeopleDialogVisible.value = false;
  } catch {
    ElMessage.error('人员分配失败');
  } finally {
    assignLoading.value = false;
  }
}

async function confirmAssignAgent() {
  assignLoading.value = true;
  try {
    const groupId = currentGroupId.value;
    const currentIds = [...assignedAgentIds.value];
    const newIds = selectedAgentIds.value;
    const toRemove = currentIds.filter((id) => !newIds.includes(id));
    const toAdd = newIds.filter((id) => !currentIds.includes(id));
    for (const agentId of toRemove) {
      await deleteGroupAgentInfoByGroupAndAgentApi(groupId, agentId);
    }
    for (const agentId of toAdd) {
      await createGroupAgentInfoApi({ groupId, agentId });
    }
    ElMessage.success('智能体分配成功');
    assignAgentDialogVisible.value = false;
  } catch {
    ElMessage.error('智能体分配失败');
  } finally {
    assignLoading.value = false;
  }
}

async function confirmRemoveAgent() {
  removeAgentLoading.value = true;
  try {
    for (const agentId of selectedRemoveAgentIds.value) {
      await deleteGroupAgentInfoByGroupAndAgentApi(removeAgentGroupId.value, agentId);
    }
    ElMessage.success('智能体移除成功');
    removeAgentDialogVisible.value = false;
    loadData();
  } catch {
    ElMessage.error('智能体移除失败');
  } finally {
    removeAgentLoading.value = false;
  }
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
  <Page auto-content-height >
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
          stripe
          v-loading="loading"
          empty-text="暂无数据"
        >
          <ElTableColumn prop="name" label="组名称" min-width="100" resizable />
          <ElTableColumn prop="sn" label="组编码" min-width="100" resizable />
          <ElTableColumn
            prop="description"
            label="描述"
            min-width="120"
            show-overflow-tooltip
            resizable
          />
          <ElTableColumn prop="createTime" label="创建时间" width="180" resizable />
          <ElTableColumn label="操作" width="440" fixed="right">
            <template #default="scope">
              <ElButton
                type="success"
                size="small"
                @click="
                  openAssignPeople(
                    (scope.row as GroupInfo).id!,
                    (scope.row as GroupInfo).name!,
                  )
                "
              >
                分配人员
              </ElButton>
              <ElButton
                type="warning"
                size="small"
                @click="
                  openAssignAgent(
                    (scope.row as GroupInfo).id!,
                    (scope.row as GroupInfo).name!,
                  )
                "
              >
                分配智能体
              </ElButton>
<!--              <ElButton-->
<!--                type="danger"-->
<!--                size="small"-->
<!--                @click="-->
<!--                  openRemoveAgentDialog(-->
<!--                    (scope.row as GroupInfo).id!,-->
<!--                    (scope.row as GroupInfo).name!,-->
<!--                  )-->
<!--                "-->
<!--              >-->
<!--                移除智能体-->
<!--              </ElButton>-->
              <ElButton
                type="primary"
                size="small"
                @click="handleEdit(scope.row as GroupInfo)"
              >
                编辑
              </ElButton>
              <ElButton
                type="danger"
                size="small"
                @click="
                  handleDelete(
                    (scope.row as GroupInfo).id!,
                    (scope.row as GroupInfo).name!,
                  )
                "
              >
                禁用
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

      <!-- Add/Edit Dialog -->
      <ElDialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑组' : '新增组'"
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
          <ElFormItem label="组编码" prop="sn">
            <ElInput v-model="formData.sn" placeholder="请输入组编码" />
          </ElFormItem>
          <ElFormItem label="组名称" prop="name">
            <ElInput v-model="formData.name" placeholder="请输入组名称" />
          </ElFormItem>
          <ElFormItem label="描述">
            <ElInput
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入组描述"
            />
          </ElFormItem>
        </ElForm>
        <template #footer>
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton
            type="primary"
            @click="handleSubmit"
            :loading="submitting"
          >
            {{ isEditMode ? '更新' : '创建' }}
          </ElButton>
        </template>
      </ElDialog>

      <!-- Assign People Dialog -->
      <ElDialog
        v-model="assignPeopleDialogVisible"
        title="分配人员"
        width="700px"
        :close-on-click-modal="false"
      >
        <div class="assign-dialog-header">
          为组 <ElTag type="primary">{{ currentGroupName }}</ElTag> 分配人员
        </div>
        <div class="assign-search-bar">
          <ElInput
            v-model="assignPeopleName"
            placeholder="姓名"
            clearable
            style="width: 160px"
            @keyup.enter="assignPeoplePage = 1; loadAssignPeople()"
          />
          <ElInput
            v-model="assignPeopleCode"
            placeholder="工号"
            clearable
            style="width: 160px"
            @keyup.enter="assignPeoplePage = 1; loadAssignPeople()"
          />
          <ElButton type="primary" @click="assignPeoplePage = 1; loadAssignPeople()">
            搜索
          </ElButton>
        </div>
        <div
          v-loading="assignLoading"
          class="assign-table-wrapper"
          element-loading-text="加载中..."
        >
          <ElTable
            ref="peopleTableRef"
            :data="allAccounts"
            style="width: 100%"
            stripe
            max-height="360"
            @selection-change="
              (rows) => {
                selectedAccountIds = rows.map((r: any) => r.id).filter(Boolean);
              }
            "
            :row-key="(row: any) => row.id"
          >
            <ElTableColumn
              type="selection"
              width="50"
              :reserve-selection="true"
            />
            <ElTableColumn prop="code" label="工号" width="100" />
            <ElTableColumn prop="username" label="用户名" />
            <ElTableColumn prop="realName" label="真实姓名" />
            <ElTableColumn prop="phone" label="手机号" />
            <ElTableColumn label="状态" width="70">
              <template #default="scope">
                <ElTag
                  :type="
                    (scope.row as AccountInfo).status === '1'
                      ? 'success'
                      : 'danger'
                  "
                  size="small"
                >
                  {{
                    (scope.row as AccountInfo).status === '1' ? '启用' : '禁用'
                  }}
                </ElTag>
              </template>
            </ElTableColumn>
          </ElTable>
          <div class="assign-pagination-wrapper">
            <ElPagination
              v-model:current-page="assignPeoplePage"
              v-model:page-size="assignPeoplePageSize"
              :total="assignPeopleTotal"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              small
              @current-change="loadAssignPeople"
              @size-change="assignPeoplePage = 1; loadAssignPeople()"
            />
          </div>
        </div>
        <template #footer>
          <ElButton @click="assignPeopleDialogVisible = false">取消</ElButton>
          <ElButton
            type="primary"
            @click="confirmAssignPeople"
            :loading="assignLoading"
          >
            确认分配
          </ElButton>
        </template>
      </ElDialog>

      <!-- Assign Agent Dialog -->
      <ElDialog
        v-model="assignAgentDialogVisible"
        title="分配智能体"
        width="800px"
        :close-on-click-modal="false"
      >
        <div class="assign-dialog-header">
          为组 <ElTag type="primary">{{ currentGroupName }}</ElTag> 分配智能体
        </div>
        <div
          v-loading="assignLoading"
          class="assign-table-wrapper"
          element-loading-text="加载中..."
        >
          <ElTable
            ref="agentTableRef"
            :data="allAgents"
            style="width: 100%"
            stripe
            max-height="400"
            @selection-change="
              (rows) => {
                selectedAgentIds = rows.map((r: any) => r.id).filter(Boolean);
              }
            "
            :row-key="(row: any) => row.id"
          >
            <ElTableColumn
              type="selection"
              width="50"
              :reserve-selection="true"
            />
            <ElTableColumn prop="name" label="名称" min-width="80" />
            <ElTableColumn prop="category" label="类型" min-width="40" />
            <ElTableColumn prop="description" label="描述" min-width="180" show-overflow-tooltip />
          </ElTable>
        </div>
        <template #footer>
          <ElButton @click="assignAgentDialogVisible = false">取消</ElButton>
          <ElButton
            type="primary"
            @click="confirmAssignAgent"
            :loading="assignLoading"
          >
            确认分配
          </ElButton>
        </template>
      </ElDialog>

      <!-- Remove Agent Dialog -->
      <ElDialog
        v-model="removeAgentDialogVisible"
        title="移除智能体"
        width="600px"
        :close-on-click-modal="false"
      >
        <div class="assign-dialog-header">
          为组 <ElTag type="primary">{{ removeAgentGroupName }}</ElTag> 移除智能体
        </div>
        <div
          v-loading="removeAgentLoading"
          class="assign-table-wrapper"
          element-loading-text="加载中..."
        >
          <ElTable
            :data="assignedAgents"
            style="width: 100%"
            stripe
            max-height="400"
            @selection-change="
              (rows) => {
                selectedRemoveAgentIds = rows.map((r: any) => r.agentId).filter(Boolean);
              }
            "
            :row-key="(row: any) => row.agentId"
          >
            <ElTableColumn type="selection" width="50" />
            <ElTableColumn prop="agentName" label="智能体名称" min-width="120" />
          </ElTable>
        </div>
        <template #footer>
          <ElButton @click="removeAgentDialogVisible = false">取消</ElButton>
          <ElButton
            type="danger"
            @click="confirmRemoveAgent"
            :loading="removeAgentLoading"
          >
            确认移除
          </ElButton>
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

.assign-dialog-header {
  margin-bottom: 1rem;
  font-size: 0.95rem;
  color: #374151;
}

.assign-search-bar {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.assign-table-wrapper {
  min-height: 200px;
}

.assign-pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.75rem;
}
</style>
