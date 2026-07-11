<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { Datasource, LogicalRelation } from '#/api';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElCard,
  ElCheckbox,
  ElCheckboxGroup,
  ElCol,
  ElDialog,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElRow,
  ElSelect,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  createDatasourceApi,
  deleteDatasourceApi,
  getAllDatasourcesApi,
  getDatasourceTableColumnsApi,
  getDatasourceTablesApi,
  getDatasourceTypesApi,
  getLogicalRelationsApi,
  saveLogicalRelationsApi,
  testDatasourceConnectionApi,
  updateAgentDatasourceTablesApi,
  updateDatasourceApi,
} from '#/api';

const loading = ref(false);
const tableData = ref<Datasource[]>([]);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);
const formRef = ref<FormInstance>();
const testingId = ref<null | number>(null);
const datasourceTypes = ref<{ code: string; displayName: string }[]>([]);

const agentId = ref(1);
const expandTables = ref<Record<number, string[]>>({});
const expandLoading = ref<Record<number, boolean>>({});
const expandSelected = ref<Record<number, string[]>>({});
const expandSaving = ref<Record<number, boolean>>({});

const lrDialogVisible = ref(false);
const lrLoading = ref(false);
const lrDatasourceId = ref<number>(0);
const lrList = ref<LogicalRelation[]>([]);
const lrEditingId = ref<null | number>(null);
const lrSaving = ref(false);
const tableList = ref<string[]>([]);
const sourceColumns = ref<string[]>([]);
const targetColumns = ref<string[]>([]);
const sourceColumnsLoading = ref(false);
const targetColumnsLoading = ref(false);
const lrForm = reactive<Record<string, any>>({
  sourceTableName: '',
  sourceColumnName: '',
  targetTableName: '',
  targetColumnName: '',
  relationType: 'ONE_TO_MANY',
  description: '',
});

const searchForm = reactive({
  type: '',
  status: '',
});

const formData = reactive<Record<string, any>>({
  name: '',
  type: 'MYSQL',
  host: '',
  port: 3306,
  databaseName: '',
  schema: '',
  username: '',
  password: '',
  description: '',
});

const formRules: FormRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据源类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名称', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
};

