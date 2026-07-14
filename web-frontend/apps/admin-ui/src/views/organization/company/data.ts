import { z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'cname',
      component: 'Input',
      label: '公司名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入公司名称' },
    },
    {
      fieldName: 'shortName',
      component: 'Input',
      label: '简称',
      componentProps: { placeholder: '请输入公司简称' },
    },
    {
      fieldName: 'code',
      component: 'Input',
      label: '公司编码',
      componentProps: { placeholder: '请输入公司编码' },
    },
  ];
}

export function useSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      hidden: true,
      label: 'id',
    },
    {
      component: 'Input',
      fieldName: 'cname',
      label: '公司名称',
      rules: z.string().min(2, '最小长度为2').max(64, '最大值为64'),
    },
    {
      component: 'Input',
      fieldName: 'shortName',
      label: '公司简称',
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: '公司编码',
      rules: z.string().min(1, '请输入公司编码').max(32, '最大值为32'),
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: '排序',
      componentProps: { min: 0, max: 999 },
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: '状态',
      componentProps: {
        options: [
          { label: '启用', value: 1 },
          { label: '禁用', value: 0 },
        ],
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    {
      field: 'cname',
      title: '公司名称',
      minWidth: 180,
      align: 'left',
      resizable: true,
    },
    {
      field: 'shortName',
      title: '简称',
      width: 120,
    },
    {
      field: 'code',
      title: '公司编码',
      width: 140,
    },
    {
      field: 'sort',
      title: '排序',
      width: 80,
    },
    {
      field: 'status',
      title: '状态',
      width: 80,
      slots: { default: 'statusSlot' },
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 180,
    },
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
