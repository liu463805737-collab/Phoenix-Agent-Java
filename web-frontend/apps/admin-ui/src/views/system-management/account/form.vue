<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  createUserApi,
  updateUserApi,
} from '#/api';

import { useVbenForm } from '#/adapter/form';
import DepartmentSelector from '#/components/dept/DepartmentSelector.vue';
import EmployeeSelector from '#/components/dept/EmployeeSelector.vue';

import { useSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<any>();
const isEdit = computed(() => !!formData.value?.id);

const getTitle = computed(() => {
  return isEdit.value ? '编辑账号' : '新增账号';
});

const [Form, formApi] = useVbenForm({
  layout: 'horizontal',
  schema: useSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

function handleEmployeeSelect(emp: any) {
  formApi.setValues({
    employeeId: emp.id,
    realName: emp.empName || '',
    mobile: emp.mobile || '',
    code: emp.empCode || '',
    companyId: emp.companyId != null ? emp.companyId : undefined,
    deptId: emp.deptId != null ? emp.deptId : undefined,
  });
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const values = await formApi.getValues();
      delete values.confirmPassword;
      if (!values.password) {
        delete values.password;
      }
      try {
        await (formData.value?.id
          ? updateUserApi(values)
          : createUserApi(values));
        modalApi.close();
        emit('success');
      } finally {
        modalApi.lock(false);
      }
    }
  },
  onOpenChange(isOpen) {
    debugger;
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
  <Modal :title="getTitle" class="w-200">
    <Form class="mx-4">
      <template #employeeSelector>
        <EmployeeSelector @change="handleEmployeeSelect" />
      </template>
      <template #deptId>
        <DepartmentSelector
          :model-value="formData?.deptId"
          @update:model-value="(val) => formApi.setValues({ deptId: val })"
          @change="(data) => { if (data.companyId) formApi.setValues({ companyId: data.companyId }) }"
        />
      </template>
    </Form>
  </Modal>
</template>
