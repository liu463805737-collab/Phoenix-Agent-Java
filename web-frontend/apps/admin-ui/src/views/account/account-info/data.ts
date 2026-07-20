import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

import { z } from '#/adapter/form';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'keyword',
      component: 'Input',
      label: '搜索',
      labelWidth: 40,
      componentProps: {
        placeholder: '用户名/真实姓名/手机号',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      component: 'Select',
      label: '状态',
      componentProps: {
        placeholder: '请选择状态',
        options: [
          { label: '启用', value: '1' },
          { label: '禁用', value: '0' },
        ],
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    { field: 'username', title: '用户名', minWidth: 120 },
    { field: 'realName', title: '真实姓名', width: 100 },
    { field: 'nickName', title: '昵称', width: 100 },
    { field: 'code', title: '编码', width: 100 },
    { field: 'phone', title: '手机号', width: 130, align: 'center' },
    { field: 'email', title: '邮箱', minWidth: 160 },
    { field: 'gender', title: '性别', width: 70, align: 'center', slots: { default: 'genderSlot' } },
    {
      field: 'status',
      title: '状态',
      width: 70,
      align: 'center',
      slots: { default: 'statusSlot' },
    },
    {
      field: 'groups',
      title: '所属组',
      minWidth: 160,
      slots: { default: 'groupsSlot' },
    },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    {
      align: 'center',
      field: 'operation',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 240,
    },
  ];
}

export function useSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      label: 'id',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      component: 'Input',
      fieldName: 'deptName',
      label: 'deptName',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      component: 'Input',
      fieldName: 'deptId',
      label: 'deptId',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      component: 'Input',
      fieldName: 'employeeId',
      label: 'employeeId',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      component: 'Input',
      fieldName: 'thirdPartyId',
      label: 'thirdPartyId',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      fieldName: 'employeeSelector',
      label: '选择人员',
      component: 'Input',
      formItemClass: 'col-span-2',
      dependencies: {
        triggerFields: ['id'],
        show: (values: any) => !values.id,
      },
    },
    {
      component: 'Input',
      fieldName: 'username',
      label: '用户名',
      rules: z.string().min(1, '请输入用户名'),
    },
    {
      component: 'Input',
      fieldName: 'realName',
      label: '真实姓名',
    },
    {
      component: 'Input',
      fieldName: 'nickName',
      label: '昵称',
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: '账号编码',
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: '手机号',
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: '邮箱',
    },
    {
      component: 'Select',
      fieldName: 'gender',
      label: '性别',
      defaultValue: '-1',
      componentProps: {
        options: [
          { label: '男', value: '1' },
          { label: '女', value: '0' },
          { label: '未知', value: '-1' },
        ],
      },
    },
    {
      component: 'Input',
      fieldName: 'birthday',
      label: '生日',
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: '状态',
      defaultValue: '1',
      componentProps: {
        options: [
          { label: '启用', value: '1' },
          { label: '禁用', value: '0' },
        ],
      },
    },
    {
      component: 'Input',
      fieldName: 'password',
      label: '密码',
      componentProps: {
        type: 'password',
        showPassword: true,
        placeholder: '留空则不修改',
      },
    },
    {
      component: 'Input',
      fieldName: 'confirmPassword',
      label: '确认密码',
      componentProps: {
        type: 'password',
        showPassword: true,
        placeholder: '请再次输入密码',
      },
      dependencies: {
        triggerFields: ['password'],
        rules(values) {
          const password = values.password;
          if (!password) return z.string().optional();
          return z
            .string({ required_error: '请再次输入密码' })
            .min(1, { message: '请再次输入密码' })
            .refine((value) => value === password, {
              message: '两次输入的密码不一致',
            });
        },
      },
    },
  ];
}
