import { z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'keyword',
      component: 'Input',
      label: '搜索',
      labelWidth: 40,
      componentProps: {
        placeholder: '请输入用户名/姓名',
        allowClear: true,
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    { field: 'username', title: '用户名', minWidth: 120 },
    { field: 'realName', title: '真实姓名', width: 120 },
    { field: 'mobile', title: '手机号', width: 100 },
    { field: 'code', title: '工号', width: 100 },
    { field: 'companyName', title: '公司名称', width: 140 },
    { field: 'deptName', title: '部门名称', width: 120 },
    {
      field: 'userType',
      title: '用户类型',
      width: 110,
      slots: { default: 'userTypeSlot' },
    },
    {
      field: 'status',
      title: '状态',
      width: 70,
      slots: { default: 'statusSlot' },
    },
    {
      field: 'roles',
      title: '角色',
      width: 180,
      slots: { default: 'rolesSlot' },
    },
    { field: 'createTime', title: '创建时间', width: 180 },
    {
      align: 'center',
      field: 'operation',
      slots: { default: 'action' },
      fixed: 'right',
      title: '操作',
      width: 360,
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
      fieldName: 'employeeId',
      label: 'employeeId',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      component: 'Input',
      fieldName: 'companyId',
      label: 'companyId',
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
      fieldName: 'deptId',
      label: '选择部门',
      component: 'Input',
      formItemClass: 'col-span-2',
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
      rules: z.string().min(1, '请输入真实姓名'),
    },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: '手机号',
      rules: z.string().min(1, '请输入手机号'),
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: '工号',
      rules: z.string().min(1, '请输入工号'),
    },
    {
      component: 'Select',
      fieldName: 'userType',
      label: '用户类型',
      componentProps: {
        options: [
          { label: '自建用户', value: 0 },
          { label: 'idm用户', value: 1 },
        ],
      },
      rules: 'selectRequired',
    }
  ];
}
