<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { PrivilegePvalue } from '#/api';

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
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElPagination,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import {
  createPvalueApi,
  deletePvalueApi,
  getPvaluePageApi,
  updatePvalueApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<PrivilegePvalue[]>([]);
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
      label: '权限名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入权限名称' },
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
  position: 0,
  orderNo: 0,
  remark: '',
  systemId: 110,
});

const formRules: FormRules = {
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  position: [{ required: true, message: '请输入位值', trigger: 'blur' }],
};

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getPvaluePageApi(
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
  formData.position = 0;
  formData.orderNo = 0;
  formData.remark = '';
  formData.systemId = 110;
  dialogVisible.value = true;
}

function handleEdit(row: PrivilegePvalue) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    position: row.position,
    orderNo: row.orderNo,
    remark: row.remark,
    systemId: row.systemId,
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updatePvalueApi({ ...formData });
      ElMessage.success('权限值更新成功');
    } else {
      await createPvalueApi({ ...formData });
      ElMessage.success('权限值创建成功');
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
      `确定要删除权限值 "${name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deletePvalueApi(id);
    ElMessage.success('权限值删除成功');
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
          <ElTableColumn prop="name" label="权限名称" min-width="150" />
          <ElTableColumn prop="position" label="位值" width="80" />
          <ElTableColumn prop="orderNo" label="排序号" width="80" />
          <ElTableColumn prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <ElTableColumn prop="createTime" label="创建时间" width="180" />
          <ElTableColumn label="操作" width="180" fixed="right">
            <template #default="scope">
              <ElButton
                type="primary"
                size="small"
                @click="handleEdit(scope.row as PrivilegePvalue)"
              >
                编辑
              </ElButton>
              <ElButton
                type="danger"
                size="small"
                @click="
                  handleDelete(
                    (scope.row as PrivilegePvalue).id!,
                    (scope.row as PrivilegePvalue).name!,
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
        :title="isEditMode ? '编辑权限值' : '新增权限值'"
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
          <ElFormItem label="权限名称" prop="name">
            <ElInput v-model="formData.name" placeholder="请输入权限名称" />
          </ElFormItem>
          <ElFormItem label="位值" prop="position">
            <ElInputNumber
              v-model="formData.position"
              :min="0"
              :max="63"
              style="width: 100%"
            />
          </ElFormItem>
          <ElFormItem label="排序号">
            <ElInputNumber
              v-model="formData.orderNo"
              :min="0"
              :max="999"
              style="width: 100%"
            />
          </ElFormItem>
          <ElFormItem label="备注">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注"
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
