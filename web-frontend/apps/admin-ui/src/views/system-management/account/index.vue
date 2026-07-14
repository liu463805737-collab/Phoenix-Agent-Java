<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { FormInstance, FormRules } from 'element-plus';

import DeptTreeSidebar from '#/components/dept/DeptTreeSidebar.vue';
import DepartmentSelector from '#/components/dept/DepartmentSelector.vue';
import EmployeeSelector from '#/components/dept/EmployeeSelector.vue';
import type { DepartmentTreeVO, OrganizationTreeVO, PrivilegeRole, PrivilegeUser, PrivilegeUserRoleVO } from '#/api';

import { computed, nextTick, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useVbenForm, z } from '#/adapter/form';

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
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  adminSetPasswordApi,
  createUserApi,
  deleteUserApi,
  getRolePageApi,
  getUserPageApi,
  getUserRolesByUserIdApi,
  removeUserRoleBatchApi,
  saveUserRoleBatchApi,
  updateUserApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<PrivilegeUser[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const setPasswordDialogVisible = ref(false);
const passwordSubmitting = ref(false);
const currentPasswordUserId = ref<string | null>(null);
const isEditMode = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const selectedDeptId = ref<string | undefined>(undefined);

const passwordFormSchema = computed((): VbenFormSchema[] => [
  {
    fieldName: 'newPassword',
    label: '新密码',
    component: 'VbenInputPassword',
    componentProps: {
      passwordStrength: true,
      placeholder: '请输入新密码',
    },
  },
  {
    fieldName: 'confirmPassword',
    label: '确认密码',
    component: 'VbenInputPassword',
    componentProps: {
      passwordStrength: true,
      placeholder: '请再次输入新密码',
    },
    dependencies: {
      rules(values) {
        const { newPassword } = values;
        return z
          .string({ required_error: '请再次输入新密码' })
          .min(1, { message: '请再次输入新密码' })
          .refine((value) => value === newPassword, {
            message: '两次输入的密码不一致',
          });
      },
      triggerFields: ['newPassword'],
    },
  },
]);

const [PasswordForm, passwordFormApi] = useVbenForm(
  reactive({
    schema: passwordFormSchema,
    showDefaultActions: false,
    layout: 'horizontal',
    commonConfig: {
      labelWidth: 80,
      componentProps: {
        class: 'w-full',
      },
    },
  }),
);

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
      componentProps: { placeholder: '请输入用户名/姓名' },
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
  mobile: '',
  code: '',
  companyId: undefined,
  deptId: undefined,
  userType: undefined,
  password: '',
  confirmPassword: '',
  employeeId: undefined,
});

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
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

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const queryParams = { ...params };
    if (selectedDeptId.value) {
      queryParams.deptId = selectedDeptId.value;
    }
    const res = (await getUserPageApi(
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

async function showAddDialog() {
  isEditMode.value = false;
  formData.username = '';
  formData.realName = '';
  formData.mobile = '';
  formData.code = '';
  formData.companyId = undefined;
  formData.deptId = undefined;
  formData.userType = undefined;
  formData.password = '';
  formData.confirmPassword = '';
  formData.employeeId = undefined;
  dialogVisible.value = true;
}

async function handleEdit(row: PrivilegeUser) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    mobile: row.mobile,
    code: row.code,
    companyId: row.companyId,
    deptId: row.deptId,
    userType: row.userType,
    password: '',
    confirmPassword: '',
    employeeId: row.employeeId,
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
      await updateUserApi(payload);
      ElMessage.success('账号更新成功');
    } else {
      await createUserApi(payload);
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
    await deleteUserApi(id);
    ElMessage.success('账号删除成功');
    loadData();
  } catch {
    // cancelled or error
  }
}

async function handleSetPassword(id: string) {
  currentPasswordUserId.value = id;
  setPasswordDialogVisible.value = true;
  await nextTick();
  passwordFormApi.resetForm();
  passwordFormApi.resetValidate();
}

