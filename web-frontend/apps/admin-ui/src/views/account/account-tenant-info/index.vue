<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type {
  AccountInfo,
  GroupAgentInfo,
  PlatformAccountTenantInfo,
} from '#/api';

import { onMounted, reactive, ref } from 'vue';

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
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import {
  createAccountTenantInfoApi,
  deleteAccountTenantInfoApi,
  getAccountInfoListApi,
  getAccountTenantInfoPageApi,
  getGroupAgentInfoListApi,
  updateAccountTenantInfoApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<PlatformAccountTenantInfo[]>([]);
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
  schema: [
    {
      fieldName: 'accountId',
      component: 'Input',
      label: '账号ID',
      componentProps: { placeholder: '请输入账号ID' },
    },
    {
      fieldName: 'tenantId',
      component: 'Input',
      label: '租户ID',
      componentProps: { placeholder: '请输入租户ID' },
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
  accountId: '',
  tenantId: '',
});

const formRules: FormRules = {
  accountId: [{ required: true, message: '请选择关联人员', trigger: 'change' }],
  tenantId: [{ required: true, message: '请选择关联智能体', trigger: 'change' }],
};

const accountOptions = ref<AccountInfo[]>([]);
const agentOptions = ref<GroupAgentInfo[]>([]);

async function loadAccountOptions() {
  try {
    const res = (await getAccountInfoListApi()) as any;
    accountOptions.value = res?.data || res || [];
  } catch {
    accountOptions.value = [];
  }
}

async function loadAgentOptions() {
  try {
    const res = (await getGroupAgentInfoListApi()) as any;
    agentOptions.value = res?.data || res || [];
  } catch {
    agentOptions.value = [];
  }
}

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getAccountTenantInfoPageApi(
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
  formData.accountId = '';
  formData.tenantId = '';
  delete formData.id;
  loadAccountOptions();
  loadAgentOptions();
  dialogVisible.value = true;
}

function handleEdit(row: PlatformAccountTenantInfo) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    accountId: row.accountId,
    tenantId: row.tenantId,
  });
  loadAccountOptions();
  loadAgentOptions();
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updateAccountTenantInfoApi({ ...formData });
      ElMessage.success('关联更新成功');
    } else {
      await createAccountTenantInfoApi({ ...formData });
      ElMessage.success('关联创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch {
    // validation or API error
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: string) {
  try {
    await ElMessageBox.confirm(
      '确定要删除该关联记录吗？此操作不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteAccountTenantInfoApi(id);
    ElMessage.success('关联删除成功');
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
      <FilterForm />

      <ElCard class="table-section" :body-style="{ padding: '20px' }">
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
          <ElTableColumn prop="accountId" label="账号ID" min-width="200" />
          <ElTableColumn prop="tenantId" label="租户ID" min-width="200" />
          <ElTableColumn prop="createTime" label="创建时间" width="180" />
          <ElTableColumn label="操作" width="160" fixed="right">
            <template #default="scope">
              <ElButton
                type="primary"
                size="small"
                @click="handleEdit(scope.row as PlatformAccountTenantInfo)"
              >
                编辑
              </ElButton>
              <ElButton
                type="danger"
                size="small"
                @click="
                  handleDelete((scope.row as PlatformAccountTenantInfo).id!)
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
        :title="isEditMode ? '编辑关联' : '新增关联'"
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
          <ElFormItem label="关联人员" prop="accountId">
            <ElSelect
              v-model="formData.accountId"
              placeholder="请选择关联人员"
              clearable
              filterable
              style="width: 100%"
            >
              <ElOption
                v-for="item in accountOptions"
                :key="item.id ?? ''"
                :label="item.realName || item.username || ''"
                :value="item.id ?? ''"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="关联智能体" prop="tenantId">
            <ElSelect
              v-model="formData.tenantId"
              placeholder="请选择关联智能体"
              clearable
              filterable
              style="width: 100%"
            >
              <ElOption
                v-for="item in agentOptions"
                :key="item.id ?? ''"
                :label="item.name || ''"
                :value="item.id ?? ''"
              />
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
</style>
