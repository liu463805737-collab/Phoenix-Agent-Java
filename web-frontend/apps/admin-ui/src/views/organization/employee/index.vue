<script lang="ts" setup>
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
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';

import DeptTreeSidebar from '#/components/dept/DeptTreeSidebar.vue';
import DepartmentSelector from '#/components/dept/DepartmentSelector.vue';
import type { DepartmentTreeVO, OrganizationTreeVO, PrivilegeEmployee } from '#/api';
import {
  createEmployeeApi,
  deleteEmployeeApi,
  getEmployeePageApi,
  updateEmployeeApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<PrivilegeEmployee[]>([]);
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
      componentProps: { placeholder: '用户名/工号/手机号' },
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
  id: undefined,
  empName: '',
  empCode: '',
  mobile: '',
  companyId: '',
  deptId: '',
  deptName: '',
  status: 1,
  enableFlag: 1,
});

const formRules: FormRules = {
  empName: [{ required: true, message: '请输入人员姓名', trigger: 'blur' }],
  empCode: [{ required: true, message: '请输入工号', trigger: 'blur' }],
};

const selectedDeptId = ref<string | undefined>(undefined);

function handleDeptClick(_data: OrganizationTreeVO | DepartmentTreeVO) {
  page.value = 1;
  loadData();
}

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const queryParams = { ...params };
    if (selectedDeptId.value) {
      queryParams.deptId = selectedDeptId.value;
    }
    const res = (await getEmployeePageApi(
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
  Object.assign(formData, {
    id: undefined,
    empName: '',
    empCode: '',
    mobile: '',
    companyId: '',
    deptId: '',
    deptName: '',
    status: 1,
    enableFlag: 1,
  });
  dialogVisible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
}

async function handleEdit(row: PrivilegeEmployee) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    empName: row.empName,
    empCode: row.empCode,
    mobile: row.mobile,
    companyId: row.companyId,
    deptId: row.deptId,
    deptName: row.deptName ?? '',
    status: row.status,
    enableFlag: row.enableFlag,
  });
  dialogVisible.value = true;
  await nextTick();
  formRef.value?.clearValidate();
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updateEmployeeApi({ ...formData });
      ElMessage.success('人员更新成功');
    } else {
      await createEmployeeApi({ ...formData });
      ElMessage.success('人员创建成功');
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
      `确定要删除人员 "${name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteEmployeeApi(id);
    ElMessage.success('人员删除成功');
    loadData();
  } catch {
    // cancelled or error
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
            v-loading="loading"
            empty-text="暂无数据"
          >
            <ElTableColumn prop="empName" label="姓名" min-width="80" />
            <ElTableColumn prop="empCode" label="工号" width="80" />
            <ElTableColumn prop="mobile" label="手机号" width="110" />
            <ElTableColumn prop="companyName" label="公司名称" width="160" />
            <ElTableColumn prop="deptName" label="部门名称" width="160" />
            <ElTableColumn label="状态" width="70">
              <template #default="scope">
                <ElTag
                  :type="
                    (scope.row as PrivilegeEmployee).status === 1
                      ? 'success'
                      : 'danger'
                  "
                  size="small"
                >
                  {{
                    (scope.row as PrivilegeEmployee).status === 1
                      ? '在职'
                      : '离职'
                  }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn label="启用" width="60">
              <template #default="scope">
                <ElTag
                  :type="
                    (scope.row as PrivilegeEmployee).enableFlag === 1
                      ? 'success'
                      : 'info'
                  "
                  size="small"
                >
                  {{
                    (scope.row as PrivilegeEmployee).enableFlag === 1
                      ? '是'
                      : '否'
                  }}
                </ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="createdTime" label="创建时间" width="180" />
            <ElTableColumn label="操作" width="180" fixed="right">
              <template #default="scope">
                <ElButton
                  type="primary"
                  size="small"
                  @click="handleEdit(scope.row as PrivilegeEmployee)"
                >
                  编辑
                </ElButton>
                <ElButton
                  type="danger"
                  size="small"
                  @click="
                    handleDelete(
                      (scope.row as PrivilegeEmployee).id!,
                      (scope.row as PrivilegeEmployee).empName!,
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
        :title="isEditMode ? '编辑人员' : '新增人员'"
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
          <ElFormItem label="姓名" prop="empName">
            <ElInput v-model="formData.empName" placeholder="请输入姓名" />
          </ElFormItem>
          <ElFormItem label="工号" prop="empCode">
            <ElInput v-model="formData.empCode" placeholder="请输入工号" />
          </ElFormItem>
          <ElFormItem label="手机号">
            <ElInput v-model="formData.mobile" placeholder="请输入手机号" />
          </ElFormItem>
          <ElFormItem label="部门名称">
            <DepartmentSelector
              v-model="formData.deptId"
              @change="(data) => { formData.deptName = data.deptName }"
            />
          </ElFormItem>
          <ElFormItem label="状态">
            <ElSelect v-model="formData.status" placeholder="请选择状态">
              <ElOption label="在职" :value="1" />
              <ElOption label="离职" :value="0" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="启用">
            <ElSelect v-model="formData.enableFlag" placeholder="请选择">
              <ElOption label="启用" :value="1" />
              <ElOption label="禁用" :value="0" />
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

.split-layout {
  display: flex;
  flex: 1;
  gap: 16px;
  min-height: 0;
}
</style>
