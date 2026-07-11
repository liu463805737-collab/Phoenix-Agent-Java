<script lang="ts" setup>
import type { UploadFile } from 'element-plus';

import type { AgentDatasource } from '#/api/core/datasource';
import type { BatchImportResult } from '#/api/core/semanticModel';
import type { SemanticModel, SemanticModelAddDto, SemanticModelImportItem } from '#/api/core/semanticModel';

import { computed, onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElRadioButton,
  ElRadioGroup,
  ElResult,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag,
  ElUpload,
} from 'element-plus';

import {
  batchDeleteSemanticModelApi,
  batchImportSemanticModelApi,
  createSemanticModelApi,
  deleteSemanticModelApi,
  disableSemanticModelApi,
  downloadSemanticModelTemplateApi,
  enableSemanticModelApi,
  getAgentDatasourcesApi,
  getDatasourceTableColumnNamesApi,
  getDatasourceTableColumnsApi,
  getDatasourceTablesApi,
  getSemanticModelListApi,
  importSemanticModelExcelApi,
  updateSemanticModelApi,
} from '#/api';

defineOptions({ name: 'SemanticsConfig' });

const props = defineProps<{ agentId: number }>();

const semanticModelList = ref<SemanticModel[]>([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const searchKeyword = ref('');
const selectedModels = ref<SemanticModel[]>([]);
const currentEditId = ref<null | number>(null);

const modelForm = ref<SemanticModel>({
  tableName: '',
  columnName: '',
  businessName: '',
  synonyms: '',
  businessDescription: '',
  columnComment: '',
  dataType: '',
  status: 1,
  agentId: props.agentId,
} as SemanticModel);

const agentDatasources = ref<AgentDatasource[]>([]);
const cascTables = ref<string[]>([]);
const cascColumns = ref<string[]>([]);
const cascDsLoading = ref(false);
const cascTableLoading = ref(false);
const cascColumnLoading = ref(false);

// 数据库字段名查询
const columnQueryDialogVisible = ref(false);
const queryDsId = ref<number>();
const queryDsLoading = ref(false);
const queryTables = ref<string[]>([]);
const queryTableLoading = ref(false);
const queryTableName = ref('');
const queryColumnSearch = ref('');
const queryColumns = ref<string[]>([]);
const queryColumnLoading = ref(false);

async function openColumnQueryDialog() {
  columnQueryDialogVisible.value = true;
  queryDsId.value = undefined;
  queryTables.value = [];
  queryTableName.value = '';
  queryColumns.value = [];
  queryColumnSearch.value = '';
  queryDsLoading.value = true;
  try {
    const res = (await getAgentDatasourcesApi(props.agentId)) as any;
    agentDatasources.value = res?.data || res || [];
  } catch {
    agentDatasources.value = [];
  } finally {
    queryDsLoading.value = false;
  }
}

async function onQueryDatasourceChange(dsId: any) {
  queryTables.value = [];
  queryTableName.value = '';
  queryColumns.value = [];
  queryColumnSearch.value = '';
  if (!dsId) return;
  queryTableLoading.value = true;
  try {
    const res = (await getDatasourceTablesApi(dsId)) as any;
    queryTables.value = res?.data || res || [];
  } catch {
    queryTables.value = [];
  } finally {
    queryTableLoading.value = false;
  }
}

async function onQueryTableChange(tableName: string) {
  queryColumns.value = [];
  queryColumnSearch.value = '';
  if (!tableName || queryDsId.value == null) return;
  queryColumnLoading.value = true;
  try {
    queryColumns.value = await getDatasourceTableColumnNamesApi(queryDsId.value, tableName);
  } catch {
    queryColumns.value = [];
  } finally {
    queryColumnLoading.value = false;
  }
}

const filteredQueryColumns = computed(() => {
  const cols = !queryColumnSearch.value
    ? queryColumns.value
    : queryColumns.value.filter((c) =>
        c.toLowerCase().includes(queryColumnSearch.value.toLowerCase()),
      );
  return cols.map((name) => ({ name }));
});

function useColumnInForm(colName: string) {
  columnQueryDialogVisible.value = false;
  openCreateDialog();
  modelForm.value.columnName = colName;
  modelForm.value.tableName = queryTableName.value;
}

async function loadAgentDatasources() {
  cascDsLoading.value = true;
  try {
    const res = (await getAgentDatasourcesApi(props.agentId)) as any;
    agentDatasources.value = res?.data || res || [];
  } catch {
    agentDatasources.value = [];
  } finally {
    cascDsLoading.value = false;
  }
}

async function onDatasourceChange(dsId: number) {
  cascTables.value = [];
  cascColumns.value = [];
  modelForm.value.tableName = '';
  modelForm.value.columnName = '';
  modelForm.value.dataType = '';
  if (!dsId) return;
  cascTableLoading.value = true;
  try {
    const res = (await getDatasourceTablesApi(dsId)) as any;
    cascTables.value = res?.data || res || [];
  } catch {
    cascTables.value = [];
  } finally {
    cascTableLoading.value = false;
  }
}

async function onTableChange(tableName: string) {
  cascColumns.value = [];
  modelForm.value.columnName = '';
  modelForm.value.dataType = '';
  if (!tableName) return;
  const dsId = findSelectedDsId();
  if (!dsId) return;
  cascColumnLoading.value = true;
  try {
    const res = (await getDatasourceTableColumnsApi(dsId, tableName)) as any;
    cascColumns.value = res?.data || res || [];
  } catch {
    cascColumns.value = [];
  } finally {
    cascColumnLoading.value = false;
  }
}

function onColumnChange(colName: string) {
  modelForm.value.columnName = colName;
}

function findSelectedDsId(): null | number {
  const idx = agentDatasources.value.findIndex(
    (ds) => ds.datasource?.id === selectedDsId.value,
  );
  if (idx === -1 || !agentDatasources.value[idx]?.datasource?.id) return null;
  return agentDatasources.value[idx].datasource!.id!;
}

const selectedDsId = ref<null | number>(null);

function openCreateDialog() {
  isEdit.value = false;
  currentEditId.value = null;
  selectedDsId.value = null;
  cascTables.value = [];
  cascColumns.value = [];
  modelForm.value = {
    tableName: '',
    columnName: '',
    businessName: '',
    synonyms: '',
    businessDescription: '',
    columnComment: '',
    dataType: '',
    status: 1,
    agentId: props.agentId,
  } as SemanticModel;
  dialogVisible.value = true;
  loadAgentDatasources();
}

function editModel(model: SemanticModel) {
  isEdit.value = true;
  currentEditId.value = model.id || null;
  modelForm.value = { ...model };
  dialogVisible.value = true;
}

function handleSearch() {
  loadSemanticModels();
}

function handleSelectionChange(selection: unknown[]) {
  selectedModels.value = selection as SemanticModel[];
}

async function loadSemanticModels() {
  try {
    semanticModelList.value = (await getSemanticModelListApi(props.agentId, searchKeyword.value || undefined)) ?? [];
  } catch {
    ElMessage.error('加载语义模型列表失败');
  }
}

async function handleDeleteModel(model: SemanticModel) {
  if (!model.id) return;
  try {
    await ElMessageBox.confirm(`确定要删除语义模型 "${model.businessName}" 吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    await deleteSemanticModelApi(model.id!);
    ElMessage.success('删除成功');
    await loadSemanticModels();
  } catch {
    // cancelled
  }
}

async function handleToggleStatus(model: SemanticModel, status: number) {
  if (!model.id) return;
  try {
    const ids = [model.id];
    if (status === 1) {
      await enableSemanticModelApi(ids);
    } else {
      await disableSemanticModelApi(ids);
    }
    ElMessage.success(`${status === 1 ? '启用' : '停用'}成功`);
    model.status = status;
  } catch {
    ElMessage.error(`${status === 1 ? '启用' : '停用'}失败`);
  }
}

async function handleBatchDelete() {
  if (selectedModels.value.length === 0) {
    ElMessage.warning('请先选择要删除的语义模型');
    return;
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedModels.value.length} 个语义模型吗？`, '确认批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    const ids = selectedModels.value.map((m) => m.id).filter((id) => id !== undefined) as number[];
    await batchDeleteSemanticModelApi(ids);
    ElMessage.success(`成功删除 ${ids.length} 个语义模型`);
    selectedModels.value = [];
    await loadSemanticModels();
  } catch {
    // cancelled
  }
}

async function handleSave() {
  try {
    if (isEdit.value && currentEditId.value) {
      const formData: SemanticModel = { ...modelForm.value, id: currentEditId.value };
      await updateSemanticModelApi(currentEditId.value, formData);
      ElMessage.success('更新成功');
    } else {
      const formData: SemanticModelAddDto = {
        agentId: props.agentId,
        tableName: modelForm.value.tableName,
        columnName: modelForm.value.columnName,
        businessName: modelForm.value.businessName,
        synonyms: modelForm.value.synonyms,
        businessDescription: modelForm.value.businessDescription,
        columnComment: modelForm.value.columnComment,
        dataType: modelForm.value.dataType,
      };
      await createSemanticModelApi(formData);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    await loadSemanticModels();
  } catch {
    ElMessage.error(`${isEdit.value ? '更新' : '创建'}失败`);
  }
}

function formatDateTime(dateTime: string | undefined): string {
  if (!dateTime) return '-';
  try {
    return new Date(dateTime).toLocaleString('zh-CN', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false,
    });
  } catch {
    return dateTime;
  }
}

// Batch import
const batchImportDialogVisible = ref(false);
const importMode = ref('json');
const importJsonText = ref('');
const importTab = ref('input');
const importResult = ref<BatchImportResult | null>(null);
const importing = ref(false);
const uploadedFile = ref<File | null>(null);

const jsonTemplate = [
  { tableName: 'work_order', columnName: 'order_type', businessName: '工单类型', synonyms: '类型,工单种类', businessDesc: '枚举值：1=资产工单, 2=账号工单', dataType: 'int' },
  { tableName: 'work_order', columnName: 'status', businessName: '工单状态', synonyms: '状态,处理状态', businessDesc: '枚举值：0=待处理 1=处理中 2=已完成 3=已关闭', dataType: 'int' },
];

function openBatchImportDialog() {
  batchImportDialogVisible.value = true;
  importMode.value = 'json';
  importJsonText.value = '';
  importResult.value = null;
  importTab.value = 'input';
  uploadedFile.value = null;
}

function loadTemplate() {
  importJsonText.value = JSON.stringify(jsonTemplate, null, 2);
  ElMessage.success('模板已加载');
}

function handleValidateJson(): boolean {
  try {
    const data = JSON.parse(importJsonText.value);
    if (!Array.isArray(data)) {
      ElMessage.error('JSON格式错误：数据必须是数组');
      return false;
    }
    if (data.length === 0) {
      ElMessage.error('导入数据不能为空');
      return false;
    }
    for (let i = 0; i < data.length; i++) {
      const item = data[i];
      if (!item.tableName || !item.columnName || !item.businessName || !item.dataType) {
        ElMessage.error(`第${i + 1}条记录缺少必填字段（tableName, columnName, businessName, dataType）`);
        return false;
      }
    }
    ElMessage.success('JSON格式验证通过');
    return true;
  } catch (error) {
    ElMessage.error('JSON格式错误：' + (error as Error).message);
    return false;
  }
}

async function handleJsonImport() {
  if (!handleValidateJson()) return;
  let items: SemanticModelImportItem[];
  try {
    items = JSON.parse(importJsonText.value);
  } catch {
    ElMessage.error('JSON格式错误');
    return;
  }
  try {
    importing.value = true;
    const result = await batchImportSemanticModelApi({ agentId: props.agentId, items });
    importResult.value = result;
    importTab.value = 'result';
    if (result.failCount === 0) {
      ElMessage.success(`批量导入成功！共导入${result.successCount}条记录`);
    } else {
      ElMessage.warning(`批量导入完成！成功${result.successCount}条，失败${result.failCount}条`);
    }
    await loadSemanticModels();
  } catch {
    ElMessage.error('批量导入失败');
  } finally {
    importing.value = false;
  }
}

function handleFileChange(file: UploadFile) {
  uploadedFile.value = file.raw ?? null;
}

async function handleExcelImport() {
  if (!uploadedFile.value) return;
  try {
    importing.value = true;
    const result = await importSemanticModelExcelApi(uploadedFile.value, props.agentId);
    importResult.value = result;
    importTab.value = 'result';
    if (result.failCount === 0) {
      ElMessage.success(`Excel导入成功！共导入${result.successCount}条记录`);
    } else {
      ElMessage.warning(`Excel导入完成！成功${result.successCount}条，失败${result.failCount}条`);
    }
    await loadSemanticModels();
  } catch {
    ElMessage.error('Excel导入失败');
  } finally {
    importing.value = false;
  }
}

async function handleDownloadExcelTemplate() {
  try {
    await downloadSemanticModelTemplateApi();
    ElMessage.success('模板下载成功');
  } catch {
    ElMessage.error('模板下载失败');
  }
}

onMounted(loadSemanticModels);
</script>

<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <h3 class="m-0 text-base font-semibold">语义模型配置</h3>
        <p class="mt-1 text-sm text-gray-500">配置字段级别的业务语义信息</p>
        <ElButton v-if="selectedModels.length > 0" size="small" type="danger" plain @click="handleBatchDelete" class="ml-2">
          批量删除 ({{ selectedModels.length }})
        </ElButton>
      </div>
      <div class="flex gap-2">
        <ElInput
          v-model="searchKeyword"
          placeholder="搜索关键词..."
          clearable
          style="width: 200px"
          size="small"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <ElIcon><IconifyIcon icon="lucide:search" /></ElIcon>
          </template>
        </ElInput>
        <ElButton size="small" @click="openColumnQueryDialog">数据库字段名查询</ElButton>
        <ElButton size="small" type="success" @click="openBatchImportDialog">批量导入</ElButton>
        <ElButton size="small" type="primary" @click="openCreateDialog">添加语义模型</ElButton>
      </div>
    </div>

    <ElTable :data="semanticModelList" border stripe @selection-change="handleSelectionChange" empty-text="暂无语义模型" class="w-full">
      <ElTableColumn type="selection" width="55" />
      <ElTableColumn prop="tableName" label="表名" min-width="120" />
      <ElTableColumn prop="columnName" label="数据库字段名" min-width="120" />
      <ElTableColumn prop="businessName" label="业务名称" min-width="120" />
      <ElTableColumn prop="synonyms" label="同义词" min-width="120" show-overflow-tooltip />
      <ElTableColumn prop="dataType" label="数据类型" width="90" />
      <ElTableColumn label="状态" width="80">
        <template #default="{ row }">
          <ElTag :type="row.status === 1 ? 'success' : 'info'" round>{{ row.status === 1 ? '启用' : '停用' }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="创建时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdTime || row.updateTime) }}</template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <ElButton size="small" link type="primary" @click="editModel(row as any)">编辑</ElButton>
          <ElButton v-if="(row as any).status === 0" size="small" link type="success" @click="handleToggleStatus(row as any, 1)">启用</ElButton>
          <ElButton v-else size="small" link type="warning" @click="handleToggleStatus(row as any, 0)">停用</ElButton>
          <ElButton size="small" link type="danger" @click="handleDeleteModel(row as any)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog v-model="dialogVisible" :title="isEdit ? '编辑语义模型' : '添加语义模型'" width="700" :close-on-click-modal="false">
      <ElForm :model="modelForm" label-width="120px">
        <ElFormItem label="数据源" v-if="!isEdit">
          <ElSelect
            v-model="selectedDsId"
            placeholder="请选择数据源"
            :loading="cascDsLoading"
            filterable
            clearable
            @change="onDatasourceChange"
          >
            <ElOption
              v-for="ds in agentDatasources"
              :key="ds.datasource?.id"
              :label="ds.datasource?.name"
              :value="ds.datasource?.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="表名" required>
          <template v-if="!isEdit">
            <ElSelect
              v-model="modelForm.tableName"
              placeholder="请先选择数据源"
              :loading="cascTableLoading"
              :disabled="!selectedDsId"
              filterable
              clearable
              @change="onTableChange"
            >
              <ElOption
                v-for="t in cascTables"
                :key="t"
                :label="t"
                :value="t"
              />
            </ElSelect>
          </template>
          <template v-else>
            <ElInput v-model="modelForm.tableName" placeholder="请输入表名" />
          </template>
        </ElFormItem>
        <ElFormItem label="数据库字段名" required>
          <template v-if="!isEdit">
            <ElSelect
              v-model="modelForm.columnName"
              placeholder="请先选择表"
              :loading="cascColumnLoading"
              :disabled="!modelForm.tableName"
              filterable
              clearable
              @change="onColumnChange"
            >
              <ElOption
                v-for="c in cascColumns"
                :key="c"
                :label="c"
                :value="c"
              />
            </ElSelect>
          </template>
          <template v-else>
            <ElInput v-model="modelForm.columnName" placeholder="请输入数据库字段名" />
          </template>
        </ElFormItem>
        <ElFormItem label="业务名称" required>
          <ElInput v-model="modelForm.businessName" placeholder="请输入业务名称" />
        </ElFormItem>
        <ElFormItem label="同义词">
          <ElInput v-model="modelForm.synonyms" type="textarea" :rows="2" placeholder="多个同义词用逗号分隔" />
        </ElFormItem>
        <ElFormItem label="业务描述">
          <ElInput v-model="modelForm.businessDescription" type="textarea" :rows="3" placeholder="向LLM解释字段的业务含义" />
        </ElFormItem>
        <ElFormItem label="字段注释">
          <ElInput v-model="modelForm.columnComment" type="textarea" :rows="2" placeholder="数据库字段的原始注释" />
        </ElFormItem>
        <ElFormItem label="数据类型" required>
          <ElInput v-model="modelForm.dataType" placeholder="如：int, varchar(20)" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSave">{{ isEdit ? '更新' : '创建' }}</ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="batchImportDialogVisible" title="批量导入语义模型" width="900" :close-on-click-modal="false">
      <ElAlert title="导入说明" type="info" :closable="false" style="margin-bottom: 20px">
        <p>1. 支持JSON和Excel两种导入方式</p>
        <p>2. 必填字段：表名、字段名、业务名称、数据类型</p>
      </ElAlert>

      <ElRadioGroup v-model="importMode" style="margin-bottom: 20px">
        <ElRadioButton value="json">JSON导入</ElRadioButton>
        <ElRadioButton value="excel">Excel导入</ElRadioButton>
      </ElRadioGroup>

      <ElTabs v-model="importTab">
        <ElTabPane label="JSON输入" name="input" v-if="importMode === 'json'">
          <ElInput v-model="importJsonText" type="textarea" :rows="15" placeholder="请输入JSON格式的数据..." style="font-family: 'Courier New', monospace" />
          <div style="margin-top: 10px; text-align: right">
            <ElButton size="small" @click="loadTemplate">加载模板</ElButton>
            <ElButton size="small" type="primary" @click="handleValidateJson">验证JSON</ElButton>
          </div>
        </ElTabPane>

        <ElTabPane label="Excel上传" name="input" v-if="importMode === 'excel'">
          <div style="padding: 40px 0; text-align: center">
            <ElUpload :auto-upload="false" :limit="1" accept=".xlsx,.xls" :on-change="handleFileChange" drag>
              <ElIcon class="el-icon--upload"><IconifyIcon icon="lucide:upload" /></ElIcon>
              <div class="el-upload__text">将Excel文件拖到此处，或<em>点击上传</em></div>
              <template #tip><div class="el-upload__tip">只能上传 xlsx/xls 文件</div></template>
            </ElUpload>
            <div style="margin-top: 20px">
              <ElButton @click="handleDownloadExcelTemplate" type="primary" plain>下载Excel模板</ElButton>
            </div>
          </div>
        </ElTabPane>

        <ElTabPane label="导入结果" name="result" :disabled="!importResult">
          <ElResult
v-if="importResult" :icon="importResult.failCount === 0 ? 'success' : 'warning'"
            :title="importResult.failCount === 0 ? '导入成功' : '部分导入失败'"
>
            <template #sub-title>
              <p>总数：{{ importResult.total }} | 成功：{{ importResult.successCount }} | 失败：{{ importResult.failCount }}</p>
            </template>
            <template #extra>
              <div v-if="importResult.errors.length > 0" style="max-height: 300px; overflow-y: auto; text-align: left">
                <ElAlert v-for="(error, idx) in importResult.errors" :key="idx" :title="error" type="error" :closable="false" style="margin-bottom: 10px" />
              </div>
            </template>
          </ElResult>
        </ElTabPane>
      </ElTabs>

      <template #footer>
        <ElButton @click="batchImportDialogVisible = false">关闭</ElButton>
        <ElButton type="primary" :loading="importing" @click="handleJsonImport" v-if="importMode === 'json'">开始导入</ElButton>
        <ElButton type="primary" :loading="importing" @click="handleExcelImport" v-if="importMode === 'excel'">开始导入</ElButton>
      </template>
    </ElDialog>

    <ElDialog v-model="columnQueryDialogVisible" title="数据库字段名查询" width="650" :close-on-click-modal="false">
      <ElForm label-width="100px">
        <ElFormItem label="数据源">
          <ElSelect
            :model-value="queryDsId"
            placeholder="请选择数据源"
            :loading="queryDsLoading"
            filterable
            clearable
            @update:model-value="(v: any) => { queryDsId = v; onQueryDatasourceChange(v); }"
          >
            <ElOption
              v-for="ds in agentDatasources"
              :key="ds.datasource?.id"
              :label="ds.datasource?.name"
              :value="ds.datasource?.id"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="表名">
          <ElSelect
            v-model="queryTableName"
            placeholder="请先选择数据源"
            :loading="queryTableLoading"
            :disabled="!queryDsId"
            filterable
            clearable
            @change="onQueryTableChange"
          >
            <ElOption
              v-for="t in queryTables"
              :key="t"
              :label="t"
              :value="t"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="字段搜索">
          <ElInput
            v-model="queryColumnSearch"
            placeholder="输入字段名关键词搜索..."
            clearable
            :disabled="queryColumns.length === 0"
          />
        </ElFormItem>
      </ElForm>

      <ElTable
        :data="filteredQueryColumns"
        border
        stripe
        empty-text="暂无字段数据"
        max-height="380"
        style="width: 100%"
        :loading="queryColumnLoading"
      >
        <ElTableColumn type="index" label="序号" width="60" />
        <ElTableColumn label="字段名" min-width="200">
          <template #default="{ row }">
            <code style=" padding: 2px 6px; font-size: 13px;background: #f5f5f5; border-radius: 3px;">{{ (row as any).name }}</code>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="120">
          <template #default="{ row }">
            <ElButton size="small" type="primary" link @click="useColumnInForm((row as any).name)">使用该字段</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div style="margin-top: 8px; font-size: 12px; color: #999;">
        共 {{ queryColumns.length }} 个字段，匹配 {{ filteredQueryColumns.length }} 个
      </div>

      <template #footer>
        <ElButton @click="columnQueryDialogVisible = false">关闭</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
