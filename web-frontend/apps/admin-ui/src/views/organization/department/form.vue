<script lang="ts" setup>
import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import {
  createDepartmentApi,
  updateDepartmentApi,
  type PrivilegeDepartment,
} from '#/api';

import { useVbenForm } from '#/adapter/form';
import { ElButton, ElDialog, ElIcon, ElInput, ElTree } from 'element-plus';

import { useSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<any>();
const deptTree = ref<PrivilegeDepartment[]>([]);
const selectedParentName = ref('');
const pidDialogVisible = ref(false);
const pidTreeRef = ref();

const getTitle = computed(() => {
  return formData.value?.id
    ? '编辑部门'
    : '新增部门';
});

const [Form, formApi] = useVbenForm({
  layout: 'horizontal',
  schema: useSchema(),
  showDefaultActions: false,
});

function handleOpenPidTree() {
  pidDialogVisible.value = true;
  nextTick(() => {
    if (pidTreeRef.value && formData.value?.pid) {
      pidTreeRef.value.setCurrentKey(formData.value.pid);
    }
  });
}

function handlePidNodeClick(data: PrivilegeDepartment) {
  if (data.id === formData.value?.id) return;
  formApi.setValues({ pid: data.id });
  selectedParentName.value = data.name ?? '';
  pidDialogVisible.value = false;
}

function handleClearPid() {
  formApi.setValues({ pid: undefined });
  selectedParentName.value = '';
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const data = await formApi.getValues();
      try {
        await (formData.value?.id
          ? updateDepartmentApi(data)
          : createDepartmentApi(data));
        modalApi.close();
        emit('success');
      } finally {
        modalApi.lock(false);
      }
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const modalData = modalApi.getData<any>();
      if (modalData) {
        formData.value = modalData.row || modalData;
        deptTree.value = modalData.tree || [];
        selectedParentName.value = modalData.parentName || '';
        formApi.setValues(formData.value);
      }
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4">
      <template #pid>
        <ElInput
          :model-value="selectedParentName || ''"
          placeholder="点击选择上级部门"
          readonly
          clearable
          class="pid-trigger"
          @click="handleOpenPidTree"
          @clear="handleClearPid"
        >
          <template #prepend>
            <ElIcon><IconifyIcon icon="lucide:folder-tree" /></ElIcon>
          </template>
          <template #append>
            <ElButton @click.stop="handleOpenPidTree">选择</ElButton>
          </template>
        </ElInput>
      </template>
    </Form>

    <ElDialog
      v-model="pidDialogVisible"
      title="选择上级部门"
      width="480px"
      :close-on-click-modal="false"
      class="pid-dialog"
      append-to-body
    >
      <div class="pid-tree-container">
        <ElTree
          ref="pidTreeRef"
          :data="deptTree"
          :props="{ children: 'children', label: 'name' }"
          node-key="id"
          default-expand-all
          highlight-current
          @node-click="handlePidNodeClick"
        >
          <template #default="{ node }">
            <div class="pid-node">
              <ElIcon class="pid-node-icon">
                <IconifyIcon icon="lucide:folder-tree" />
              </ElIcon>
              <span class="pid-node-label">{{ node.label }}</span>
            </div>
          </template>
        </ElTree>
      </div>
    </ElDialog>
  </Modal>
</template>

<style scoped>
.pid-trigger {
  cursor: pointer;
}

.pid-trigger :deep(.el-input__wrapper) {
  cursor: pointer;
  border-radius: 8px 0 0 8px;
}

.pid-trigger :deep(.el-input__inner) {
  cursor: pointer;
}

.pid-trigger :deep(.el-input-group__prepend) {
  background: transparent;
  border: none;
  padding: 0 0 0 12px;
  color: hsl(var(--muted-foreground));
  font-size: 15px;
}

.pid-trigger :deep(.el-input-group__append) {
  background: hsl(var(--primary));
  border-color: hsl(var(--primary));
  border-radius: 0 8px 8px 0;
  padding: 0 16px;
}

.pid-trigger :deep(.el-input-group__append .el-button) {
  color: hsl(var(--primary-foreground));
  border: none;
  background: transparent;
  font-size: 13px;
  margin: 0;
  padding: 0;
}

.pid-dialog :deep(.el-dialog) {
  overflow: hidden;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgb(0 0 0 / 12%);
}

.pid-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin: 0;
}

.pid-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.pid-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
}

.pid-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  font-size: 16px;
  color: hsl(var(--muted-foreground));
}

.pid-dialog :deep(.el-dialog__body) {
  padding: 16px 24px;
}

.pid-tree-container {
  max-height: 340px;
  overflow-y: auto;
}

.pid-tree-container::-webkit-scrollbar {
  width: 4px;
}

.pid-tree-container::-webkit-scrollbar-thumb {
  background: hsl(var(--border));
  border-radius: 2px;
}

.pid-tree-container :deep(.el-tree-node__content) {
  height: 36px;
  padding: 0 8px;
  border-radius: 6px;
  transition: background 0.15s;
}

.pid-tree-container :deep(.el-tree-node__content:hover) {
  background: hsl(var(--accent));
}

.pid-tree-container :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: hsl(var(--primary) / 10%);
}

.pid-node {
  display: flex;
  gap: 6px;
  align-items: center;
  width: 100%;
}

.pid-node-icon {
  flex-shrink: 0;
  font-size: 15px;
  color: hsl(var(--primary) / 65%);
}

.pid-node-label {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  color: hsl(var(--foreground));
  white-space: nowrap;
}

.pid-tree-container :deep(.el-tree-node.is-current > .el-tree-node__content .pid-node-label) {
  font-weight: 500;
  color: hsl(var(--primary));
}
</style>