async function handlePasswordSubmit() {
  const { valid } = await passwordFormApi.validate();
  if (!valid || currentPasswordUserId.value === null) return;
  const values = await passwordFormApi.getValues();
  passwordSubmitting.value = true;
  try {
    await adminSetPasswordApi(currentPasswordUserId.value, values.newPassword);
    ElMessage.success('密码设置成功');
    setPasswordDialogVisible.value = false;
  } catch {
    // Error handled by interceptor
  } finally {
    passwordSubmitting.value = false;
  }
}

const assignRoleDialogVisible = ref(false);
const assignRoleUser = ref<PrivilegeUser | null>(null);
const roleList = ref<PrivilegeRole[]>([]);
const roleTotal = ref(0);
const rolePage = ref(1);
const rolePageSize = ref(10);
const roleLoading = ref(false);
const roleSubmitting = ref(false);
const checkedRoleIds = ref<Set<string>>(new Set());

async function loadRoles() {
  roleLoading.value = true;
  try {
    const res = (await getRolePageApi({ page: rolePage.value, size: rolePageSize.value, validState: 1 })) as any;
    const pageResult = res?.data || res;
    roleList.value = pageResult?.records || [];
    roleTotal.value = pageResult?.totalRow || 0;
  } catch {
    roleList.value = [];
    roleTotal.value = 0;
  } finally {
    roleLoading.value = false;
  }
}

async function handleAssignRole(row: PrivilegeUser) {
  assignRoleUser.value = row;
  checkedRoleIds.value = new Set();
  rolePage.value = 1;
  assignRoleDialogVisible.value = true;
  await loadRoles();
  if (row.id) {
    try {
      const res = (await getUserRolesByUserIdApi(row.id)) as any;
      const userRoles = res?.data || res || [];
      checkedRoleIds.value = new Set(
        (userRoles as PrivilegeUserRoleVO[]).map((r) => r.roleId!).filter(Boolean),
      );
    } catch {
      // ignore
    }
  }
}

function handleRolePageChange(val: number) {
  rolePage.value = val;
  loadRoles();
}

function handleRoleSizeChange(val: number) {
  rolePageSize.value = val;
  rolePage.value = 1;
  loadRoles();
}

function handleRoleCheckChange(roleId: string, checked: string | number | boolean) {
  const next = new Set(checkedRoleIds.value);
  if (checked) {
    next.add(roleId);
  } else {
    next.delete(roleId);
  }
  checkedRoleIds.value = next;
}

function handleRoleCheckAll(val: string | number | boolean) {
  if (val) {
    checkedRoleIds.value = new Set(roleList.value.map((r) => r.id!));
  } else {
    checkedRoleIds.value = new Set();
  }
}

async function handleAssignRoleConfirm() {
  const user = assignRoleUser.value;
  if (!user?.id) return;
  roleSubmitting.value = true;
  try {
    const userIds = [user.id];
    const checked = checkedRoleIds.value;
    const allRoleIds = new Set(roleList.value.map((r) => r.id!));

    const toSave: string[] = [];
    const toRemove: string[] = [];

    for (const roleId of checked) {
      toSave.push(roleId);
    }

    for (const roleId of allRoleIds) {
      if (!checked.has(roleId)) {
        toRemove.push(roleId);
      }
    }

    for (const roleId of toSave) {
      await saveUserRoleBatchApi({ roleId, userIds });
    }

    for (const roleId of toRemove) {
      await removeUserRoleBatchApi({ roleId, userIds });
    }

    ElMessage.success('权限分配成功');
    assignRoleDialogVisible.value = false;
  } catch {
    ElMessage.error('权限分配失败');
  } finally {
    roleSubmitting.value = false;
  }
}

