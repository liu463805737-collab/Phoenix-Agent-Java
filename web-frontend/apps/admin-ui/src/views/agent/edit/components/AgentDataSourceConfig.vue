<script lang="ts" setup>
import type {
  Datasource,
  LogicalRelation,
} from '#/api/core/datasource';

import { onMounted, ref, watch } from 'vue';

import {
  Check,
  CirclePlus,
  Connection,
  Edit as EditIcon,
  FolderOpened,
  Link,
  Loading,
  Lock,
  Plus,
  Right,
} from '@element-plus/icons-vue';
import {
  ElButton,
  ElCheckbox,
  ElCheckboxGroup,
  ElCol,
  ElDialog,
  ElDivider,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElRow,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag,
  ElTooltip,
} from 'element-plus';

import {
  addAgentDatasourceApi,
  createDatasourceApi,
  deleteDatasourceApi,
  getAgentDatasourcesApi,
  getAllDatasourcesApi,
  getDatasourceTableColumnsApi,
  getDatasourceTablesApi,
  getDatasourceTypesApi,
  getLogicalRelationsApi,
  removeAgentDatasourceApi,
  saveLogicalRelationsApi,
  testDatasourceConnectionApi,
  toggleAgentDatasourceApi,
  updateAgentDatasourceTablesApi,
  updateDatasourceApi,
} from '#/api';
import { initSchema } from '#/api/core/datasource';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

const props = defineProps<{
  agentId: number;
}>();

function unwrapData<T>(resp: unknown): T {
  if (resp && typeof resp === 'object' && 'data' in resp) {
    return (resp as ApiResponse<T>).data;
  }
  return resp as T;
}

async function checkSuccess(resp: unknown): Promise<boolean> {
  if (resp && typeof resp === 'object' && 'success' in resp) {
    return (resp as Record<string, unknown>).success as boolean;
  }
  return true;
}

async function ensureSuccess<T>(resp: T): Promise<T> {
  if (!(await checkSuccess(resp))) {
    throw new Error(
      ((resp as Record<string, unknown>).message as string) || '操作失败',
    );
  }
  return resp;
}

// ===== Datasource Type Interface =====
interface DatasourceType {
  typeName: string;
  displayName: string;
}

// ===== State =====
const datasource = ref<
  (Datasource & { status?: string; testStatus?: string })[]
>([]);
const initStatus = ref(false);
const dialogVisible = ref(false);
const dialogActiveName = ref('select');
const allDatasource = ref<Datasource[]>([]);
const newDatasource = ref<Datasource & { port?: number }>({ port: 3306 });
const selectedDatasourceId = ref<null | number>(null);
const editDialogVisible = ref(false);
const editingDatasource = ref<Datasource>({});
const schemaName = ref('');
const schemaNameEdit = ref('');

// Table management
const tableLists = ref<Record<number, string[]>>({});
const selectedTables = ref<Record<number, string[]>>({});
const tableLoadingStates = ref<Record<number, boolean>>({});
const updateLoadingStates = ref<Record<number, boolean>>({});

// Datasource types
const datasourceTypes = ref<DatasourceType[]>([]);

// Foreign key management
const foreignKeyDialogVisible = ref(false);
const currentForeignKeyDatasource = ref<Datasource | null>(null);
const foreignKeyList = ref<LogicalRelation[]>([]);
const editingForeignKey = ref<LogicalRelation | null>(null);
const newForeignKey = ref<LogicalRelation>({
  sourceTableName: '',
  sourceColumnName: '',
  targetTableName: '',
  targetColumnName: '',
  relationType: '',
  description: '',
});
const tableList = ref<string[]>([]);
const sourceColumnList = ref<string[]>([]);
const targetColumnList = ref<string[]>([]);
const savingForeignKeys = ref(false);

// ===== Load functions =====
async function loadAgentDatasource() {
  selectedDatasourceId.value = null;
  try {
    const response = unwrapData<
      { datasource: Datasource; isActive: number; selectTables: string[] }[]
    >(await getAgentDatasourcesApi(props.agentId));
    const list = response || [];
    datasource.value = list.map(
      (item: {
        datasource: Datasource;
        isActive: number;
        selectTables: string[];
      }) => {
        const ds = { ...item.datasource } as Datasource & {
          status?: string;
          testStatus?: string;
        };
        ds.status = item.isActive === 1 ? 'active' : 'inactive';
        if (item.datasource?.id) {
          selectedTables.value[item.datasource.id] = item.selectTables
            ? [...item.selectTables]
            : [];
        }
        return ds;
      },
    );
  } catch (error) {
    ElMessage.error('加载当前智能体的数据源列表失败');
    console.error('Failed to load datasource:', error);
  }
}

async function loadAllDatasource() {
  try {
    const response = unwrapData<Datasource[]>(await getAllDatasourcesApi());
    allDatasource.value = response || [];
  } catch (error) {
    ElMessage.error('加载所有数据源列表失败');
    console.error('Failed to load all datasource:', error);
  }
}

async function loadDatasourceTypes() {
  try {
    const response = unwrapData<DatasourceType[]>(
      await getDatasourceTypesApi(),
    );
    if (response) {
      datasourceTypes.value = response;
    }
  } catch (error) {
    ElMessage.error('加载数据源类型失败');
    console.error('Failed to load datasource types:', error);
  }
}

