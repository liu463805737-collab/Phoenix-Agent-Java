<script lang="ts" setup>
import type { PrivilegeEmployee } from '#/api';

import { onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElButton, ElDialog, ElIcon, ElInput, ElPagination, ElTable, ElTableColumn } from 'element-plus';

import { getEmployeePageApi } from '#/api';

defineOptions({ name: 'EmployeeSelector' });

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
    clearable?: boolean;
    disabled?: boolean;
  }>(),
  { placeholder: '请选择人员', clearable: true, disabled: false },
);

const emit = defineEmits<{
  (e: 'update:modelValue', val: string | undefined): void;
  (e: 'change', data: PrivilegeEmployee): void;
}>();

const dialogVisible = ref(false);
const loading = ref(false);
const selectedLabel = ref(props.modelValue || '');
const tempSelected = ref<PrivilegeEmployee | null>(null);

const tableData = ref<PrivilegeEmployee[]>([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const keyword = ref('');

const isActive = ref(!!props.modelValue);

async function loadData() {
  loading.value = true;
  try {
    const params: Record<string, any> = {};
    if (keyword.value) params.keyword = keyword.value;
    const res = (await getEmployeePageApi(page.value, pageSize.value, params)) as any;
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

function openDialog() {
  if (props.disabled) return;
  tempSelected.value = null;
  keyword.value = '';
  page.value = 1;
  pageSize.value = 10;
  dialogVisible.value = true;
  loadData();
}

function handleSearch() {
  page.value = 1;
  loadData();
}

function handleReset() {
  keyword.value = '';
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

function handleRowClick(row: PrivilegeEmployee) {
  tempSelected.value = row;
}

function handleRowDblclick(row: PrivilegeEmployee) {
  tempSelected.value = row;
  confirm();
}

function confirm() {
  if (!tempSelected.value) return;
  const emp = tempSelected.value;
  selectedLabel.value = emp.empName || '';
  isActive.value = true;
  emit('update:modelValue', emp.empName || '');
  emit('change', { ...emp });
  dialogVisible.value = false;
}

function handleClear(e?: MouseEvent) {
  e?.stopPropagation();
  selectedLabel.value = '';
  isActive.value = false;
  emit('update:modelValue', undefined);
}

onMounted(() => {
  if (props.modelValue) {
    selectedLabel.value = props.modelValue;
    isActive.value = true;
  }
});
</script>

<template>
  <div class="emp-selector-wrap">
    <ElInput
      :model-value="selectedLabel || ''"
      :placeholder="placeholder"
      readonly
      clearable
      class="emp-trigger"
      @click="openDialog"
      @clear="handleClear"
    >
      <template #append>
        <ElButton @click.stop="openDialog">选择</ElButton>
      </template>
    </ElInput>

    <ElDialog
      v-model="dialogVisible"
      title="选择人员"
      width="640px"
      :close-on-click-modal="false"
      class="emp-dialog"
      top="8vh"
      append-to-body
    >
      <div class="emp-dialog-body">
        <div class="emp-search-bar">
          <ElInput
            v-model="keyword"
            placeholder="搜索姓名/工号/手机号"
            clearable
            class="emp-search-input--wide"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <ElIcon><IconifyIcon icon="lucide:search" /></ElIcon>
            </template>
          </ElInput>
          <div class="emp-search-actions">
            <ElButton type="primary" @click="handleSearch">查询</ElButton>
            <ElButton @click="handleReset">重置</ElButton>
          </div>
        </div>
        <div v-loading="loading" class="emp-table-container">
          <ElTable
            :data="tableData"
            style="width: 100%"
            stripe
            highlight-current-row
            @current-change="handleRowClick"
            @row-dblclick="handleRowDblclick"
          >
            <ElTableColumn type="index" label="序号" width="60" />
            <ElTableColumn prop="empName" label="姓名" width="120" />
            <ElTableColumn prop="empCode" label="工号" width="120" />
            <ElTableColumn prop="mobile" label="手机号" width="140" />
            <ElTableColumn prop="deptName" label="部门" min-width="150" />
          </ElTable>
        </div>
        <div class="emp-pagination">
          <ElPagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            background
            small
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </div>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton
          type="primary"
          :disabled="!tempSelected"
          @click="confirm"
        >
          确定
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.emp-selector-wrap {
  width: 100%;
}

.emp-trigger {
  cursor: pointer;
}

.emp-trigger :deep(.el-input__wrapper) {
  cursor: pointer;
  border-radius: 8px 0 0 8px;
}

.emp-trigger :deep(.el-input__inner) {
  cursor: pointer;
}

.emp-trigger :deep(.el-input-group__prepend) {
  background: transparent;
  border: none;
  padding: 0 0 0 12px;
  color: hsl(var(--muted-foreground));
  font-size: 15px;
}

.emp-trigger :deep(.el-input-group__append) {
  background: hsl(var(--primary));
  border-color: hsl(var(--primary));
  border-radius: 0 8px 8px 0;
  padding: 0 16px;
}

.emp-trigger :deep(.el-input-group__append .el-button) {
  color: hsl(var(--primary-foreground));
  border: none;
  background: transparent;
  font-size: 13px;
  margin: 0;
  padding: 0;
}

/* Dialog */
.emp-dialog :deep(.el-dialog) {
  overflow: hidden;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgb(0 0 0 / 12%);
}

.emp-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin: 0;
}

.emp-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.emp-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

.emp-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  font-size: 16px;
  color: hsl(var(--muted-foreground));
}

.emp-dialog :deep(.el-dialog__body) {
  padding: 16px 24px;
}

.emp-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 20px;
  border-top: none;
}

.emp-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.emp-search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.emp-search-input--wide {
  flex: 1;
  min-width: 200px;
}

.emp-search-input--wide :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.emp-search-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.emp-table-container {
  min-height: 200px;
}

.emp-table-container :deep(.el-table) {
  border-radius: 8px;
}

.emp-table-container :deep(.el-table th.el-table__cell) {
  font-size: 13px;
  font-weight: 500;
  color: hsl(var(--muted-foreground));
  background: hsl(var(--muted));
}

.emp-table-container :deep(.el-table .el-table__row--striped) {
  background: hsl(var(--accent) / 30%);
}

.emp-table-container :deep(.el-table .el-table__row--current) {
  background: hsl(var(--primary) / 8%);
}

.emp-table-container :deep(.el-table--striped .el-table__body tr.el-table__row--striped.current-row td) {
  background: hsl(var(--primary) / 8%);
}

.emp-pagination {
  display: flex;
  justify-content: flex-end;
}
</style>
