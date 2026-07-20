<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { ElButton, ElDialog, ElIcon, ElInput, ElTree } from 'element-plus';
import { IconifyIcon } from '@vben/icons';
import type { OrganizationTreeVO } from '#/api';
import { getDepartmentTreeApi } from '#/api';

defineOptions({ name: 'DepartmentSelector' });

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
    clearable?: boolean;
    disabled?: boolean;
  }>(),
  { placeholder: '请选择部门', clearable: true, disabled: false },
);

const emit = defineEmits<{
  (e: 'update:modelValue', val: string | undefined): void;
  (e: 'change', data: { deptId: string; deptName: string; companyId?: string }): void;
}>();

const dialogVisible = ref(false);
const loading = ref(false);
const treeData = ref<OrganizationTreeVO[]>([]);
const searchKeyword = ref('');
const tempSelectedNode = ref<(OrganizationTreeVO & { companyId?: string }) | null>(null);
const selectedLabel = ref('');
const defaultExpandedKeys = ref<string[]>([]);

function findNodeById(tree: OrganizationTreeVO[], id: string): (OrganizationTreeVO & { companyId?: string }) | null {
  for (const node of tree) {
    if (node.id === id) return node as any;
    if (node.children) {
      const found = findNodeById(node.children, id);
      if (found) return found;
    }
  }
  return null;
}

function syncSelectedLabel() {
  if (props.modelValue && treeData.value.length > 0) {
    const node = findNodeById(treeData.value, props.modelValue);
    selectedLabel.value = node?.name || '';
  } else if (!props.modelValue) {
    selectedLabel.value = '';
  }
}

async function loadTree() {
  if (treeData.value.length > 0) return;
  loading.value = true;
  try {
    const res = (await getDepartmentTreeApi()) as any;
    const data = res?.data || res || [];
    treeData.value = data;
    const first = data[0];
    if (first?.id) {
      defaultExpandedKeys.value = [first.id];
    }
  } catch {
    treeData.value = [];
  } finally {
    loading.value = false;
  }
}

function openDialog() {
  if (props.disabled) return;
  tempSelectedNode.value = null;
  searchKeyword.value = '';
  dialogVisible.value = true;
  loadTree();
}

function handleNodeClick(data: any) {
  tempSelectedNode.value = data;
}

function confirm() {
  if (!tempSelectedNode.value) return;
  const node = tempSelectedNode.value;
  selectedLabel.value = node.name || '';
  emit('update:modelValue', node.id);
  emit('change', { deptId: node.id, deptName: node.name || '', companyId: node.companyId });
  dialogVisible.value = false;
}

function handleClear(e?: MouseEvent) {
  e?.stopPropagation();
  selectedLabel.value = '';
  emit('update:modelValue', undefined);
  emit('change', { deptId: '', deptName: '' });
}

const filteredTreeData = computed(() => {
  if (!searchKeyword.value) return treeData.value;
  function filter(nodes: OrganizationTreeVO[]): OrganizationTreeVO[] {
    const result: OrganizationTreeVO[] = [];
    for (const node of nodes) {
      const match = node.name?.toLowerCase().includes(searchKeyword.value.toLowerCase());
      if (match) {
        result.push(node);
      } else if (node.children?.length) {
        const filtered = filter(node.children);
        if (filtered.length > 0) {
          result.push({ ...node, children: filtered });
        }
      }
    }
    return result;
  }
  return filter(treeData.value);
});

watch(
  () => props.modelValue,
  (val) => {
    if (val && treeData.value.length === 0) {
      loadTree().then(() => syncSelectedLabel());
    } else {
      syncSelectedLabel();
    }
  },
);

onMounted(async () => {
  if (props.modelValue) {
    await loadTree();
    syncSelectedLabel();
  }
});
</script>

<template>
  <div class="dept-selector-wrap">
    <ElInput
      :model-value="selectedLabel || ''"
      :placeholder="placeholder"
      readonly
      clearable
      class="dept-trigger"
      @click="openDialog"
      @clear="handleClear"
    >
      <template #append>
        <ElButton @click.stop="openDialog">选择</ElButton>
      </template>
    </ElInput>

    <ElDialog
      v-model="dialogVisible"
      title="选择部门"
      width="480px"
      :close-on-click-modal="false"
      class="dept-dialog"
      @open="openDialog"
      append-to-body
    >
      <div class="dept-dialog-body">
        <div class="dept-search">
          <ElIcon class="dept-search-icon">
            <IconifyIcon icon="lucide:search" />
          </ElIcon>
          <input
            v-model="searchKeyword"
            class="dept-search-input"
            placeholder="搜索部门..."
          />
          <ElIcon
            v-if="searchKeyword"
            class="dept-search-clear"
            @click="searchKeyword = ''"
          >
            <IconifyIcon icon="lucide:x" />
          </ElIcon>
        </div>
        <div v-loading="loading" class="dept-tree-container">
          <div v-if="filteredTreeData.length === 0 && !loading" class="dept-empty">
            <ElIcon class="dept-empty-icon">
              <IconifyIcon icon="lucide:folder-tree" />
            </ElIcon>
            <span>暂无数据</span>
          </div>
          <ElTree
            v-else
            ref="treeRef"
            :data="filteredTreeData"
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            :default-expanded-keys="defaultExpandedKeys"
            :filter-node-method="undefined"
            highlight-current
            @node-click="handleNodeClick"
          >
            <template #default="{ node }">
              <div class="dept-node" :class="{ 'is-org': node.level === 1 }">
                <ElIcon class="dept-node-icon">
                  <IconifyIcon
                    :icon="node.level === 1 ? 'lucide:building-2' : 'lucide:folder-tree'"
                  />
                </ElIcon>
                <span class="dept-node-label">{{ node.label }}</span>
              </div>
            </template>
          </ElTree>
        </div>
      </div>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton
          type="primary"
          :disabled="!tempSelectedNode"
          @click="confirm"
        >
          确定
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<style scoped>
.dept-selector-wrap {
  width: 100%;
}