// ===== Init schema =====
async function initAgentDatasource() {
  initStatus.value = true;
  try {
    const response = await getAgentDatasourcesApi(props.agentId);
    const list =
      unwrapData<{ isActive: number; selectTables?: string[] }[]>(response) ||
      [];
    const hasActiveDs = list.some(
      (item) =>
        item.isActive === 1 &&
        item.selectTables &&
        item.selectTables.length > 0,
    );

    if (!hasActiveDs) {
      ElMessage.warning(
        '当前智能体没有启用的数据源或未选择数据表，请先配置数据源',
      );
      return;
    }

    await initSchema(props.agentId);
    ElMessage.success('初始化数据源成功');
  } catch (error) {
    ElMessage.error('初始化数据源失败');
    console.error('Failed to init datasource:', error);
  } finally {
    initStatus.value = false;
  }
}

// ===== Toggle datasource =====
async function changeDatasource(
  row: Datasource & { status?: string },
  active: boolean,
) {
  if (!row.id) return;
  try {
    await ensureSuccess(
      await toggleAgentDatasourceApi(props.agentId, row.id, active),
    );
    ElMessage.success('操作成功！');
    row.status = active ? 'active' : 'inactive';
  } catch (error) {
    ElMessage.error('操作失败！');
    console.error('Failed to change datasource:', error);
  }
}

// ===== Test connection =====
async function testConnection(row: Datasource & { testStatus?: string }) {
  if (!row.id) return;
  try {
    const resp = await testDatasourceConnectionApi(row.id);
    if (await checkSuccess(resp)) {
      ElMessage.success('测试连接成功！');
      row.testStatus = 'success';
    } else {
      ElMessage.error('测试连接失败！');
      row.testStatus = 'fail';
    }
  } catch (error) {
    ElMessage.error('测试连接失败！');
    console.error('Failed to test connection:', error);
    row.testStatus = 'fail';
  }
}

