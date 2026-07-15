<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { DepartmentTreeVO, GroupInfo, OrganizationTreeVO, PlatformAccountGroupInfo, PlatformAccountInfo } from '#/api';

import { nextTick, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

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
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createAccountGroupInfoApi,
  createAccountInfoApi,
  deleteAccountGroupInfoByGroupAndAccountApi,
  deleteAccountInfoApi,
  getAccountGroupInfoByAccountApi,
  getAccountInfoPageApi,
  getGroupInfoPageApi,
  updateAccountInfoApi,
} from '#/api';
import DepartmentSelector from '#/components/dept/DepartmentSelector.vue';
import DeptTreeSidebar from '#/components/dept/DeptTreeSidebar.vue';
import EmployeeSelector from '#/components/dept/EmployeeSelector.vue';

const loading = ref(false);
const tableData = ref<PlatformAccountInfo[]>([]);
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
      fieldName: 'keyword',
      component: 'Input',
      label: '搜索',
      labelWidth: 40,
      componentProps: { placeholder: '用户名/真实姓名/手机号' },
    },
    {
      fieldName: 'status',
      component: 'Select',
      label: '状态',
      componentProps: {
        style: 'width: 160px',
        placeholder: '请选择状态',
        options: [
          { label: '启用', value: '1' },
          { label: '禁用', value: '0' },
        ],
      },
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
  username: '',
  realName: '',
  nickName: '',
  code: '',
  phone: '',
  email: '',
  gender: '',
  birthday: '',
  status: '1',
  password: '',
  confirmPassword: '',
  deptId: '',
  deptName: '',
});

const genderOptions = [
  { label: '未知', value: '0' },
  { label: '男', value: '1' },
  { label: '女', value: '2' },
];

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: ['blur', 'change'] },
  ],
};

const editFormRules: FormRules = {
  confirmPassword: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!formData.password && !value) {
          callback();
          return;
        }
        if (value !== formData.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: ['blur', 'change'],
    },
  ],
};

