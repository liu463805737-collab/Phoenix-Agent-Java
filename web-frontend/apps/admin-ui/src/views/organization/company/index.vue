<script lang="ts" setup>
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

import type { PrivilegeCompany } from '#/api';
import {
  createCompanyApi,
  deleteCompanyApi,
  getCompanyPageApi,
  updateCompanyApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<PrivilegeCompany[]>([]);
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
      fieldName: 'cname',
      component: 'Input',
      label: '公司名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入公司名称' },
    },
    {
      fieldName: 'shortName',
      component: 'Input',
      label: '简称',
      componentProps: { placeholder: '请输入公司简称' },
    },
    {
      fieldName: 'code',
      component: 'Input',
      label: '公司编码',
      componentProps: { placeholder: '请输入公司编码' },
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
  cname: '',
  shortName: '',
  code: '',
  sort: 0,
  status: 1,
});

const formRules: FormRules = {
  cname: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入公司编码', trigger: 'blur' }],
};

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getCompanyPageApi(
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
  Object.assign(formData, {
    id: undefined,
    cname: '',
    shortName: '',
    code: '',
    sort: 0,
    status: 1,
  });
  dialogVisible.value = true;
}

function handleEdit(row: PrivilegeCompany) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    cname: row.cname,
    shortName: row.shortName,
    code: row.code,
    sort: row.sort,
    status: row.status,
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updateCompanyApi({ ...formData });
      ElMessage.success('公司更新成功');
    } else {
      await createCompanyApi({ ...formData });
      ElMessage.success('公司创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch {
    // validation or API error
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: number, name: string) {
  try {
    await ElMessageBox.confirm(
      `确定要删除公司 "${name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteCompanyApi(id);
    ElMessage.success('公司删除成功');
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
          <ElTableColumn prop="cname" label="公司名称" min-width="180" />
          <ElTableColumn prop="shortName" label="简称" width="120" />
          <ElTableColumn prop="code" label="公司编码" width="140" />
          <ElTableColumn prop="sort" label="排序" width="80" />
          <ElTableColumn label="状态" width="80">
            <template #default="scope">
              <ElTag
                :type="
                  (scope.row as PrivilegeCompany).status === 1
                    ? 'success'
                    : 'danger'
                "
                size="small"
              >
                {{
                  (scope.row as PrivilegeCompany).status === 1 ? '启用' : '禁用'
                }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="createTime" label="创建时间" width="180" />
          <ElTableColumn label="操作" width="180" fixed="right">
            <template #default="scope">
              <ElButton
                type="primary"
                size="small"
                @click="handleEdit(scope.row as PrivilegeCompany)"
              >
                编辑
              </ElButton>
              <ElButton
                type="danger"
                size="small"
                @click="
                  handleDelete(
                    (scope.row as PrivilegeCompany).id!,
                    (scope.row as PrivilegeCompany).cname!,
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
        :title="isEditMode ? '编辑公司' : '新增公司'"
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
          <ElFormItem label="公司名称" prop="cname">
            <ElInput v-model="formData.cname" placeholder="请输入公司名称" />
          </ElFormItem>
          <ElFormItem label="公司简称">
            <ElInput
              v-model="formData.shortName"
              placeholder="请输入公司简称"
            />
          </ElFormItem>
          <ElFormItem label="公司编码" prop="code">
            <ElInput v-model="formData.code" placeholder="请输入公司编码" />
          </ElFormItem>
          <ElFormItem label="排序">
            <ElInput v-model="formData.sort" placeholder="请输入排序号" type="number" />
          </ElFormItem>
          <ElFormItem label="状态">
            <ElSelect v-model="formData.status" placeholder="请选择状态">
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
  height: 100%;
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
