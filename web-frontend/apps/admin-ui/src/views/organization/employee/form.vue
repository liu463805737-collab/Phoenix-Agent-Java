<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  createEmployeeApi,
  updateEmployeeApi,
} from '#/api';

import { useVbenForm } from '#/adapter/form';
import DepartmentSelector from '#/components/dept/DepartmentSelector.vue';
import EmployeeSelector from '#/components/dept/EmployeeSelector.vue';

import { useSchema } from './data';

defineOptions({ name: 'EmployeeForm' });

const emit = defineEmits(['success']);
const formData = ref<any>();

const isEdit = computed(() => !!formData.value?.id);

const getTitle = computed(() => {
  return isEdit.value ? '编辑人员' : '新增人员';
});

function handleEmployeeSelect(emp: any) {
  formApi.setValues({
    empName: emp.empName || '',
    empCode: emp.empCode || '',
    mobile: emp.mobile || '',
    deptId: emp.deptId ?? '',
    deptName: emp.deptName || '',
    companyId: emp.companyId ?? '',
  });
}

const [Form, formApi] = useVbenForm({
  layout: 'horizontal',
  schema: useSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const values = await formApi.getValues();
      try {
        await (formData.value?.id
          ? updateEmployeeApi(values)
          : createEmployeeApi(values));
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
  <Modal :title="getTitle" class="w-200">
    <Form class="mx-4">
      <template #employeeSelector>
        <EmployeeSelector @change="handleEmployeeSelect" />
      </template>
      <template #deptId>
        <DepartmentSelector
          :model-value="formData?.deptId"
          @update:model-value="(val) => formApi.setValues({ deptId: val })"
          @change="(data) => formApi.setValues({ deptName: data.deptName, companyId: data.companyId })"
        />
      </template>
    </Form>
  </Modal>
</template>