function handleDeptClick(_data: OrganizationTreeVO | DepartmentTreeVO) {
  page.value = 1;
  loadData();
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
      <div class="split-layout">
        <DeptTreeSidebar v-model="selectedDeptId" @select="handleDeptClick" />

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

          <div class="table-wrapper">
          <ElTable
            :data="tableData"
            style="width: 100%"
            stripe
            v-loading="loading"
            empty-text="暂无数据"
          >
            <ElTableColumn prop="username" label="用户名" min-width="120" />
            <ElTableColumn prop="realName" label="真实姓名" width="120" />
            <ElTableColumn prop="mobile" label="手机号" width="100" />
            <ElTableColumn prop="code" label="工号" width="100" />
            <ElTableColumn prop="companyName" label="公司名称" width="140" />
            <ElTableColumn prop="deptName" label="部门名称" width="120" />
            <ElTableColumn prop="userType" label="用户类型" width="110">
              <template #default="scope">
                <ElTag
                  :type="
                    (scope.row as PrivilegeUser).userType === 1
                      ? 'primary'
                      : 'info'
                  "
                  size="small"
                >
                  {{
                    (scope.row as PrivilegeUser).userType === 1
                      ? 'idm用户'
                      : '自建用户'
                  }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="状态" width="80">
              <template #default="scope">
                <ElTag
                  :type="
                    (scope.row as PrivilegeUser).status === 0
                      ? 'success'
                      : 'danger'
                  "
                  size="small"
                >
                  {{
                    (scope.row as PrivilegeUser).status === 0 ? '启用' : '禁用'
                  }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="角色" width="180">
              <template #default="scope">
                <span>{{
                  ((scope.row as PrivilegeUser).roles || []).map(r => r.name).join('、')
                }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createTime" label="创建时间" width="180" />
            <ElTableColumn label="操作" width="300" fixed="right">
              <template #default="scope">
                <ElButton
                  type="success"
                  size="small"
                  @click="handleAssignRole(scope.row as PrivilegeUser)"
                >
                  分配权限
                </ElButton>
                <ElButton
                  type="warning"
                  size="small"
                  @click="handleSetPassword((scope.row as PrivilegeUser).id!)"
                >
                  设置密码
                </ElButton>
                <ElButton
                  type="primary"
                  size="small"
                  @click="handleEdit(scope.row as PrivilegeUser)"
                >
                  编辑
                </ElButton>
                <ElButton
                  type="danger"
                  size="small"
                  @click="
                    handleDelete(
                      (scope.row as PrivilegeUser).id!,
                      (scope.row as PrivilegeUser).username!,
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
            layout="total, sizes, prev, pager, next"
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
        width="550px"
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
                formData.employeeId = emp.id;
                formData.realName = emp.empName || '';
                formData.mobile = emp.mobile || '';
                formData.code = emp.empCode || '';
                formData.companyId = emp.companyId != null ? emp.companyId : undefined;
                formData.deptId = emp.deptId != null ? emp.deptId : undefined;
              }"
            />
          </ElFormItem>
          <ElFormItem label="用户名" prop="username">
            <ElInput v-model="formData.username" placeholder="请输入用户名" />
          </ElFormItem>
          <ElFormItem label="真实姓名" prop="realName">
            <ElInput v-model="formData.realName" placeholder="请输入真实姓名" />
          </ElFormItem>
          <ElFormItem label="手机号" prop="mobile">
            <ElInput v-model="formData.mobile" placeholder="请输入手机号" />
          </ElFormItem>
          <ElFormItem label="工号" prop="code">
            <ElInput v-model="formData.code" placeholder="请输入工号" />
          </ElFormItem>

          <ElFormItem label="部门" prop="deptId">
            <DepartmentSelector
              v-model="formData.deptId"
              @change="(data) => { formData.companyId = data.companyId }"
            />
          </ElFormItem>
          <ElFormItem label="用户类型" prop="userType">
            <ElSelect v-model="formData.userType" placeholder="请选择用户类型">
              <ElOption label="自建用户" :value="0" />
              <ElOption label="idm用户" :value="1" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem :label="isEditMode ? '新密码' : '密码'" prop="password">
            <ElInput
              v-model="formData.password"
              :placeholder="isEditMode ? '留空则不修改' : '请输入密码'"
              type="password"
              show-password
            />
          </ElFormItem>
          <ElFormItem :label="isEditMode ? '确认新密码' : '确认密码'" prop="confirmPassword">
            <ElInput
              v-model="formData.confirmPassword"
              :placeholder="isEditMode ? '请再次输入新密码' : '请再次输入密码'"
              type="password"
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

      <ElDialog
        v-model="setPasswordDialogVisible"
        title="设置密码"
        width="480px"
        :close-on-click-modal="false"
      >
        <div class="set-password-form">
          <PasswordForm />
        </div>
        <template #footer>
          <ElButton @click="setPasswordDialogVisible = false">取消</ElButton>
          <ElButton
            type="primary"
            :loading="passwordSubmitting"
            @click="handlePasswordSubmit"
          >
            确认设置
          </ElButton>
        </template>
      </ElDialog>

      <ElDialog
        v-model="assignRoleDialogVisible"
        width="560px"
        :close-on-click-modal="false"
      >
        <template #header>
          <div class="assign-role-header">
            <span class="assign-role-title">分配权限</span>
            <span class="assign-role-user">
              {{ assignRoleUser?.realName || assignRoleUser?.username }}
            </span>
          </div>
        </template>
        <div v-loading="roleLoading" class="assign-role-body">
          <div v-if="roleList.length === 0 && !roleLoading" class="assign-empty">
            暂无角色数据
          </div>
          <template v-else>
            <div class="assign-role-toolbar">
              <ElCheckbox
                :model-value="
                  roleList.length > 0 &&
                  checkedRoleIds.size === roleList.length
                "
                :indeterminate="
                  checkedRoleIds.size > 0 &&
                  checkedRoleIds.size < roleList.length
                "
                @change="handleRoleCheckAll"
              >
                全选
              </ElCheckbox>
              <span class="assign-role-count">
                已选 {{ checkedRoleIds.size }} / {{ roleList.length }}
              </span>
            </div>
            <div class="assign-role-list">
              <div
                v-for="role in roleList"
                :key="role.id"
                class="assign-role-item"
                :class="{ 'is-checked': checkedRoleIds.has(role.id!) }"
                @click="handleRoleCheckChange(role.id!, !checkedRoleIds.has(role.id!))"
              >
                <ElCheckbox
                  :model-value="checkedRoleIds.has(role.id!)"
                  @click.stop
                >
                  <span class="assign-role-name">{{ role.name }}</span>
                </ElCheckbox>
              </div>
            </div>
          </template>
        </div>
        <div class="pagination-wrapper">
          <ElPagination
            v-model:current-page="rolePage"
            v-model:page-size="rolePageSize"
            :total="roleTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            background
            small
            @current-change="handleRolePageChange"
            @size-change="handleRoleSizeChange"
          />
        </div>
        <template #footer>
          <ElButton @click="assignRoleDialogVisible = false">取消</ElButton>
          <ElButton
            type="primary"
            :loading="roleSubmitting"
            @click="handleAssignRoleConfirm"
          >
            确认
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

.set-password-form {
  padding: 1rem 0.5rem;
}

.department-selector {
  width: 100%;
}

.assign-role-header {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.assign-role-title {
  font-size: 1rem;
  font-weight: 600;
}

.assign-role-user {
  padding: 2px 10px;
  font-size: 0.85rem;
  color: #909399;
  white-space: nowrap;
  background: #f4f4f5;
  border-radius: 4px;
}

.assign-role-body {
  min-height: 120px;
}

.assign-empty {
  padding: 3rem 0;
  font-size: 0.9rem;
  color: #999;
  text-align: center;
}

.assign-role-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 0.75rem;
  margin-bottom: 0.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.assign-role-count {
  font-size: 0.8rem;
  color: #909399;
}

.assign-role-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 320px;
  overflow-y: auto;
}

.assign-role-item {
  display: flex;
  align-items: center;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
}

.assign-role-item:hover {
  background: #f5f7fa;
}

.assign-role-item.is-checked {
  background: #ecf5ff;
}

.assign-role-name {
  font-size: 0.9rem;
}



.split-layout {
  display: flex;
  flex: 1;
  gap: 16px;
  min-height: 0;
}
</style>
