<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  createGroupInfoApi,
  updateGroupInfoApi,
} from '#/api';

import { useVbenForm } from '#/adapter/form';

import { useSchema } from './data';

defineOptions({ name: 'GroupInfoForm' });

const emit = defineEmits(['success']);
const formData = ref<any>();

const getTitle = computed(() => {
  return formData.value?.id ? '编辑组' : '新增组';
});

const [Form, formApi] = useVbenForm({
  layout: 'horizontal',
  schema: useSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-1',
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const values = await formApi.getValues();
      try {
        await (formData.value?.id
          ? updateGroupInfoApi(values)
          : createGroupInfoApi(values));
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
    <Form class="mx-4" />
  </Modal>
</template>
