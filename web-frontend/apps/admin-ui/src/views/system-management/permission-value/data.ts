import { z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      component: 'Input',
      label: '权限名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入权限名称' },
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
      fieldName: 'name',
      label: '权限名称',
      rules: z
        .string()
        .min(2, '最小长度为2')
        .max(32, '最大值为32'),
    },
    {
      component: 'InputNumber',
      fieldName: 'position',
      label: '位值',
      componentProps: { min: 0, max: 63 },
      rules: z.number().min(0, '位值不能小于0').max(63, '位值不能大于63'),
    },
    {
      component: 'InputNumber',
      fieldName: 'orderNo',
      label: '排序号',
      componentProps: { min: 0, max: 999 },
    },
    {
      component: 'Input',
      fieldName: 'remark',
      label: '备注',
      componentProps: {
        type: 'textarea',
        rows: 2,
        placeholder: '请输入备注',
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    {
      field: 'name',
      title: '权限名称',
      minWidth: 150,
      align: 'left',
      resizable: true,
    },
    {
      field: 'position',
      title: '位值',
      width: 80,
    },
    {
      field: 'orderNo',
      title: '排序号',
      width: 80,
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 180,
      showOverflow: true,
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 180,
    },
    {
      align: 'center',
      field: 'operation',
      slots: { default: 'action' },
      title: '操作',
      width: 180,
    },
  ];
}