// ===== Remove datasource from agent =====
async function removeAgentDatasource(row: Datasource) {
  if (!row.id) return;
  try {
    await ElMessageBox.confirm('确定要移除该数据源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
  } catch {
    return;
  }

  try {
    await ensureSuccess(await removeAgentDatasourceApi(props.agentId, row.id));
    ElMessage.success('移除成功！');
    datasource.value = datasource.value.filter((item) => item.id !== row.id);
  } catch (error) {
    ElMessage.error('移除失败！');
    console.error('Failed to remove datasource:', error);
  }
}

// ===== Add datasource to agent =====
async function addDatasourceToAgent(datasourceId: number) {
  try {
    await ensureSuccess(
      await addAgentDatasourceApi(props.agentId, datasourceId),
    );
    await loadAgentDatasource();
    ElMessage.success('添加数据源成功');
    dialogVisible.value = false;
  } catch (error) {
    ElMessage.error('添加数据源失败');
    console.error('Failed to add datasource:', error);
  }
}

function handleSelectDatasourceChange(value: Datasource | null) {
  selectedDatasourceId.value = value?.id ?? null;
}

async function addSelectDatasource() {
  if (selectedDatasourceId.value === null) {
    ElMessage.warning('请选择一个数据源');
    return;
  }
  await addDatasourceToAgent(selectedDatasourceId.value);
}

// ===== Validate form =====
function validateDatasourceForm(
  form: Datasource & { port?: number },
  needsSchema: boolean = false,
  schemaValue: string = '',
): string[] {
  const errors: string[] = [];
  if (!form.name?.trim()) errors.push('数据源名称不能为空');
  if (!form.type) errors.push('请选择数据源类型');
  if (!form.host?.trim()) errors.push('主机地址不能为空');
  if (!form.port || form.port <= 0 || form.port > 65_535)
    errors.push('请输入有效的端口号（1-65535）');
  if (!form.databaseName?.trim()) errors.push('数据库名不能为空');
  if (needsSchema && !schemaValue?.trim()) errors.push('Schema 名不能为空');
  if (!form.username?.trim()) errors.push('用户名不能为空');
  if (!form.password?.trim()) errors.push('密码不能为空');
  return errors;
}

// ===== Create datasource =====
async function createNewDatasource() {
  const needsSchema =
    newDatasource.value.type === 'postgresql' ||
    newDatasource.value.type === 'oracle';
  const formErrors = validateDatasourceForm(
    newDatasource.value,
    needsSchema,
    schemaName.value,
  );
  if (formErrors.length > 0) {
    ElMessage.error(formErrors.join('\r\n'));
    return;
  }

  try {
    if (needsSchema && schemaName.value) {
      newDatasource.value.databaseName = `${newDatasource.value.databaseName}|${schemaName.value}`;
    }
    const datasourceResult = unwrapData<Datasource>(
      await createDatasourceApi(newDatasource.value as Partial<Datasource>),
    );
    if (datasourceResult?.id) {
      await addDatasourceToAgent(datasourceResult.id);
    } else {
      throw new Error('创建数据源失败');
    }
  } catch (error) {
    ElMessage.error('创建数据源失败');
    console.error('Failed to create datasource:', error);
  }
}

// ===== Edit datasource =====
function editDatasource(row: Datasource) {
  editingDatasource.value = JSON.parse(JSON.stringify(row));
  const needsSchema = row.type === 'postgresql' || row.type === 'oracle';
  if (needsSchema && editingDatasource.value.databaseName) {
    const parts = editingDatasource.value.databaseName.split('|');
    if (parts.length === 2) {
      editingDatasource.value.databaseName = parts[0] || '';
      schemaNameEdit.value = parts[1] || '';
    } else {
      schemaNameEdit.value = '';
    }
  } else {
    schemaNameEdit.value = '';
  }
  editDialogVisible.value = true;
}

async function saveEditDatasource() {
  if (!editingDatasource.value.id) return;
  const needsSchema =
    editingDatasource.value.type === 'postgresql' ||
    editingDatasource.value.type === 'oracle';
  const formErrors = validateDatasourceForm(
    editingDatasource.value,
    needsSchema,
    schemaNameEdit.value,
  );
  if (formErrors.length > 0) {
    ElMessage.error(formErrors.join('\n'));
    return;
  }

  try {
    if (needsSchema && schemaNameEdit.value) {
      editingDatasource.value.databaseName = `${editingDatasource.value.databaseName}|${schemaNameEdit.value}`;
    }
    const response = unwrapData<Datasource>(
      await updateDatasourceApi(
        editingDatasource.value.id,
        editingDatasource.value,
      ),
    );
    if (response?.id) {
      ElMessage.success('修改成功！');
      const index = allDatasource.value.findIndex(
        (item) => item.id === editingDatasource.value.id,
      );
      if (index !== -1) {
        allDatasource.value[index] = response;
      }
      editDialogVisible.value = false;
    } else {
      ElMessage.error('修改失败！');
    }
  } catch (error) {
    ElMessage.error('修改失败！');
    console.error('Failed to update datasource:', error);
  }
}

// ===== Delete datasource =====
async function deleteDatasource(row: Datasource) {
  if (!row.id) return;
  try {
    await ElMessageBox.confirm(
      '删除后无法恢复，确定要删除该数据源吗？',
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
  } catch {
    return;
  }

  try {
    await ensureSuccess(await deleteDatasourceApi(row.id));
    ElMessage.success('删除成功！');
    allDatasource.value = allDatasource.value.filter(
      (item) => item.id !== row.id,
    );
  } catch (error) {
    ElMessage.error('删除失败！');
    console.error('Failed to delete datasource:', error);
  }
}

// ===== Table management =====
async function loadDatasourceTables(ds: Datasource) {
  if (!ds.id) return;
  tableLoadingStates.value[ds.id] = true;
  try {
    const tables = unwrapData<string[]>(await getDatasourceTablesApi(ds.id));
    tableLists.value[ds.id] = tables || [];
    if (!selectedTables.value[ds.id]) {
      selectedTables.value[ds.id] = [];
    }
    ElMessage.success(`成功加载 ${tables?.length || 0} 个表`);
  } catch (error) {
    ElMessage.error('加载表列表失败');
    console.error('Failed to load datasource tables:', error);
  } finally {
    tableLoadingStates.value[ds.id] = false;
  }
}

async function updateDatasourceTables(ds: Datasource) {
  if (!ds.id) return;
  updateLoadingStates.value[ds.id] = true;
  try {
    await ensureSuccess(
      await updateAgentDatasourceTablesApi(
        props.agentId,
        ds.id,
        selectedTables.value[ds.id] || [],
      ),
    );
    ElMessage.success('数据表更新成功');
  } catch (error) {
    ElMessage.error('数据表更新失败');
    console.error('Failed to update datasource tables:', error);
  } finally {
    updateLoadingStates.value[ds.id] = false;
  }
}

function selectAllTables(ds: Datasource) {
  if (!ds.id || !tableLists.value[ds.id]) return;
  const tables = tableLists.value[ds.id];
  selectedTables.value[ds.id] = tables ? [...tables] : [];
}

function clearAllTables(ds: Datasource) {
  if (!ds.id) return;
  selectedTables.value[ds.id] = [];
}

function truncateText(text: string, maxLength: number): string {
  if (!text || text.length <= maxLength) return text;
  return `${text.substring(0, maxLength)}...`;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function handleExpandChange(row: any, expandedRows: any[]) {
  if (expandedRows.includes(row) && row.status === 'active' && row.id) {
    loadDatasourceTables(row);
  }
}

// ===== Foreign Key management =====
async function openForeignKeyDialog(datasourceRow: Datasource) {
  if (!datasourceRow.id) {
    ElMessage.warning('数据源ID不存在');
    return;
  }

  currentForeignKeyDatasource.value = datasourceRow;
  foreignKeyDialogVisible.value = true;

  try {
    const tables = unwrapData<string[]>(
      await getDatasourceTablesApi(datasourceRow.id),
    );
    tableList.value = tables || [];
  } catch (error) {
    ElMessage.error('加载表列表失败');
    console.error('Failed to load table list:', error);
  }

  await loadForeignKeys(datasourceRow.id);
  resetForeignKeyForm();
}

async function loadForeignKeys(datasourceId: number) {
  try {
    foreignKeyList.value =
      unwrapData<LogicalRelation[]>(
        await getLogicalRelationsApi(datasourceId),
      ) || [];
  } catch (error) {
    ElMessage.error('加载逻辑外键列表失败');
    console.error('Failed to load logical relations:', error);
  }
}

async function handleSourceTableChange(tableName: string) {
  if (!tableName || !currentForeignKeyDatasource.value?.id) {
    sourceColumnList.value = [];
    newForeignKey.value.sourceColumnName = '';
    return;
  }
  try {
    sourceColumnList.value =
      unwrapData<string[]>(
        await getDatasourceTableColumnsApi(
          currentForeignKeyDatasource.value.id,
          tableName,
        ),
      ) || [];
    newForeignKey.value.sourceColumnName = '';
  } catch (error) {
    ElMessage.error('加载字段列表失败');
    console.error('Failed to load source columns:', error);
  }
}

async function handleTargetTableChange(tableName: string) {
  if (!tableName || !currentForeignKeyDatasource.value?.id) {
    targetColumnList.value = [];
    newForeignKey.value.targetColumnName = '';
    return;
  }
  try {
    targetColumnList.value =
      unwrapData<string[]>(
        await getDatasourceTableColumnsApi(
          currentForeignKeyDatasource.value.id,
          tableName,
        ),
      ) || [];
    newForeignKey.value.targetColumnName = '';
  } catch (error) {
    ElMessage.error('加载字段列表失败');
    console.error('Failed to load target columns:', error);
  }
}

function editForeignKey(foreignKey: LogicalRelation) {
  editingForeignKey.value = foreignKey;
  newForeignKey.value = {
    id: foreignKey.id,
    sourceTableName: foreignKey.sourceTableName || '',
    sourceColumnName: foreignKey.sourceColumnName || '',
    targetTableName: foreignKey.targetTableName || '',
    targetColumnName: foreignKey.targetColumnName || '',
    relationType: foreignKey.relationType || '',
    description: foreignKey.description || '',
  };

  if (foreignKey.sourceTableName && currentForeignKeyDatasource.value?.id) {
    getDatasourceTableColumnsApi(
      currentForeignKeyDatasource.value.id,
      foreignKey.sourceTableName,
    )
      .then((resp) => {
        sourceColumnList.value = unwrapData<string[]>(resp) || [];
      })
      .catch(() => {});
  }

  if (foreignKey.targetTableName && currentForeignKeyDatasource.value?.id) {
    getDatasourceTableColumnsApi(
      currentForeignKeyDatasource.value.id,
      foreignKey.targetTableName,
    )
      .then((resp) => {
        targetColumnList.value = unwrapData<string[]>(resp) || [];
      })
      .catch(() => {});
  }

  ElMessage.info('正在编辑逻辑外键，修改后点击"更新"按钮');
}

function saveOrUpdateForeignKey() {
  if (
    !newForeignKey.value.sourceTableName ||
    !newForeignKey.value.sourceColumnName ||
    !newForeignKey.value.targetTableName ||
    !newForeignKey.value.targetColumnName
  ) {
    ElMessage.warning('请完整填写主表、字段、关联表和字段');
    return;
  }

  const isDuplicate = foreignKeyList.value.some(
    (fk) =>
      fk.id !== editingForeignKey.value?.id &&
      fk.sourceTableName === newForeignKey.value.sourceTableName &&
      fk.sourceColumnName === newForeignKey.value.sourceColumnName &&
      fk.targetTableName === newForeignKey.value.targetTableName &&
      fk.targetColumnName === newForeignKey.value.targetColumnName,
  );

  if (isDuplicate) {
    ElMessage.warning('该逻辑外键关系已存在');
    return;
  }

  if (editingForeignKey.value?.id) {
    const index = foreignKeyList.value.findIndex(
      (fk) => fk.id === editingForeignKey.value!.id,
    );
    if (index !== -1) {
      foreignKeyList.value[index] = {
        ...foreignKeyList.value[index],
        sourceTableName: newForeignKey.value.sourceTableName,
        sourceColumnName: newForeignKey.value.sourceColumnName,
        targetTableName: newForeignKey.value.targetTableName,
        targetColumnName: newForeignKey.value.targetColumnName,
        relationType: newForeignKey.value.relationType || '',
        description: newForeignKey.value.description || '',
      };
    }
    ElMessage.success('更新成功，请点击"保存全部配置"以保存到数据库');
  } else {
    foreignKeyList.value.push({
      sourceTableName: newForeignKey.value.sourceTableName,
      sourceColumnName: newForeignKey.value.sourceColumnName,
      targetTableName: newForeignKey.value.targetTableName,
      targetColumnName: newForeignKey.value.targetColumnName,
      relationType: newForeignKey.value.relationType || '',
      description: newForeignKey.value.description || '',
    });
    ElMessage.success('添加成功，请点击"保存全部配置"以保存到数据库');
  }

  resetForeignKeyForm();
}

async function deleteForeignKey(_foreignKey: LogicalRelation, index: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条逻辑外键关系吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    foreignKeyList.value.splice(index, 1);
    ElMessage.success('删除成功，请点击"保存全部配置"以保存到数据库');
  } catch {
    // cancelled
  }
}

async function saveForeignKeyConfig() {
  if (!currentForeignKeyDatasource.value?.id) {
    ElMessage.error('数据源ID不存在');
    return;
  }

  savingForeignKeys.value = true;
  try {
    await ensureSuccess(
      await saveLogicalRelationsApi(
        currentForeignKeyDatasource.value.id,
        foreignKeyList.value,
      ),
    );
    ElMessage.success('保存成功');
    foreignKeyDialogVisible.value = false;
  } catch (error) {
    ElMessage.error('保存失败');
    console.error('Failed to save logical relations:', error);
  } finally {
    savingForeignKeys.value = false;
  }
}

function resetForeignKeyForm() {
  editingForeignKey.value = null;
  newForeignKey.value = {
    sourceTableName: '',
    sourceColumnName: '',
    targetTableName: '',
    targetColumnName: '',
    relationType: '',
    description: '',
  };
  sourceColumnList.value = [];
  targetColumnList.value = [];
}

// ===== Watchers =====
watch(dialogVisible, (newValue) => {
  if (newValue) {
    loadAllDatasource();
    loadDatasourceTypes();
    newDatasource.value = { port: 3306 } as Datasource & { port?: number };
    schemaName.value = '';
    dialogActiveName.value = 'select';
  }
});

watch(editDialogVisible, (newValue) => {
  if (newValue) {
    loadDatasourceTypes();
  }
});

// ===== Lifecycle =====
onMounted(() => {
  loadAgentDatasource();
});
</script>

<template>
  <div class="datasource-config">
    <div class="mb-5 flex items-center justify-between">
      <div>
        <h3 class="text-base font-semibold">数据源配置</h3>
        <p class="mt-1 text-sm text-gray-500">管理和配置智能体的数据源连接</p>
      </div>
      <div class="flex gap-2">
        <ElButton
          @click="dialogVisible = true"
          size="small"
          type="primary"
          :icon="Plus"
        >
          添加数据源
        </ElButton>
        <ElButton
          @click="initAgentDatasource"
          :disabled="initStatus"
          size="small"
          type="primary"
          plain
        >
          {{ initStatus ? '初始化中...' : '初始化数据源' }}
        </ElButton>
      </div>
    </div>

    <ElTable
      :data="datasource"
      border
      stripe
      empty-text="暂无数据源"
      class="w-full"
      @expand-change="handleExpandChange as (...args: any[]) => void"
    >
      <ElTableColumn type="expand" width="60" label="数据表">
        <template #default="{ row }">
          <div v-if="row.status === 'active'" class="expand-content">
            <div class="expand-header">
              <h4 class="m-0 text-sm font-semibold">数据表管理</h4>
              <ElButton
                @click="loadDatasourceTables(row)"
                size="small"
                type="primary"
                :loading="tableLoadingStates[row.id!]"
                round
              >
                刷新表列表
              </ElButton>
            </div>

            <div
              v-if="(tableLists[row.id!] || []).length > 0"
              class="table-list"
            >
              <ElCheckboxGroup v-model="selectedTables[row.id!]">
                <ElRow :gutter="10">
                  <ElCol
                    v-for="table in tableLists[row.id!] || []"
                    :key="table"
                    :span="6"
                    class="mb-2"
                  >
                    <ElCheckbox :label="table" size="large">
                      {{ table }}
                    </ElCheckbox>
                  </ElCol>
                </ElRow>
              </ElCheckboxGroup>

              <div class="mt-4 text-right">
                <ElButton
                  @click="updateDatasourceTables(row)"
                  size="small"
                  type="success"
                  :loading="updateLoadingStates[row.id!]"
                  round
                >
                  更新数据表
                </ElButton>
                <ElButton
                  @click="selectAllTables(row)"
                  size="small"
                  type="primary"
                  round
                  plain
                >
                  全选
                </ElButton>
                <ElButton
                  @click="clearAllTables(row)"
                  size="small"
                  type="info"
                  round
                  plain
                >
                  清空
                </ElButton>
              </div>
            </div>

            <div v-else-if="tableLoadingStates[row.id!]" class="expand-empty">
              <ElIcon class="is-loading" :size="24"><Loading /></ElIcon>
              <div class="mt-2 text-gray-500">正在加载表列表...</div>
            </div>

            <div v-else class="expand-empty">
              <ElIcon :size="24"><FolderOpened /></ElIcon>
              <div class="mt-2 text-gray-400">暂无表数据，请点击刷新表列表</div>
            </div>
          </div>

          <div v-else class="expand-empty">
            <ElIcon :size="24"><Lock /></ElIcon>
            <div class="mt-2 text-gray-400">请先启用数据源以管理表</div>
          </div>
        </template>
      </ElTableColumn>

      <ElTableColumn prop="name" label="数据源名称" min-width="120" />
      <ElTableColumn prop="type" label="类型" min-width="100" />
      <ElTableColumn label="连接地址" min-width="200">
        <template #default="{ row }">
          <ElTooltip
            :content="row.jdbcUrl || row.connectionUrl || ''"
            placement="top"
            :disabled="!row.jdbcUrl && !row.connectionUrl"
          >
            <span class="connection-url">
              {{
                row.jdbcUrl || row.connectionUrl
                  ? truncateText(row.jdbcUrl || row.connectionUrl || '', 50)
                  : '-'
              }}
            </span>
          </ElTooltip>
        </template>
      </ElTableColumn>
      <ElTableColumn label="连接状态" width="100">
        <template #default="{ row }">
          <ElTag
            v-if="row.testStatus"
            :type="row.testStatus === 'success' ? 'success' : 'danger'"
            round
          >
            {{ row.testStatus === 'success' ? '连接成功' : '连接失败' }}
          </ElTag>
          <span v-else class="text-gray-400">未测试</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="状态" width="80">
        <template #default="{ row }">
          <ElTag :type="row.status === 'active' ? 'success' : 'info'" round>
            {{ row.status === 'active' ? '启用' : '禁用' }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" min-width="320" fixed="right">
        <template #default="{ row }">
          <ElButton
            v-if="row.status === 'active'"
            @click="changeDatasource(row, false)"
            size="small"
            type="warning"
            round
            plain
          >
            禁用
          </ElButton>
          <ElButton
            v-else
            @click="changeDatasource(row, true)"
            size="small"
            type="success"
            round
            plain
          >
            启用
          </ElButton>
          <ElButton
            @click="testConnection(row)"
            size="small"
            type="primary"
            round
            plain
          >
            测试连接
          </ElButton>
          <ElButton
            @click="openForeignKeyDialog(row)"
            size="small"
            type="success"
            round
            plain
          >
            <ElIcon style="margin-right: 4px"><Connection /></ElIcon>
            逻辑外键
          </ElButton>
          <ElButton
            @click="removeAgentDatasource(row)"
            size="small"
            type="danger"
            round
            plain
          >
            移除
          </ElButton>
        </template>
      </ElTableColumn>
    </ElTable>
  </div>

  <!-- 添加数据源 Dialog -->
  <ElDialog
    v-model="dialogVisible"
    title="添加数据源"
    width="1000"
    :close-on-click-modal="false"
  >
    <ElTabs v-model="dialogActiveName" type="card" stretch>
      <ElTabPane label="选择已有数据源" name="select">
        <div style="height: 450px; overflow-y: auto">
          <ElTable
            @current-change="handleSelectDatasourceChange"
            :data="allDatasource"
            highlight-current-row
            style="width: 100%"
          >
            <ElTableColumn property="name" label="数据源名称" width="150" />
            <ElTableColumn property="type" label="类型" width="100" />
            <ElTableColumn property="host" label="Host" width="100" />
            <ElTableColumn property="port" label="Port" width="80" />
            <ElTableColumn property="description" label="描述" width="300" />
            <ElTableColumn label="操作" width="150">
              <template #default="scope">
                <ElButton
                  @click="editDatasource(scope.row)"
                  size="small"
                  type="primary"
                  round
                  plain
                >
                  修改
                </ElButton>
                <ElButton
                  @click="deleteDatasource(scope.row)"
                  size="small"
                  type="danger"
                  round
                  plain
                >
                  删除
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
        <ElDivider />
        <div class="text-right">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="addSelectDatasource">
添加选中数据源
</ElButton>
        </div>
      </ElTabPane>
      <ElTabPane label="添加新数据源" name="add">
        <div style="height: 450px; overflow-y: auto">
          <ElRow :gutter="20">
            <ElCol :span="12">
              <div class="form-item">
                <label>数据源名称 *</label>
                <ElInput
                  v-model="newDatasource.name"
                  placeholder="请输入数据源名称"
                  size="large"
                />
              </div>
            </ElCol>
            <ElCol :span="12">
              <div class="form-item">
                <label>数据源类型 *</label>
                <ElSelect
                  v-model="newDatasource.type"
                  placeholder="请选择数据源类型"
                  style="width: 100%"
                  size="large"
                >
                  <ElOption
                    v-for="type in datasourceTypes"
                    :key="type.typeName"
                    :label="type.displayName"
                    :value="type.typeName"
                  />
                </ElSelect>
              </div>
            </ElCol>
          </ElRow>
          <ElRow :gutter="20">
            <ElCol :span="12">
              <div class="form-item">
                <label>主机地址 *</label>
                <ElInput
                  v-model="newDatasource.host"
                  placeholder="例如：localhost"
                  size="large"
                />
              </div>
            </ElCol>
            <ElCol :span="12">
              <div class="form-item">
                <label>端口号 *</label>
                <ElInputNumber
                  v-model="newDatasource.port"
                  :min="0"
                  :max="65535"
                  size="large"
                  style="width: 100%"
                />
              </div>
            </ElCol>
          </ElRow>
          <ElRow :gutter="20">
            <ElCol :span="12">
              <div class="form-item">
                <label>数据库名 *</label>
                <ElInput
                  v-model="newDatasource.databaseName"
                  :placeholder="
                    newDatasource.type === 'postgresql'
                      ? '例如：postgres'
                      : '请输入数据库名称'
                  "
                  size="large"
                />
              </div>
            </ElCol>
            <ElCol
              :span="12"
              v-if="
                newDatasource.type === 'postgresql' ||
                newDatasource.type === 'oracle'
              "
            >
              <div class="form-item">
                <label>Schema 名 *</label>
                <ElInput
                  v-model="schemaName"
                  :placeholder="
                    newDatasource.type === 'postgresql'
                      ? '例如：public'
                      : '例如：SYSTEM'
                  "
                  size="large"
                />
              </div>
            </ElCol>
          </ElRow>
          <ElRow :gutter="20">
            <ElCol :span="24">
              <div class="form-item">
                <label>连接地址</label>
                <ElInput
                  v-model="newDatasource.jdbcUrl"
                  placeholder="请输入JDBC地址（若不填则自动生成）"
                  size="large"
                />
              </div>
            </ElCol>
          </ElRow>
          <ElRow :gutter="20">
            <ElCol :span="12">
              <div class="form-item">
                <label>用户名 *</label>
                <ElInput
                  v-model="newDatasource.username"
                  placeholder="请输入数据库用户名"
                  size="large"
                />
              </div>
            </ElCol>
            <ElCol :span="12">
              <div class="form-item">
                <label>密码 *</label>
                <ElInput
                  v-model="newDatasource.password"
                  placeholder="请输入数据库密码"
                  size="large"
                  show-password
                />
              </div>
            </ElCol>
          </ElRow>
          <ElRow :gutter="20">
            <ElCol :span="24">
              <div class="form-item">
                <label>描述</label>
                <ElInput
                  v-model="newDatasource.description"
                  :rows="4"
                  type="textarea"
                  placeholder="请输入数据源描述（可选）"
                  size="large"
                />
              </div>
            </ElCol>
          </ElRow>
        </div>
        <ElDivider />
        <div class="text-right">
          <ElButton @click="dialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="createNewDatasource">
创建并添加
</ElButton>
        </div>
      </ElTabPane>
    </ElTabs>
  </ElDialog>

  <!-- 编辑数据源 Dialog -->
  <ElDialog
    v-model="editDialogVisible"
    title="编辑数据源"
    width="1000"
    :close-on-click-modal="false"
  >
    <ElRow :gutter="20">
      <ElCol :span="12">
        <div class="form-item">
          <label>数据源名称 *</label>
          <ElInput
            v-model="editingDatasource.name"
            placeholder="请输入数据源名称"
            size="large"
          />
        </div>
      </ElCol>
      <ElCol :span="12">
        <div class="form-item">
          <label>数据源类型 *</label>
          <ElSelect
            v-model="editingDatasource.type"
            placeholder="请选择数据源类型"
            style="width: 100%"
            size="large"
          >
            <ElOption
              v-for="type in datasourceTypes"
              :key="type.typeName"
              :label="type.displayName"
              :value="type.typeName"
            />
          </ElSelect>
        </div>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :span="12">
        <div class="form-item">
          <label>主机地址 *</label>
          <ElInput
            v-model="editingDatasource.host"
            placeholder="例如：localhost"
            size="large"
          />
        </div>
      </ElCol>
      <ElCol :span="12">
        <div class="form-item">
          <label>端口号 *</label>
          <ElInputNumber
            v-model="editingDatasource.port"
            :min="0"
            :max="65535"
            size="large"
            style="width: 100%"
          />
        </div>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :span="12">
        <div class="form-item">
          <label>数据库名 *</label>
          <ElInput
            v-model="editingDatasource.databaseName"
            :placeholder="
              editingDatasource.type === 'postgresql'
                ? '例如：postgres'
                : '请输入数据库名称'
            "
            size="large"
          />
        </div>
      </ElCol>
      <ElCol
        :span="12"
        v-if="
          editingDatasource.type === 'postgresql' ||
          editingDatasource.type === 'oracle'
        "
      >
        <div class="form-item">
          <label>Schema 名 *</label>
          <ElInput
            v-model="schemaNameEdit"
            :placeholder="
              editingDatasource.type === 'postgresql'
                ? '例如：public'
                : '例如：SYSTEM'
            "
            size="large"
          />
        </div>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :span="24">
        <div class="form-item">
          <label>连接地址</label>
          <ElInput
            v-model="editingDatasource.jdbcUrl"
            placeholder="请输入JDBC地址（若不填则自动生成）"
            size="large"
          />
        </div>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :span="12">
        <div class="form-item">
          <label>用户名 *</label>
          <ElInput
            v-model="editingDatasource.username"
            placeholder="请输入数据库用户名"
            size="large"
          />
        </div>
      </ElCol>
      <ElCol :span="12">
        <div class="form-item">
          <label>密码 *</label>
          <ElInput
            v-model="editingDatasource.password"
            placeholder="请输入数据库密码"
            size="large"
            show-password
          />
        </div>
      </ElCol>
    </ElRow>
    <ElRow :gutter="20">
      <ElCol :span="24">
        <div class="form-item">
          <label>描述</label>
          <ElInput
            v-model="editingDatasource.description"
            :rows="4"
            type="textarea"
            placeholder="请输入数据源描述（可选）"
            size="large"
          />
        </div>
      </ElCol>
    </ElRow>
    <ElDivider />
    <div class="text-right">
      <ElButton @click="editDialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="saveEditDatasource">保存修改</ElButton>
    </div>
  </ElDialog>

  <!-- 逻辑外键配置 Dialog -->
  <ElDialog
    v-model="foreignKeyDialogVisible"
    title="逻辑外键配置"
    width="900"
    :close-on-click-modal="false"
  >
    <div v-if="currentForeignKeyDatasource">
      <div class="fk-header-info">
        <p class="m-0 text-sm text-gray-500">
          当前配置数据源：
          <span class="font-semibold text-blue-500">
            {{ currentForeignKeyDatasource.name }}
          </span>
        </p>
      </div>

      <div class="mb-6">
        <h4 class="section-title mb-3">已生效的逻辑外键</h4>
        <ElTable :data="foreignKeyList" border style="width: 100%" size="small">
          <ElTableColumn
            prop="sourceTableName"
            label="主表 (Source)"
            min-width="100"
          >
            <template #default="scope">
              <span class="text-blue-500 font-mono">{{
                scope.row.sourceTableName
              }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="sourceColumnName" label="字段" min-width="80">
            <template #default="scope">
              <span class="font-mono">{{ scope.row.sourceColumnName }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn label="关系" width="70" align="center">
            <template #default="scope">
              <ElIcon color="#999"><Link /></ElIcon>
              <span class="font-mono ml-1">{{
                scope.row.relationType || '-'
              }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn
            prop="targetTableName"
            label="关联表 (Target)"
            min-width="100"
          >
            <template #default="scope">
              <span class="text-green-500 font-mono">{{
                scope.row.targetTableName
              }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="targetColumnName" label="字段" min-width="80">
            <template #default="scope">
              <span class="font-mono">{{ scope.row.targetColumnName }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="description" label="描述" min-width="120" />
          <ElTableColumn label="操作" width="130" align="right">
            <template #default="scope">
              <ElButton
                @click="editForeignKey(scope.row)"
                size="small"
                type="primary"
                link
                >
编辑
</ElButton>
              <ElButton
                @click="deleteForeignKey(scope.row, scope.$index)"
                size="small"
                type="danger"
                link
                >
删除
</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
        <div
          v-if="!foreignKeyList || foreignKeyList.length === 0"
          class="fk-empty"
        >
          <ElIcon :size="32"><FolderOpened /></ElIcon>
          <div>暂无逻辑外键配置</div>
        </div>
      </div>

      <div class="fk-form">
        <h4 class="mb-3 text-sm font-semibold">
          <ElIcon class="mr-1 align-middle text-blue-500">
            <CirclePlus v-if="!editingForeignKey" />
            <EditIcon v-else />
          </ElIcon>
          <span class="align-middle">{{
            editingForeignKey ? '编辑关联关系' : '新增关联关系'
          }}</span>
        </h4>

        <ElRow :gutter="10">
          <ElCol :span="5">
            <div class="mb-1">
              <label class="text-xs font-semibold text-gray-500">主表</label>
            </div>
            <ElSelect
              v-model="newForeignKey.sourceTableName"
              placeholder="选择表..."
              style="width: 100%"
              size="large"
              @change="handleSourceTableChange"
              clearable
              filterable
            >
              <ElOption
                v-for="table in tableList"
                :key="table"
                :label="table"
                :value="table"
              />
            </ElSelect>
          </ElCol>
          <ElCol :span="4">
            <div class="mb-1">
              <label class="text-xs font-semibold text-gray-500">字段</label>
            </div>
            <ElSelect
              v-model="newForeignKey.sourceColumnName"
              placeholder="先选表"
              style="width: 100%"
              size="large"
              :disabled="!newForeignKey.sourceTableName"
              clearable
              filterable
            >
              <ElOption
                v-for="col in sourceColumnList"
                :key="col"
                :label="col"
                :value="col"
              />
            </ElSelect>
          </ElCol>
          <ElCol
            :span="1"
            class="flex items-center justify-center"
            style="line-height: 70px"
          >
            <ElIcon color="#999" :size="20"><Right /></ElIcon>
          </ElCol>
          <ElCol :span="5">
            <div class="mb-1">
              <label class="text-xs font-semibold text-gray-500">关联表</label>
            </div>
            <ElSelect
              v-model="newForeignKey.targetTableName"
              placeholder="选择表..."
              style="width: 100%"
              size="large"
              @change="handleTargetTableChange"
              clearable
              filterable
            >
              <ElOption
                v-for="table in tableList"
                :key="table"
                :label="table"
                :value="table"
              />
            </ElSelect>
          </ElCol>
          <ElCol :span="4">
            <div class="mb-1">
              <label class="text-xs font-semibold text-gray-500">字段</label>
            </div>
            <ElSelect
              v-model="newForeignKey.targetColumnName"
              placeholder="先选表"
              style="width: 100%"
              size="large"
              :disabled="!newForeignKey.targetTableName"
              clearable
              filterable
            >
              <ElOption
                v-for="col in targetColumnList"
                :key="col"
                :label="col"
                :value="col"
              />
            </ElSelect>
          </ElCol>
          <ElCol :span="5" style="line-height: 70px">
            <ElButton
              @click="saveOrUpdateForeignKey"
              type="primary"
              size="large"
              style="width: 100%"
            >
              <ElIcon class="mr-1"><Check /></ElIcon>
              {{ editingForeignKey ? '更新' : '添加' }}
            </ElButton>
          </ElCol>
        </ElRow>

        <ElRow class="mt-2">
          <ElCol :span="24">
            <ElSelect
              v-model="newForeignKey.relationType"
              placeholder="选择关系类型（可选）"
              size="large"
              clearable
              style="width: 100%"
            >
              <ElOption label="1:1 (一对一)" value="1:1" />
              <ElOption label="1:N (一对多)" value="1:N" />
              <ElOption label="N:1 (多对一)" value="N:1" />
            </ElSelect>
          </ElCol>
        </ElRow>

        <ElRow class="mt-2">
          <ElCol :span="24">
            <ElInput
              v-model="newForeignKey.description"
              placeholder="描述（可选）：例如 '订单关联用户'，帮助 LLM 理解语义"
              size="large"
              clearable
            />
          </ElCol>
        </ElRow>
      </div>
    </div>

    <template #footer>
      <div class="text-right">
        <ElButton @click="foreignKeyDialogVisible = false" size="large">
取消
</ElButton>
        <ElButton
          type="primary"
          @click="saveForeignKeyConfig"
          size="large"
          :loading="savingForeignKeys"
        >
          保存全部配置
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<style scoped>
.datasource-config {
  padding: 4px 0;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.expand-content {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.expand-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

.expand-empty {
  padding: 20px;
  color: #999;
  text-align: center;
}

.table-list {
  margin-top: 10px;
}

.connection-url {
  font-family: monospace;
  font-size: 12px;
}

.fk-header-info {
  padding: 10px;
  margin-bottom: 20px;
  background: #f0f9ff;
  border-radius: 4px;
}

.section-title {
  padding-left: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  border-left: 4px solid #1890ff;
}

.fk-empty {
  padding: 40px;
  color: #999;
  text-align: center;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.fk-form {
  padding: 20px;
  background: #f0f9ff;
  border: 1px solid #bae7ff;
  border-radius: 8px;
}
</style>
