<script lang="ts" setup>
import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  createAccountInfoApi,
  updateAccountInfoApi,
} from '#/api';

import { useVbenForm } from '#/adapter/form';
import DepartmentSelector from '#/components/dept/DepartmentSelector.vue';
import EmployeeSelector from '#/components/dept/EmployeeSelector.vue';

import { useSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<any>();
const deptName = ref('');

const getTitle = computed(() => {
  return formData.value?.id ? '编辑账号' : '新增账号';
});

const [Form, formApi] = useVbenForm({
  layout: 'horizontal',
  schema: useSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

function handleEmployeeSelect(emp: any) {
  formApi.setValues({
    username: emp.empCode || '',
    realName: emp.empName || '',
    code: emp.empCode || '',
    phone: emp.mobile || '',
    deptId: emp.deptId ?? '',
  });
  deptName.value = emp.deptName || '';
}

function handleDeptChange(data: { deptId: string; deptName: string }) {
  deptName.value = data.deptName;
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const values = await formApi.getValues();
      values.deptName = deptName.value;
      delete values.confirmPassword;
      if (!values.password) {
        delete values.password;
      }
      try {
        await (formData.value?.id
          ? updateAccountInfoApi(values)
          : createAccountInfoApi(values));
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
        deptName.value = modalData.deptName || '';
        nextTick(() => {
          formApi.setValues(formData.value);
          if (formData.value?.id) {
            formApi.setValues({ password: '', confirmPassword: '' });
          }
        });
      }
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-200">
    <Form class="mx-4">
      <template #employeeSelector>
        <EmployeeSelector @change="handleEmployeeSelect" />
      </template>
      <template #deptId>
        <DepartmentSelector
          :model-value="formData?.deptId"
          @update:model-value="(val) => formApi.setValues({ deptId: val })"
          @change="handleDeptChange"
        />
      </template>
    </Form>
  </Modal>
</template>
