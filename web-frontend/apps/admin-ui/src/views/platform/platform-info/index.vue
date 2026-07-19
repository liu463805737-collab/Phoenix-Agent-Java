<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { PlatformInfo } from '#/api';

import { onMounted, reactive, ref } from 'vue';

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
  createPlatformInfoApi,
  deletePlatformInfoApi,
  getPlatformInfoPageApi,
  updatePlatformInfoApi,
} from '#/api/core/platform-info';

const loading = ref(false);
const tableData = ref<PlatformInfo[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();

const platformTypeOptions = [
  { value: 'dingtalk', label: '钉钉' },
  { value: 'feishu', label: '飞书' },
  { value: 'weixin', label: '企业微信' },
];

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
      label: '平台名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入平台名称' },
    },
    {
      fieldName: 'type',
      component: 'Select',
      label: '平台类型',
      componentProps: {
        placeholder: '请选择平台类型',
        options: platformTypeOptions,
        style: { width: '200px' },
      },
    },
  ],
  handleSubmit: (values) => {
    page.value = 1;
    const params = Object.fromEntries(
        Object.entries(values).filter(([, v]) => v !== '' && v != null),
    );
    loadData(params);
  },
  handleReset: () => {
    page.value = 1;
    loadData({});
  },
});

const formData = reactive<Record<string, any>>({
  id: undefined,
  type: '',
  name: '',
  status: '0',
  corpid: '',
  corpsecret: '',
  agentid: '',
  appKey: '',
});

const formRules: FormRules = {
  name: [{ required: true, message: '请输入平台名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择平台类型', trigger: 'change' }],
};

async function loadData(params: Record<string, any> = {}) {
  loading.value = true;
  try {
    const res = (await getPlatformInfoPageApi(
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
    type: '',
    name: '',
    status: '0',
    corpid: '',
    corpsecret: '',
    agentid: '',
    appKey: '',
  });
  dialogVisible.value = true;
}

function handleEdit(row: PlatformInfo) {
  isEditMode.value = true;
  Object.assign(formData, {
    id: row.id,
    type: row.type,
    name: row.name,
    status: row.status ?? '0',
    corpid: row.corpid,
    corpsecret: row.corpsecret,
    agentid: row.agentid,
    appKey: row.appKey,
  });
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;
    submitting.value = true;
    if (isEditMode.value) {
      await updatePlatformInfoApi({ ...formData });
      ElMessage.success('平台更新成功');
    } else {
      await createPlatformInfoApi({ ...formData });
      ElMessage.success('平台创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch {
    ElMessage.error(isEditMode.value ? '平台更新失败' : '平台创建失败');
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: string, name: string) {
  try {
    await ElMessageBox.confirm(
        `确定要删除平台 "${name}" 吗？此操作不可恢复。`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          confirmButtonType: 'danger',
        },
    );
    await deletePlatformInfoApi(id);
    ElMessage.success('平台删除成功');
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
            :data="tableData"
            style="width: 100%"
            stripe
            v-loading="loading"
            empty-text="暂无数据"
        >
          <ElTableColumn prop="name" label="平台名称" min-width="150" />
          <ElTableColumn label="平台类型" width="110">
            <template #default="scope">
              <ElTag type="primary" size="small">
                {{
                  scope.row.type === 'dingtalk' ? '钉钉' :
                      scope.row.type === 'feishu' ? '飞书' :
                          scope.row.type === 'weixin' ? '企业微信' : scope.row.type
                }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn label="状态" width="80">
            <template #default="scope">
              <ElTag
                  :type="scope.row.status === '1' ? 'success' : 'danger'"
                  size="small"
              >
                {{ scope.row.status === '1' ? '启用' : '禁用' }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="corpid" label="企业ID" width="180" />
          <ElTableColumn label="通讯录Secret" width="220">
            <template #default="scope">
              <span class="secret-text">{{ scope.row.corpsecret?.slice(0, 8) }}******</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="agentid" label="AgentId" width="100" />
          <ElTableColumn prop="appKey" label="登录AppKey" width="200" />
          <ElTableColumn label="创建时间" width="170">
            <template #default="scope">
              {{ scope.row.createTime?.replace('T', ' ')?.slice(0, 16) }}
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="180" fixed="right">
            <template #default="scope">
              <ElButton
                  type="primary"
                  size="small"
                  @click="handleEdit(scope.row as PlatformInfo)"
              >
                编辑
              </ElButton>
              <ElButton
                  type="danger"
                  size="small"
                  @click="
                  handleDelete(
                    (scope.row as PlatformInfo).id!,
                    (scope.row as PlatformInfo).name!,
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
          :title="isEditMode ? '编辑三方平台' : '新增三方平台'"
          width="550px"
          :close-on-click-modal="false"
      >
        <ElForm
            ref="formRef"
            :model="formData"
            :rules="formRules"
            label-width="110px"
            label-position="left"
        >
          <ElFormItem label="平台名称" prop="name">
            <ElInput v-model="formData.name" placeholder="请输入平台名称" />
          </ElFormItem>
          <ElFormItem label="平台类型" prop="type">
            <ElSelect v-model="formData.type" placeholder="请选择平台类型" style="width: 100%">
              <ElOption
                  v-for="opt in platformTypeOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="状态">
            <ElSelect v-model="formData.status" placeholder="请选择状态">
              <ElOption label="启用" value="1" />
              <ElOption label="禁用" value="0" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem label="企业ID(CorpId)">
            <ElInput v-model="formData.corpid" placeholder="请输入企业ID" />
          </ElFormItem>
          <ElFormItem label="通讯录Secret">
            <ElInput
                v-model="formData.corpsecret"
                placeholder="请输入通讯录Secret"
                type="password"
                show-password
            />
          </ElFormItem>
          <ElFormItem label="AgentId">
            <ElInput v-model="formData.agentid" placeholder="请输入AgentId" />
          </ElFormItem>
          <ElFormItem label="登录AppKey">
            <ElInput v-model="formData.appKey" placeholder="请输入登录认证AppKey（钉钉/飞书OAuth用）" />
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

.secret-text {
  font-family: monospace;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}
</style>
