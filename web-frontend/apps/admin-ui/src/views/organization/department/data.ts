import { z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';

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
      fieldName: 'companyId',
      hidden: true,
      label: 'companyId',
    },
    {
      component: 'Input',
      fieldName: 'pid',
      hidden: true,
      label: 'pid',
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
      componentProps: {
        options: [
          { label: '部门', value: 0 },
          { label: '组', value: 1 },
        ],
      },
    },
  ];
}
