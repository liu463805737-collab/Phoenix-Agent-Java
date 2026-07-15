<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';
import { useVbenForm, z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';
import { ElMessage } from 'element-plus';

import { adminSetPasswordApi } from '#/api';

defineOptions({ name: 'PasswordForm' });

const emit = defineEmits(['success']);

const schema: VbenFormSchema[] = [
  {
    fieldName: 'newPassword',
    label: '新密码',
    component: 'VbenInputPassword',
    componentProps: {
      passwordStrength: true,
      placeholder: '请输入新密码',
    },
  },
  {
    fieldName: 'confirmPassword',
    label: '确认密码',
    component: 'VbenInputPassword',
    componentProps: {
      passwordStrength: true,
      placeholder: '请再次输入新密码',
    },
    dependencies: {
      rules(values) {
        const { newPassword } = values;
        return z
          .string({ required_error: '请再次输入新密码' })
          .min(1, { message: '请再次输入新密码' })
          .refine((value) => value === newPassword, {
            message: '两次输入的密码不一致',
          });
      },
      triggerFields: ['newPassword'],
    },
  },
];

const [Form, formApi] = useVbenForm({
  schema,
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: {
    labelWidth: 80,
    componentProps: { class: 'w-full' },
  },
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const values = await formApi.getValues();
    try {
      const userId = modalApi.getData<any>()?.id;
      await adminSetPasswordApi(userId, values.newPassword);
      ElMessage.success('密码设置成功');
      modalApi.close();
      emit('success');
    } finally {
      modalApi.lock(false);
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      formApi.resetForm();
      formApi.resetValidate();
    }
  },
});
</script>

<template>
  <Modal title="设置密码">
    <div class="px-2 py-4">
      <Form />
    </div>
  </Modal>
</template>
