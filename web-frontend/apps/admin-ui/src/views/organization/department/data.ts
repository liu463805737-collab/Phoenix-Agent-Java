import { z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'keyword',
      component: 'Input',
      label: '关键字',
      labelWidth: 60,
      componentProps: {
        placeholder: '请输入名称/编码',
        allowClear: true,
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    {
      title: '名称',
      field: 'name',
      minWidth: 200,
      align: 'left',
      resizable: true,
      treeNode: true,
    },
    {
      title: '编码',
      field: 'code',
      width: 120,
      align: 'left',
      resizable: true,
    },
    {
      title: '上级部门',
      field: 'parentName',
      width: 140,
      align: 'left',
    },
    {
      title: '排序',
      field: 'orderNo',
      width: 70,
    },
    {
      title: '状态',
      field: 'status',
      width: 80,
      slots: { default: 'statusSlot' },
    },
    {
      title: '部门性质',
      field: 'nature',
      width: 100,
      slots: { default: 'natureSlot' },
    },
    {
      title: '创建时间',
      field: 'createTime',
      width: 180,
    },
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
      fieldName: 'companyId',
      label: 'companyId',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      component: 'Input',
      fieldName: 'pid',
      label: 'pid',
      dependencies: {
        triggerFields: [''],
        show: false,
      },
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: '部门名称',
      rules: z.string().min(2, '最小长度为2').max(64, '最大值为64'),
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: '部门编码',
      rules: z.string().min(1, '请输入部门编码').max(32, '最大值为32'),
    },
    {
      component: 'InputNumber',
      fieldName: 'orderNo',
      label: '排序号',
      componentProps: { min: 0, max: 999 },
    },
    {
      component: 'RadioGroup',
      fieldName: 'status',
      label: '状态',
      defaultValue: 0,
      componentProps: {
        options: [
          { label: '启用', value: 0 },
          { label: '禁用', value: 1 },
        ],
      },
    },
    {
      component: 'RadioGroup',
      fieldName: 'nature',
      label: '部门性质',
      defaultValue: 0,
      componentProps: {
        options: [
          { label: '部门', value: 0 },
          { label: '组', value: 1 },
        ],
      },
    },
  ];
}
