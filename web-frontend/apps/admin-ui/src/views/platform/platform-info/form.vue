<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  createPlatformInfoApi,
  updatePlatformInfoApi,
} from '#/api/core/platform-info';

import { useVbenForm } from '#/adapter/form';

import { useSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<any>();

const getTitle = computed(() => {
  return formData.value?.id ? '编辑三方平台' : '新增三方平台';
});

const [Form, formApi] = useVbenForm({
  layout: 'horizontal',
  schema: useSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const values = await formApi.getValues();
      try {
        await (formData.value?.id
          ? updatePlatformInfoApi(values)
          : createPlatformInfoApi(values));
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
        formData.value = modalData;
        formApi.setValues(formData.value);
      }
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form />
  </Modal>
</template>
