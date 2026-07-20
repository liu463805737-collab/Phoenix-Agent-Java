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
  return formData.value?.id ? '编辑部门' : '新增部门';
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
              <ElIcon>
                <IconifyIcon icon="lucide:folder-tree" />
              </ElIcon>
              <span>{{ node.label }}</span>
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
}

.pid-trigger :deep(.el-input__inner) {
  cursor: pointer;
}

.pid-tree-container {
  max-height: 340px;
  overflow-y: auto;
}

.pid-node {
  display: flex;
  gap: 6px;
  align-items: center;
}
</style>
