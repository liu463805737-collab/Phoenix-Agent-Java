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
        placeholder: '姓名/工号/手机号',
        allowClear: true,
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    { field: 'empName', title: '姓名', minWidth: 80 },
    { field: 'empCode', title: '工号', width: 100 },
    { field: 'mobile', title: '手机号', width: 120, align: 'center' },
    { field: 'companyName', title: '公司名称', width: 160 },
    { field: 'deptName', title: '部门名称', width: 160 },
    {
      field: 'status',
      title: '状态',
      width: 70,
      align: 'center',
      slots: { default: 'statusSlot' },
    },
    {
      field: 'enableFlag',
      title: '启用',
      width: 60,
      align: 'center',
      slots: { default: 'enableFlagSlot' },
    },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    {
      align: 'center',
      field: 'operation',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 180,
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
      fieldName: 'empName',
      label: '姓名',
      rules: z.string().min(1, '请输入姓名'),
    },
    {
      component: 'Input',
      fieldName: 'empCode',
      label: '工号',
      rules: z.string().min(1, '请输入工号'),
    },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: '手机号',
    },
    {
      component: 'Input',
      fieldName: 'deptName',
      label: '部门名称',
    },
    {
      fieldName: 'deptId',
      label: '选择部门',
      component: 'Input',
      formItemClass: 'col-span-2',
    },
    {
      component: 'RadioGroup',
      fieldName: 'status',
      label: '状态',
      defaultValue: 1,
      componentProps: {
        options: [
          { label: '在职', value: 1 },
          { label: '离职', value: 0 },
        ],
      },
    },
    {
      component: 'RadioGroup',
      fieldName: 'enableFlag',
      label: '启用',
      defaultValue: 1,
      componentProps: {
        options: [
          { label: '启用', value: 1 },
          { label: '禁用', value: 0 },
        ],
      },
    },
  ];
}
