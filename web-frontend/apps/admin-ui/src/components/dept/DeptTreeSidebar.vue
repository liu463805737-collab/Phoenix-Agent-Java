<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { ElCard, ElIcon, ElTree } from 'element-plus';

import { IconifyIcon } from '@vben/icons';

import type { DepartmentTreeVO, OrganizationTreeVO } from '#/api';
import { getDepartmentTreeApi } from '#/api';

defineOptions({ name: 'DeptTreeSidebar' });

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    title?: string;
  }>(),
  { title: '部门列表' },
);

const emit = defineEmits<{
  (e: 'update:modelValue', val: string | undefined): void;
  (e: 'select', data: OrganizationTreeVO | DepartmentTreeVO): void;
}>();

const loading = ref(false);
const treeData = ref<OrganizationTreeVO[]>([]);
const defaultExpandedKeys = ref<string[]>([]);

async function load() {
  if (treeData.value.length > 0) return;
  loading.value = true;
  try {
    const res = (await getDepartmentTreeApi()) as any;
    const data = res?.data || res || [];
    computeDefaultExpandedKeys(data);
    treeData.value = data;
  } catch {
    treeData.value = [];
  } finally {
    loading.value = false;
  }
}

function computeDefaultExpandedKeys(tree: OrganizationTreeVO[]) {
  const keys: string[] = [];
  const firstOrg = tree[0];
  if (firstOrg?.id) {
    keys.push(firstOrg.id);
  }
  defaultExpandedKeys.value = keys;
}

function handleNodeClick(data: OrganizationTreeVO | DepartmentTreeVO) {
  if (props.modelValue === data?.id) {
    emit('update:modelValue', undefined);
    emit('select', data);
  } else {
    emit('update:modelValue', data?.id);
    emit('select', data);
  }
}

function refresh() {
  treeData.value = [];
  load();
}

defineExpose({ refresh });

onMounted(load);
</script>

<template>
  <ElCard class="tree-section" :body-style="{ padding: '12px' }">
    <div class="tree-header">{{ title }}</div>
    <div v-loading="loading" class="tree-body">
      <ElTree
        :data="treeData"
        :props="{ children: 'children', label: 'name' }"
        node-key="id"
        :default-expanded-keys="defaultExpandedKeys"
        highlight-current
        :current-node-key="props.modelValue"
        @node-click="handleNodeClick"
      >
        <template #default="{ node }">
          <div class="tree-node-content" :class="{ 'is-org': node.level === 1 }">
            <ElIcon class="tree-node-icon">
              <IconifyIcon
                :icon="node.level === 1 ? 'lucide:building-2' : 'lucide:folder-tree'"
              />
            </ElIcon>
            <span class="tree-node-label">{{ node.label }}</span>
          </div>
        </template>
      </ElTree>
    </div>
  </ElCard>
</template>

<style scoped>
.tree-section {
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  width: 260px;
  border-radius: 12px;
}

.tree-section :deep(.el-card__body) {
  display: flex;
  flex: 1;
  flex-direction: column;
  overflow: hidden;
}

.tree-header {
  flex-shrink: 0;
  padding-bottom: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: hsl(var(--foreground));
  border-bottom: 1px solid hsl(var(--border));
}

.tree-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.tree-body :deep(.el-tree-node__content) {
  height: 36px;
  padding: 0 4px;
  border-radius: 6px;
  transition: all 0.2s;
}

.tree-body :deep(.el-tree-node__content:hover) {
  background-color: hsl(var(--accent));
}

.tree-body :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: hsl(var(--primary) / 12%);
}

.tree-body :deep(.el-tree-node.is-current > .el-tree-node__content .tree-node-label) {
  font-weight: 600;
  color: hsl(var(--primary));
}

.tree-node-content {
  display: flex;
  gap: 6px;
  align-items: center;
  width: 100%;
}

.tree-node-icon {
  flex-shrink: 0;
  font-size: 16px;
  color: hsl(var(--success));
}

.is-org .tree-node-icon {
  font-size: 18px;
  color: hsl(var(--primary));
}

.tree-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  color: hsl(var(--foreground));
  white-space: nowrap;
}
</style>