const removeGroupDialogVisible = ref(false);
const removeGroupLoading = ref(false);
const removeGroupAccountId = ref('');
const removeGroupAccountName = ref('');
const allGroups = ref<GroupInfo[]>([]);
const assignedGroupIds = ref<Set<string>>(new Set());
const selectedGroupIds = ref<string[]>([]);
const groupTableRef = ref();
const selectedDeptId = ref<string | undefined>(undefined);

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const queryParams = { ...params };
    if (selectedDeptId.value) {
      queryParams.deptId = selectedDeptId.value;
    }
    const res = (await getAccountInfoPageApi(
      page.value,
      pageSize.value,
      queryParams,
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
  formData.username = '';
  formData.realName = '';
  formData.nickName = '';
  formData.code = '';
  formData.phone = '';
  formData.email = '';
  formData.gender = '0';
  formData.birthday = '';
  formData.status = '1';
  formData.password = '';
  formData.confirmPassword = '';
  formData.deptId = '';
  formData.deptName = '';
  delete formData.id;
  dialogVisible.value = true;
}

function handleEdit(row: PlatformAccountInfo) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    nickName: row.nickName,
    code: row.code,
    phone: row.phone,
    email: row.email,
    gender: row.gender,
    birthday: row.birthday,
    status: row.status,
    password: '',
    confirmPassword: '',
    deptId: row.deptId ?? '',
    deptName: row.deptName ?? '',
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    const payload = { ...formData };
    delete payload.confirmPassword;
    if (!payload.password) {
      delete payload.password;
    }
    if (isEditMode.value) {
      await updateAccountInfoApi(payload);
      ElMessage.success('账号更新成功');
    } else {
      await createAccountInfoApi(payload);
      ElMessage.success('账号创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch {
    // validation or API error
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: string, username: string) {
  try {
    await ElMessageBox.confirm(
      `确定要删除账号 "${username}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteAccountInfoApi(id);
    ElMessage.success('账号删除成功');
    loadData();
  } catch {
    // cancelled or error
  }
}

async function openRemoveGroupDialog(row: PlatformAccountInfo) {
  removeGroupAccountId.value = row.id ?? '';
  removeGroupAccountName.value = row.username ?? '';
  removeGroupDialogVisible.value = true;
  removeGroupLoading.value = true;
  try {
    const groupRes = (await getGroupInfoPageApi(1, 9999)) as any;
    const groupPageResult = groupRes?.data || groupRes;
    allGroups.value = groupPageResult?.records || [];

    const assignRes = (await getAccountGroupInfoByAccountApi(row.id ?? '')) as any;
    const assignments: PlatformAccountGroupInfo[] =
      assignRes?.data || assignRes || [];
    assignedGroupIds.value = new Set(
      assignments
        .map((a) => a.groupId)
        .filter((id): id is string => id != null),
    );
    selectedGroupIds.value = [...assignedGroupIds.value];

    await nextTick();
    if (groupTableRef.value) {
      groupTableRef.value.clearSelection();
      allGroups.value.forEach((g) => {
        if (assignedGroupIds.value.has(g.id ?? '') && g.id != null) {
          groupTableRef.value.toggleRowSelection(g, true);
        }
      });
    }
  } catch {
    allGroups.value = [];
    assignedGroupIds.value = new Set();
    selectedGroupIds.value = [];
  } finally {
    removeGroupLoading.value = false;
  }
}

async function confirmAssignGroup() {
  removeGroupLoading.value = true;
  try {
    const accountId = removeGroupAccountId.value;
    const accountName = removeGroupAccountName.value;
    const currentIds = [...assignedGroupIds.value];
    const newIds = selectedGroupIds.value;
    const toRemove = currentIds.filter((id) => !newIds.includes(id));
    const toAdd = newIds.filter((id) => !currentIds.includes(id));

    for (const groupId of toRemove) {
      await deleteAccountGroupInfoByGroupAndAccountApi(groupId, accountId);
    }
    for (const groupId of toAdd) {
      const group = allGroups.value.find((g) => g.id === groupId);
      await createAccountGroupInfoApi({
        groupId,
        accountId,
        groupName: group?.name ?? '',
        accountName,
      });
    }
    ElMessage.success('组分配已保存');
    removeGroupDialogVisible.value = false;
    loadData();
  } catch {
    ElMessage.error('保存失败');
  } finally {
    removeGroupLoading.value = false;
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

function handleDeptClick(_data: OrganizationTreeVO | DepartmentTreeVO) {
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
      <div class="split-layout">
        <DeptTreeSidebar v-model="selectedDeptId" @select="handleDeptClick" />

        <ElCard class="table-section flex-1" :body-style="{ padding: '20px' }">
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

          <div class="table-wrapper">
          <ElTable
            :data="tableData"
            style="width: 100%"
            stripe
            border
            v-loading="loading"
            empty-text="暂无数据"
          >
            <ElTableColumn prop="username" label="用户名" min-width="120" resizable />
            <ElTableColumn prop="realName" label="真实姓名" width="100" resizable />
            <ElTableColumn prop="nickName" label="昵称" width="100" resizable />
            <ElTableColumn prop="code" label="编码" width="100" resizable />
            <ElTableColumn prop="phone" label="手机号" width="130" resizable />
            <ElTableColumn prop="email" label="邮箱" min-width="160" resizable />
            <ElTableColumn label="性别" width="60" resizable>
              <template #default="scope">
                {{
                  (scope.row as PlatformAccountInfo).gender === '1'
                    ? '男'
                    : (scope.row as PlatformAccountInfo).gender === '2'
                      ? '女'
                      : '未知'
                }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="状态" width="70" resizable>
              <template #default="scope">
                <ElTag
                  :type="
                    (scope.row as PlatformAccountInfo).status === '1'
                      ? 'success'
                      : 'danger'
                  "
                  size="small"
                >
                  {{
                    (scope.row as PlatformAccountInfo).status === '1'
                      ? '启用'
                      : '禁用'
                  }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="所属组" min-width="160" resizable>
              <template #default="scope">
                {{
                  ((scope.row as PlatformAccountInfo).groups ?? [])
                    .map(g => g.groupName)
                    .filter(Boolean)
                    .join('、') || '-'
                }}
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createTime" label="创建时间" width="180" resizable />
            <ElTableColumn label="操作" width="240" fixed="right">
              <template #default="scope">
                <ElButton
                  type="primary"
                  size="small"
                  @click="handleEdit(scope.row as PlatformAccountInfo)"
                >
                  编辑
                </ElButton>
                <ElButton
                  type="warning"
                  size="small"
                  @click="openRemoveGroupDialog(scope.row as PlatformAccountInfo)"
                >
                  分配组
                </ElButton>
                <ElButton
                  type="danger"
                  size="small"
                  @click="
                    handleDelete(
                      (scope.row as PlatformAccountInfo).id!,
                      (scope.row as PlatformAccountInfo).username!,
                    )
                  "
                >
                  删除
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
          </div>

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
      </div>

      <ElDialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑账号' : '新增账号'"
        width="600px"
        :close-on-click-modal="false"
      >
        <ElForm
          ref="formRef"
          :model="formData"
          :rules="isEditMode ? editFormRules : formRules"
          label-width="100px"
          label-position="left"
        >
          <ElFormItem v-if="!isEditMode" label="选择人员">
            <EmployeeSelector
              @change="(emp) => {
                formData.username = emp.empCode || '';
                formData.realName = emp.empName || '';
                formData.code = emp.empCode || '';
                formData.phone = emp.mobile || '';
                formData.deptId = emp.deptId ?? '';
                formData.deptName = emp.deptName || '';
              }"
            />
          </ElFormItem>
          <ElFormItem label="用户名" prop="username">
            <ElInput v-model="formData.username" placeholder="请输入用户名" />
          </ElFormItem>
          <ElFormItem label="真实姓名">
            <ElInput v-model="formData.realName" placeholder="请输入真实姓名" />
          </ElFormItem>
          <ElFormItem label="昵称">
            <ElInput v-model="formData.nickName" placeholder="请输入昵称" />
          </ElFormItem>
          <ElFormItem label="账号编码">
            <ElInput v-model="formData.code" placeholder="请输入账号编码" />
          </ElFormItem>
          <ElFormItem label="手机号">
            <ElInput v-model="formData.phone" placeholder="请输入手机号" />
          </ElFormItem>
          <ElFormItem label="邮箱">
            <ElInput v-model="formData.email" placeholder="请输入邮箱" />
          </ElFormItem>
          <ElFormItem label="性别">
            <ElSelect v-model="formData.gender" placeholder="请选择性别">
              <ElOption
                v-for="opt in genderOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="生日">
            <ElInput v-model="formData.birthday" placeholder="请输入生日" />
          </ElFormItem>
          <ElFormItem label="所属部门">
            <DepartmentSelector
              v-model="formData.deptId"
              @change="(data) => { formData.deptName = data.deptName }"
            />
          </ElFormItem>
          <ElFormItem label="状态">
            <ElSelect v-model="formData.status" placeholder="请选择状态">
              <ElOption label="启用" value="1" />
              <ElOption label="禁用" value="0" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem :label="isEditMode ? '新密码' : '密码'" prop="password">
            <ElInput
              v-model="formData.password"
              type="password"
              :placeholder="isEditMode ? '留空则不修改' : '请输入密码'"
              show-password
            />
          </ElFormItem>
          <ElFormItem :label="isEditMode ? '确认新密码' : '确认密码'" prop="confirmPassword">
            <ElInput
              v-model="formData.confirmPassword"
              type="password"
              :placeholder="isEditMode ? '请再次输入新密码' : '请再次输入密码'"
              show-password
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

      <!-- Assign Group Dialog -->
      <ElDialog
        v-model="removeGroupDialogVisible"
        title="分配组"
        width="600px"
        :close-on-click-modal="false"
      >
        <div class="remove-group-dialog-header">
          为用户 <ElTag type="primary">{{ removeGroupAccountName }}</ElTag> 分配组
        </div>
        <div v-loading="removeGroupLoading" class="remove-group-table-wrapper">
          <ElTable
            ref="groupTableRef"
            :data="allGroups"
            style="width: 100%"
            stripe
            max-height="360"
            @selection-change="
              (rows) => {
                selectedGroupIds = rows.map((r: any) => r.id).filter(Boolean);
              }
            "
            :row-key="(row: any) => row.id"
          >
            <ElTableColumn type="selection" width="50" :reserve-selection="true" />
            <ElTableColumn prop="name" label="组名称" />
            <ElTableColumn prop="description" label="描述" />
          </ElTable>
        </div>
        <template #footer>
          <ElButton @click="removeGroupDialogVisible = false">取消</ElButton>
          <ElButton
            type="primary"
            @click="confirmAssignGroup"
            :loading="removeGroupLoading"
          >
            确认保存
          </ElButton>
        </template>
      </ElDialog>

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

.table-section {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 12px;
}

.table-section :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 20px;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  flex-shrink: 0;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.table-wrapper {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.pagination-wrapper {
  display: flex;
  flex-shrink: 0;
  justify-content: flex-end;
  margin-top: 1rem;
}

.remove-group-dialog-header {
  margin-bottom: 1rem;
  font-size: 0.95rem;
  color: #374151;
}

.remove-group-table-wrapper {
  min-height: 120px;
}

.split-layout {
  display: flex;
  flex: 1;
  gap: 16px;
  min-height: 0;
}
</style>
