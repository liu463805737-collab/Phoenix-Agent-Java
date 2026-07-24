import { z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      component: 'Input',
      label: '组名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入组名称' },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    { field: 'name', title: '组名称', minWidth: 120 },
    { field: 'sn', title: '组编码', width: 120 },
    {
      field: 'description',
      title: '描述',
      minWidth: 200,
      showOverflow: true,
    },
    {
      field: 'status',
      title: '状态',
      width: 80,
      align: 'center',
      slots: { default: 'statusSlot' },
    },
    { field: 'createTime', title: '创建时间', width: 180, align: 'center' },
    {
      align: 'center',
      field: 'operation',
      slots: { default: 'action' },
      title: '操作',
      width: 380,
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
      fieldName: 'sn',
      label: '组编码',
      rules: z.string().min(1, '请输入组编码'),
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: '组名称',
      rules: z.string().min(1, '请输入组名称'),
    },
    {
      component: 'Input',
      fieldName: 'description',
      label: '描述',
      componentProps: {
        type: 'textarea',
        rows: 3,
        placeholder: '请输入组描述',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      defaultValue: '0',
      componentProps: {
        options: [
          { label: '启用', value: '0' },
          { label: '禁用', value: '1' },
        ],
      },
    },
  ];
}
