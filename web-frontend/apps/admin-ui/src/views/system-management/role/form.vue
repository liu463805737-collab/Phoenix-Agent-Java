<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import {
  createRoleApi,
  deleteRoleApi,
  getAclsByReleaseIdApi,
  getRoleAclsApi,
  getRolePageApi,
  saveAllAclApi,
  saveModuleAclApi,
  updateRoleApi,
} from '#/api';

import { useVbenForm } from '#/adapter/form';

import { useSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<any>();
const getTitle = computed(() => {
  return formData.value?.id
      ? '编辑角色'
      : '创建角色';
});

const [Form, formApi] = useVbenForm({
  layout: 'vertical',
  schema: useSchema(),
  showDefaultActions: false,
});

function resetForm() {
  formApi.resetForm();
  formApi.setValues(formData.value || {});
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const data = await formApi.getValues();
      try {
        await (formData.value?.id
            ? updateRoleApi(data)
            : createRoleApi(data));
        modalApi.close();
        emit('success');
      } finally {
        modalApi.lock(false);
      }
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = modalApi.getData<any>();
      if (data) {
        if (data.pid === 0) {
          data.pid = undefined;
        }
        formData.value = data;
        formApi.setValues(formData.value);
      }
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