.dept-trigger {
  cursor: pointer;
}

.dept-trigger :deep(.el-input__wrapper) {
  cursor: pointer;
  border-radius: 8px 0 0 8px;
}

.dept-trigger :deep(.el-input__inner) {
  cursor: pointer;
}

.dept-trigger :deep(.el-input-group__prepend) {
  background: transparent;
  border: none;
  padding: 0 0 0 12px;
  color: hsl(var(--muted-foreground));
  font-size: 15px;
}

.dept-trigger :deep(.el-input-group__append) {
  background: hsl(var(--primary));
  border-color: hsl(var(--primary));
  border-radius: 0 8px 8px 0;
  padding: 0 16px;
}

.dept-trigger :deep(.el-input-group__append .el-button) {
  color: hsl(var(--primary-foreground));
  border: none;
  background: transparent;
  font-size: 13px;
  margin: 0;
  padding: 0;
}

/* Dialog styles */
.dept-dialog :deep(.el-dialog) {
  overflow: hidden;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgb(0 0 0 / 12%);
}

.dept-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin: 0;
}

.dept-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.dept-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

.dept-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  font-size: 16px;
  color: hsl(var(--muted-foreground));
}

.dept-dialog :deep(.el-dialog__body) {
  padding: 16px 24px;
}

.dept-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 20px;
  border-top: none;
}

.dept-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dept-search {
  position: relative;
  display: flex;
  align-items: center;
}

.dept-search-icon {
  position: absolute;
  left: 12px;
  font-size: 14px;
  color: hsl(var(--muted-foreground));
  pointer-events: none;
}

.dept-search-input {
  width: 100%;
  height: 36px;
  padding: 0 32px 0 36px;
  font-size: 13px;
  color: hsl(var(--foreground));
  outline: none;
  background: hsl(var(--background));
  border: 1px solid hsl(var(--input));
  border-radius: 8px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.dept-search-input::placeholder {
  color: hsl(var(--muted-foreground) / 50%);
}

.dept-search-input:focus {
  border-color: hsl(var(--primary));
  box-shadow: 0 0 0 2px hsl(var(--primary) / 12%);
}

.dept-search-clear {
  position: absolute;
  right: 10px;
  padding: 2px;
  font-size: 13px;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  border-radius: 4px;
}

.dept-search-clear:hover {
  color: hsl(var(--foreground));
}

.dept-tree-container {
  min-height: 100px;
  max-height: 340px;
  overflow-y: auto;
}

.dept-tree-container::-webkit-scrollbar {
  width: 4px;
}

.dept-tree-container::-webkit-scrollbar-thumb {
  background: hsl(var(--border));
  border-radius: 2px;
}

.dept-empty {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  font-size: 13px;
  color: hsl(var(--muted-foreground));
}

.dept-empty-icon {
  font-size: 32px;
  color: hsl(var(--border));
}

.dept-tree-container :deep(.el-tree-node__content) {
  height: 36px;
  padding: 0 8px;
  border-radius: 6px;
  transition: background 0.15s;
}

.dept-tree-container :deep(.el-tree-node__content:hover) {
  background: hsl(var(--accent));
}

.dept-tree-container :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: hsl(var(--primary) / 10%);
}

.dept-node {
  display: flex;
  gap: 6px;
  align-items: center;
  width: 100%;
}

.dept-node-icon {
  flex-shrink: 0;
  font-size: 15px;
  color: hsl(var(--primary) / 65%);
}

.dept-node.is-org .dept-node-icon {
  font-size: 16px;
  color: hsl(var(--primary));
}

.dept-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  color: hsl(var(--foreground));
  white-space: nowrap;
}

.dept-tree-container :deep(.el-tree-node.is-current > .el-tree-node__content .dept-node-label) {
  font-weight: 500;
  color: hsl(var(--primary));
}
</style>
