import { z } from '#/adapter/form';
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      component: 'Input',
      label: '角色名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入角色名称' },
    },
    {
      fieldName: 'sn',
      component: 'Input',
      label: '角色标识',
      componentProps: { placeholder: '请输入角色标识' },
    },
  ];
}

/**
 * 编辑角色
 */
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
      label: '角色名称',
      rules: z
          .string()
          .min(2, '最小长度为2')
          .max(
              32,
              '最大值为32',
          ),
    },
    {
      component: 'Input',
      fieldName: 'sn',
      label: '角色标识',
    },
  ];
}

export function useColumns<T = SystemUserApi.SystemUser>(
    onStatusChange?: (newStatus: any, row: T) => PromiseLike<boolean | undefined>,
): VxeTableGridColumns {
  return [
    {
      field: 'name',
      title: '角色名称',
      minWidth: 200,
      align: 'left',
      resizable: true
    },
    {
      field: 'sn',
      title: '角色标识',
      align: 'left',
      minWidth: 200,
      resizable: true
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 200,
    },
    {
      align: 'center',
      field: 'operation',
      slots: { default: 'action' },
      title: '操作',
      width: 250,
    },
  ];
}