async function loadData() {
  loading.value = true;
  try {
    const res = (await getAllDatasourcesApi(
      searchForm.status || undefined,
      searchForm.type || undefined,
    )) as any;
    tableData.value = res?.data || res || [];
  } catch {
    tableData.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadTypes() {
  try {
    const res = (await getDatasourceTypesApi()) as any;
    const raw = res?.data || res || [];
    datasourceTypes.value = (Array.isArray(raw) ? raw : []).map((t: any) => ({
      code: t.value,
      displayName: t.label,
    }));
  } catch {
    datasourceTypes.value = [];
  }
}

function handleSearch() {
  loadData();
}

function handleReset() {
  searchForm.type = '';
  searchForm.status = '';
  loadData();
}

function showAddDialog() {
  isEditMode.value = false;
  formData.name = '';
  formData.type = 'MYSQL';
  formData.host = '';
  formData.port = 3306;
  formData.databaseName = '';
  formData.schema = '';
  formData.username = '';
  formData.password = '';
  formData.description = '';
  dialogVisible.value = true;
}

function handleEdit(row: Datasource) {
  isEditMode.value = true;
  formData.id = row.id;
  formData.name = row.name;
  formData.type = row.type;
  formData.host = row.host;
  formData.port = row.port;
  formData.databaseName = row.databaseName;
  formData.schema = row.schema;
  formData.username = row.username;
  formData.password = '';
  formData.description = row.description;
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitting.value = true;
    if (isEditMode.value) {
      await updateDatasourceApi(formData.id, { ...formData });
      ElMessage.success('数据源更新成功');
    } else {
      await createDatasourceApi({ ...formData });
      ElMessage.success('数据源创建成功');
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
      `确定要删除数据源 "${name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteDatasourceApi(id);
    ElMessage.success('数据源删除成功');
    loadData();
  } catch {
    // cancelled or error
  }
}

async function handleTest(id: number) {
  testingId.value = id;
  try {
    const res = (await testDatasourceConnectionApi(id)) as any;
    const result = res?.data || res;
    if (result?.success || result === true) {
      ElMessage.success('连接测试成功');
    } else {
      ElMessage.error(result?.message || '连接测试失败');
    }
  } catch {
    ElMessage.error('连接测试失败');
  } finally {
    testingId.value = null;
  }
}

async function handleToggleStatus(row: Datasource) {
  const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
  try {
    await updateDatasourceApi(row.id!, { ...row, status: newStatus });
    ElMessage.success(newStatus === 'ACTIVE' ? '数据源已启用' : '数据源已停用');
    loadData();
  } catch {
    ElMessage.error('操作失败');
  }
}

async function handleOpenLogicalRelations(id: number) {
  lrDatasourceId.value = id;
  lrDialogVisible.value = true;
  lrLoading.value = true;
  try {
    const [tablesRes, relationsRes] = await Promise.all([
      getDatasourceTablesApi(id),
      getLogicalRelationsApi(id),
    ]);
    tableList.value = (tablesRes as any)?.data || (tablesRes as any) || [];
    lrList.value = (relationsRes as any)?.data || (relationsRes as any) || [];
  } catch {
    tableList.value = [];
    lrList.value = [];
    ElMessage.error('获取数据失败');
  } finally {
    lrLoading.value = false;
  }
  resetLrForm();
}

async function handleExpandChange(row: Datasource, expanded: boolean) {
  if (!expanded || !row.id) return;
  expandLoading.value[row.id] = true;
  try {
    const res = (await getDatasourceTablesApi(row.id)) as any;
    const tables = res?.data || res || [];
    expandTables.value[row.id] = tables;
    expandSelected.value[row.id] ??= [];
  } catch {
    expandTables.value[row.id] = [];
    ElMessage.error('加载表列表失败');
  } finally {
    expandLoading.value[row.id] = false;
  }
}

async function refreshExpandTables(id: number) {
  expandLoading.value[id] = true;
  try {
    const res = (await getDatasourceTablesApi(id)) as any;
    expandTables.value[id] = res?.data || res || [];
    ElMessage.success('表列表已刷新');
  } catch {
    expandTables.value[id] = [];
    ElMessage.error('刷新失败');
  } finally {
    expandLoading.value[id] = false;
  }
}

function selectAllExpandTables(id: number) {
  expandSelected.value[id] = [...(expandTables.value[id] || [])];
}

function clearAllExpandTables(id: number) {
  expandSelected.value[id] = [];
}

async function saveExpandTables(id: number) {
  if (!id) return;
  expandSaving.value[id] = true;
  try {
    await updateAgentDatasourceTablesApi(
      agentId.value,
      id,
      expandSelected.value[id] || [],
    );
    ElMessage.success('数据表更新成功');
  } catch {
    ElMessage.error('数据表更新失败');
  } finally {
    expandSaving.value[id] = false;
  }
}

function resetLrForm() {
  lrEditingId.value = null;
  lrForm.sourceTableName = '';
  lrForm.sourceColumnName = '';
  lrForm.targetTableName = '';
  lrForm.targetColumnName = '';
  lrForm.relationType = 'ONE_TO_MANY';
  lrForm.description = '';
  sourceColumns.value = [];
  targetColumns.value = [];
}

function onSourceTableChange(tableName: string) {
  lrForm.sourceColumnName = '';
  if (!tableName) {
    sourceColumns.value = [];
    return;
  }
  sourceColumnsLoading.value = true;
  getDatasourceTableColumnsApi(lrDatasourceId.value, tableName)
    .then((res: any) => {
      sourceColumns.value = res?.data || res || [];
    })
    .catch(() => {
      sourceColumns.value = [];
    })
    .finally(() => {
      sourceColumnsLoading.value = false;
    });
}

function onTargetTableChange(tableName: string) {
  lrForm.targetColumnName = '';
  if (!tableName) {
    targetColumns.value = [];
    return;
  }
  targetColumnsLoading.value = true;
  getDatasourceTableColumnsApi(lrDatasourceId.value, tableName)
    .then((res: any) => {
      targetColumns.value = res?.data || res || [];
    })
    .catch(() => {
      targetColumns.value = [];
    })
    .finally(() => {
      targetColumnsLoading.value = false;
    });
}

function editLrRelation(row: LogicalRelation) {
  lrEditingId.value = row.id ?? null;
  lrForm.sourceTableName = row.sourceTableName;
  lrForm.sourceColumnName = row.sourceColumnName;
  lrForm.targetTableName = row.targetTableName;
  lrForm.targetColumnName = row.targetColumnName;
  lrForm.relationType = row.relationType || 'ONE_TO_MANY';
  lrForm.description = row.description;
  if (row.sourceTableName) {
    sourceColumnsLoading.value = true;
    getDatasourceTableColumnsApi(lrDatasourceId.value, row.sourceTableName)
      .then((res: any) => {
        sourceColumns.value = res?.data || res || [];
      })
      .catch(() => {
        sourceColumns.value = [];
      })
      .finally(() => {
        sourceColumnsLoading.value = false;
      });
  }
  if (row.targetTableName) {
    targetColumnsLoading.value = true;
    getDatasourceTableColumnsApi(lrDatasourceId.value, row.targetTableName)
      .then((res: any) => {
        targetColumns.value = res?.data || res || [];
      })
      .catch(() => {
        targetColumns.value = [];
      })
      .finally(() => {
        targetColumnsLoading.value = false;
      });
  }
}

function addOrUpdateLrRelation() {
  if (
    !lrForm.sourceTableName ||
    !lrForm.sourceColumnName ||
    !lrForm.targetTableName ||
    !lrForm.targetColumnName
  ) {
    ElMessage.warning('请完整填写主表、字段、关联表和字段');
    return;
  }
  if (
    lrEditingId.value &&
    lrForm.sourceTableName ===
      lrList.value.find((r) => r.id === lrEditingId.value)
        ?.sourceTableName &&
    lrForm.sourceColumnName ===
      lrList.value.find((r) => r.id === lrEditingId.value)
        ?.sourceColumnName &&
    lrForm.targetTableName ===
      lrList.value.find((r) => r.id === lrEditingId.value)
        ?.targetTableName &&
    lrForm.targetColumnName ===
      lrList.value.find((r) => r.id === lrEditingId.value)
        ?.targetColumnName
  ) {
    ElMessage.info('未检测到任何修改');
    return;
  }
  if (lrEditingId.value) {
    const index = lrList.value.findIndex(
      (r) => r.id === lrEditingId.value,
    );
    if (index !== -1) {
      lrList.value[index] = {
        ...lrList.value[index],
        sourceTableName: lrForm.sourceTableName,
        sourceColumnName: lrForm.sourceColumnName,
        targetTableName: lrForm.targetTableName,
        targetColumnName: lrForm.targetColumnName,
        relationType: lrForm.relationType || '',
        description: lrForm.description || '',
      };
    }
    ElMessage.success('更新成功，请点击"保存全部配置"以保存到数据库');
  } else {
    lrList.value.push({
      sourceTableName: lrForm.sourceTableName,
      sourceColumnName: lrForm.sourceColumnName,
      targetTableName: lrForm.targetTableName,
      targetColumnName: lrForm.targetColumnName,
      relationType: lrForm.relationType || '',
      description: lrForm.description || '',
    });
    ElMessage.success('添加成功，请点击"保存全部配置"以保存到数据库');
  }
  resetLrForm();
}

function deleteLrRelation(index: number) {
  ElMessageBox.confirm('确定要删除该逻辑外键吗？', '确认删除', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    confirmButtonType: 'danger',
  })
    .then(() => {
      lrList.value.splice(index, 1);
      if (lrEditingId.value === lrList.value[index]?.id) {
        resetLrForm();
      }
      ElMessage.success('删除成功，请点击"保存全部配置"以保存到数据库');
    })
    .catch(() => {});
}

async function saveLrConfig() {
  lrSaving.value = true;
  try {
    const payload = lrList.value.map((r) => ({
      sourceTableName: r.sourceTableName,
      sourceColumnName: r.sourceColumnName,
      targetTableName: r.targetTableName,
      targetColumnName: r.targetColumnName,
      relationType: r.relationType || '',
      description: r.description || '',
    }));
    await saveLogicalRelationsApi(lrDatasourceId.value, payload);
    ElMessage.success('保存成功');
    lrDialogVisible.value = false;
  } catch {
    ElMessage.error('保存失败');
  } finally {
    lrSaving.value = false;
  }
}

onMounted(() => {
  loadData();
  loadTypes();
});
</script>

<template>
  <Page>
    <div class="datasource-page">
      <div class="content-header">
        <div class="header-info">
          <h1 class="content-title">数据源配置</h1>
          <p class="content-subtitle">管理系统中的数据源连接，支持多种数据库类型</p>
        </div>
      </div>

      <ElCard class="action-section" :body-style="{ padding: '20px' }">
        <div class="action-content">
          <div class="action-buttons">
            <ElButton type="primary" @click="showAddDialog" size="large">
              <ElIcon><IconifyIcon icon="lucide:plus" /></ElIcon>
              新增
            </ElButton>
            <ElButton @click="loadData" size="large">
              <ElIcon><IconifyIcon icon="lucide:refresh-cw" /></ElIcon>
              刷新
            </ElButton>
          </div>
          <div class="filter-options">
            <ElSelect
              v-model="searchForm.type"
              placeholder="数据源类型"
              size="large"
              clearable
              style="width: 180px"
            >
              <ElOption
                v-for="t in datasourceTypes"
                :key="t.code"
                :label="t.displayName"
                :value="t.code"
              />
            </ElSelect>
            <ElSelect
              v-model="searchForm.status"
              placeholder="状态"
              size="large"
              clearable
              style="width: 140px"
            >
              <ElOption label="启用" value="ACTIVE" />
              <ElOption label="停用" value="INACTIVE" />
            </ElSelect>
            <ElButton type="primary" @click="handleSearch" size="large">
              <ElIcon><IconifyIcon icon="lucide:search" /></ElIcon>
              查询
            </ElButton>
            <ElButton @click="handleReset" size="large">
              <ElIcon><IconifyIcon icon="lucide:rotate-ccw" /></ElIcon>
              重置
            </ElButton>
          </div>
        </div>
      </ElCard>

      <div class="datasource-table">
        <ElCard>
          <ElTable
            :data="tableData"
            style="width: 100%"
            stripe
            v-loading="loading"
            empty-text="暂无数据"
            @expand-change="handleExpandChange"
          >
            <ElTableColumn type="expand" width="50">
              <template #default="scope">
                <div
                  v-if="(scope.row as Datasource).id && expandLoading[(scope.row as Datasource).id!]"
                  style="padding: 20px; color: #999; text-align: center"
                >
                  加载中...
                </div>
                <div
                  v-else-if="(scope.row as Datasource).id"
                  style="padding: 20px; background: #f8f9fa; border-radius: 8px"
                >
                  <div
                    style="
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      margin-bottom: 15px;
                    "
                  >
                    <h4 style="margin: 0">数据表管理</h4>
                    <ElButton
                      size="small"
                      type="primary"
                      :loading="expandLoading[(scope.row as Datasource).id!]"
                      @click="refreshExpandTables((scope.row as Datasource).id!)"
                    >
                      刷新
                    </ElButton>
                  </div>
                  <div v-if="expandTables[(scope.row as Datasource).id!]?.length">
                    <ElCheckboxGroup v-model="expandSelected[(scope.row as Datasource).id!]">
                      <ElRow :gutter="10">
                        <ElCol
                          v-for="t in expandTables[(scope.row as Datasource).id!]"
                          :key="t"
                          :span="6"
                          style="margin-bottom: 10px"
                        >
                          <ElCheckbox :label="t" size="large">{{ t }}</ElCheckbox>
                        </ElCol>
                      </ElRow>
                    </ElCheckboxGroup>
                    <div style=" display: flex; gap: 8px; justify-content: flex-end;margin-top: 20px; text-align: right">
                      <ElButton
                        size="small"
                        type="success"
                        :loading="expandSaving[(scope.row as Datasource).id!]"
                        @click="saveExpandTables((scope.row as Datasource).id!)"
                      >
                        更新数据表
                      </ElButton>
                      <ElButton
                        size="small"
                        type="primary"
                        @click="selectAllExpandTables((scope.row as Datasource).id!)"
                      >
                        全选
                      </ElButton>
                      <ElButton
                        size="small"
                        @click="clearAllExpandTables((scope.row as Datasource).id!)"
                      >
                        清空
                      </ElButton>
                    </div>
                  </div>
                  <div
                    v-else
                    style="padding: 20px; color: #999; text-align: center"
                  >
                    暂无表数据，请点击"刷新"
                  </div>
                </div>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="name" label="数据源名称" min-width="140" />
            <ElTableColumn prop="type" label="类型" width="100">
              <template #default="scope">
                <ElTag type="primary" size="small">{{ scope.row.type }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="host" label="主机地址" width="140" />
            <ElTableColumn prop="port" label="端口" width="70" />
            <ElTableColumn prop="databaseName" label="数据库" width="120" />
            <ElTableColumn prop="username" label="用户名" width="100" />
            <ElTableColumn label="状态" width="70">
              <template #default="scope">
                <ElSwitch
                  :model-value="(scope.row as Datasource).status === 'ACTIVE'"
                  @change="handleToggleStatus(scope.row as Datasource)"
                />
              </template>
            </ElTableColumn>
            <ElTableColumn label="操作" width="360" fixed="right">
              <template #default="scope">
                <div class="action-buttons-cell">
                  <ElButton
                    size="small"
                    @click="handleOpenLogicalRelations((scope.row as Datasource).id!)"
                  >
                    逻辑外键
                  </ElButton>
                  <ElButton
                    type="primary"
                    size="small"
                    :loading="testingId === (scope.row as Datasource).id"
                    @click="handleTest((scope.row as Datasource).id!)"
                  >
                    测试
                  </ElButton>
                  <ElButton
                    type="warning"
                    size="small"
                    @click="handleEdit(scope.row as Datasource)"
                  >
                    编辑
                  </ElButton>
                  <ElButton
                    type="danger"
                    size="small"
                    @click="handleDelete((scope.row as Datasource).id!, (scope.row as Datasource).name!)"
                  >
                    删除
                  </ElButton>
                </div>
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </div>

      <div v-if="!loading && tableData.length === 0" class="empty-state">
        <ElEmpty description="暂无数据源">
          <template #image>
            <ElIcon size="60"><IconifyIcon icon="lucide:database" /></ElIcon>
          </template>
          <ElButton type="primary" @click="showAddDialog">
            <ElIcon><IconifyIcon icon="lucide:plus" /></ElIcon>
            新增
          </ElButton>
        </ElEmpty>
      </div>

      <ElDialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑数据源' : '新增'"
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
          <ElFormItem label="数据源名称" prop="name">
            <ElInput v-model="formData.name" placeholder="请输入数据源名称" />
          </ElFormItem>
          <ElFormItem label="数据源类型" prop="type">
            <ElSelect v-model="formData.type" placeholder="请选择类型" style="width: 100%">
              <ElOption
                v-for="t in datasourceTypes"
                :key="t.code"
                :label="t.displayName"
                :value="t.code"
              />
            </ElSelect>
          </ElFormItem>
          <ElRow :gutter="20">
            <ElCol :span="14">
              <ElFormItem label="主机地址" prop="host">
                <ElInput v-model="formData.host" placeholder="请输入主机地址" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="10">
              <ElFormItem label="端口" prop="port">
                <ElInputNumber v-model="formData.port" :min="1" :max="65535" style="width: 100%" />
              </ElFormItem>
            </ElCol>
          </ElRow>
          <ElFormItem label="数据库名" prop="databaseName">
            <ElInput v-model="formData.databaseName" placeholder="请输入数据库名称" />
          </ElFormItem>
          <ElFormItem label="模式">
            <ElInput v-model="formData.schema" placeholder="请输入模式（可选）" />
          </ElFormItem>
          <ElRow :gutter="20">
            <ElCol :span="12">
              <ElFormItem label="用户名" prop="username">
                <ElInput v-model="formData.username" placeholder="请输入用户名" />
              </ElFormItem>
            </ElCol>
            <ElCol :span="12">
              <ElFormItem label="密码">
                <ElInput v-model="formData.password" type="password" placeholder="请输入密码" show-password />
              </ElFormItem>
            </ElCol>
          </ElRow>
          <ElFormItem label="描述">
            <ElInput v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述" />
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
        v-model="lrDialogVisible"
        title="逻辑外键配置"
        width="900px"
        :close-on-click-modal="false"
        :destroy-on-close="true"
      >
        <div v-loading="lrLoading">
          <div
            v-if="!lrLoading"
            style="padding: 10px; margin-bottom: 20px; background: #f0f9ff; border-radius: 4px"
          >
            <p style="margin: 0; font-size: 14px; color: #666">
              当前配置数据源：
              <span style="font-weight: 600; color: #1890ff">
                {{ tableData.find(d => d.id === lrDatasourceId)?.name || '' }}
              </span>
            </p>
          </div>

          <h4
            style="
              padding-left: 10px;
              margin-bottom: 15px;
              font-size: 14px;
              font-weight: 600;
              color: #333;
              border-left: 4px solid #1890ff;
            "
          >
            已生效的逻辑外键 (Logical Foreign Keys)
          </h4>

          <ElTable :data="lrList" stripe style="width: 100%" size="small">
            <ElTableColumn prop="sourceTableName" label="主表 (Source)" min-width="100px">
              <template #default="scope">
                <span style="font-family: monospace; color: #1890ff">{{ scope.row.sourceTableName }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="sourceColumnName" label="字段" min-width="80px">
              <template #default="scope">
                <span style="font-family: monospace">{{ scope.row.sourceColumnName }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn label="关系类型" min-width="90px" align="center">
              <template #default="scope">
                <ElTag size="small">{{ scope.row.relationType || '-' }}</ElTag>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="targetTableName" label="关联表 (Target)" min-width="100px">
              <template #default="scope">
                <span style="font-family: monospace; color: #52c41a">{{ scope.row.targetTableName }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="targetColumnName" label="字段" min-width="80px">
              <template #default="scope">
                <span style="font-family: monospace">{{ scope.row.targetColumnName }}</span>
              </template>
            </ElTableColumn>
            <ElTableColumn prop="description" label="描述" min-width="120px" />
            <ElTableColumn label="操作" width="140px" align="right">
              <template #default="scope">
                <ElButton
                  size="small"
                  link
                  type="primary"
                  @click="editLrRelation(scope.row as LogicalRelation)"
                >
                  编辑
                </ElButton>
                <ElButton
                  size="small"
                  link
                  type="danger"
                  @click="deleteLrRelation(scope.$index)"
                >
                  删除
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
          <div
            v-if="lrList.length === 0"
            style="
              padding: 40px;
              color: #999;
              text-align: center;
              background: #fafafa;
              border: 1px solid #e8e8e8;
              border-radius: 4px;
            "
          >
            <div>暂无逻辑外键配置</div>
          </div>

          <div
            style="
              padding: 20px;
              margin-top: 24px;
              background: #f0f9ff;
              border: 1px solid #bae7ff;
              border-radius: 8px;
            "
          >
            <h4 style="margin: 0 0 15px; font-size: 14px; font-weight: 600; color: #333">
              <span style="vertical-align: middle">
                {{ lrEditingId ? '编辑关联关系' : '新增关联关系' }}
              </span>
            </h4>

            <ElRow :gutter="10">
              <ElCol :span="5">
                <div style="margin-bottom: 5px">
                  <label style="font-size: 12px; font-weight: 600; color: #666">
                    主表 (Left Table)
                  </label>
                </div>
                <ElSelect
                  v-model="lrForm.sourceTableName"
                  placeholder="请选择表..."
                  style="width: 100%"
                  size="large"
                  @change="onSourceTableChange"
                  clearable
                  filterable
                >
                  <ElOption
                    v-for="t in tableList"
                    :key="t"
                    :label="t"
                    :value="t"
                  />
                </ElSelect>
              </ElCol>
              <ElCol :span="4">
                <div style="margin-bottom: 5px">
                  <label style="font-size: 12px; font-weight: 600; color: #666">字段</label>
                </div>
                <ElSelect
                  v-model="lrForm.sourceColumnName"
                  placeholder="先选表"
                  style="width: 100%"
                  size="large"
                  :disabled="!lrForm.sourceTableName"
                  clearable
                  filterable
                  :loading="sourceColumnsLoading"
                >
                  <ElOption
                    v-for="c in sourceColumns"
                    :key="c"
                    :label="c"
                    :value="c"
                  />
                </ElSelect>
              </ElCol>
              <ElCol :span="1" style="line-height: 70px; text-align: center">
                <ElIcon color="#999" :size="20"><IconifyIcon icon="lucide:arrow-right" /></ElIcon>
              </ElCol>
              <ElCol :span="5">
                <div style="margin-bottom: 5px">
                  <label style="font-size: 12px; font-weight: 600; color: #666">
                    关联表 (Right Table)
                  </label>
                </div>
                <ElSelect
                  v-model="lrForm.targetTableName"
                  placeholder="请选择表..."
                  style="width: 100%"
                  size="large"
                  @change="onTargetTableChange"
                  clearable
                  filterable
                >
                  <ElOption
                    v-for="t in tableList"
                    :key="t"
                    :label="t"
                    :value="t"
                  />
                </ElSelect>
              </ElCol>
              <ElCol :span="4">
                <div style="margin-bottom: 5px">
                  <label style="font-size: 12px; font-weight: 600; color: #666">字段</label>
                </div>
                <ElSelect
                  v-model="lrForm.targetColumnName"
                  placeholder="先选表"
                  style="width: 100%"
                  size="large"
                  :disabled="!lrForm.targetTableName"
                  clearable
                  filterable
                  :loading="targetColumnsLoading"
                >
                  <ElOption
                    v-for="c in targetColumns"
                    :key="c"
                    :label="c"
                    :value="c"
                  />
                </ElSelect>
              </ElCol>
              <ElCol :span="5" style="line-height: 70px">
                <ElButton
                  type="primary"
                  size="large"
                  style="width: 100%"
                  @click="addOrUpdateLrRelation"
                >
                  {{ lrEditingId ? '更新' : '添加' }}
                </ElButton>
              </ElCol>
            </ElRow>

            <ElRow style="margin-top: 10px">
              <ElCol :span="24">
                <div style="margin-bottom: 5px">
                  <label style="font-size: 12px; font-weight: 600; color: #666">
                    关系类型 (Relation Type)
                  </label>
                </div>
                <ElSelect
                  v-model="lrForm.relationType"
                  size="large"
                  clearable
                  style="width: 100%"
                >
                  <ElOption label="1:1 (一对一)" value="ONE_TO_ONE" />
                  <ElOption label="1:N (一对多)" value="ONE_TO_MANY" />
                  <ElOption label="N:1 (多对一)" value="MANY_TO_ONE" />
                  <ElOption label="M:N (多对多)" value="MANY_TO_MANY" />
                </ElSelect>
              </ElCol>
            </ElRow>

            <ElRow style="margin-top: 10px">
              <ElCol :span="24">
                <ElInput
                  v-model="lrForm.description"
                  placeholder="描述（可选）：例如 '订单关联用户'，帮助 LLM 理解语义"
                  size="large"
                  clearable
                />
              </ElCol>
            </ElRow>
          </div>
        </div>

        <template #footer>
          <div style="text-align: right">
            <ElButton @click="lrDialogVisible = false" size="large">取消</ElButton>
            <ElButton
              type="primary"
              size="large"
              :loading="lrSaving"
              @click="saveLrConfig"
            >
              保存全部配置
            </ElButton>
          </div>
        </template>
      </ElDialog>
    </div>
  </Page>
</template>

<style scoped>
.datasource-page {
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue',
    Arial, sans-serif;
  background: #f8fafc;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0.75rem;
  margin-bottom: 1.5rem;
}

.header-info h1 {
  margin: 0 0 0.25rem;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
}

.header-info p {
  margin: 0;
  font-size: 0.95rem;
  color: #6b7280;
}

.action-section {
  margin-bottom: 1.5rem;
  border-radius: 12px;
}

.action-content {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
  justify-content: space-between;
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
}

.filter-options {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.datasource-table {
  margin-bottom: 1.5rem;
}

.action-buttons-cell {
  display: flex;
  gap: 0.35rem;
}

.empty-state {
  padding: 3rem 1.5rem;
}

.empty-hint {
  padding: 2rem 0;
  color: #9ca3af;
  text-align: center;
}

.lr-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 20px;
}
</style>
