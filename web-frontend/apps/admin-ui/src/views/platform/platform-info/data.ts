import type { VbenFormSchema } from '#/adapter/form';

import { z } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export const platformTypeOptions = [
  { value: 'dingtalk', label: '钉钉' },
  { value: 'feishu', label: '飞书' },
  { value: 'weixin', label: '企业微信' },
];

export const typeLabels: Record<string, string> = {
  dingtalk: '钉钉',
  feishu: '飞书',
  weixin: '企业微信',
};

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      component: 'Input',
      label: '平台名称',
      labelWidth: 60,
      componentProps: { placeholder: '请输入平台名称' },
    },
    {
      fieldName: 'type',
      component: 'Select',
      label: '平台类型',
      componentProps: {
        clearable: true,
        placeholder: '请选择平台类型',
        options: platformTypeOptions,
        style: { width: '200px' },
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    { field: 'name', title: '平台名称', minWidth: 150, align: 'left', resizable: true },
    {
      field: 'type',
      title: '平台类型',
      width: 110,
      align: 'center',
      slots: { default: 'typeSlot' },
    },
    {
      field: 'status',
      title: '状态',
      width: 80,
      align: 'center',
      slots: { default: 'statusSlot' },
    },
    { field: 'corpid', title: '企业ID', width: 180, align: 'left' },
    {
      field: 'corpsecret',
      title: 'Secret',
      width: 220,
      align: 'left',
      slots: { default: 'secretSlot' },
    },
    { field: 'agentid', title: 'AgentId', width: 100, align: 'left' },
    { field: 'appKey', title: 'AppKey', width: 200, align: 'left' },
    {
      field: 'createTime',
      title: '创建时间',
      width: 170,
      align: 'center',
      slots: { default: 'timeSlot' },
    },
    {
      align: 'center',
      field: 'operation',
      fixed: 'right',
      slots: { default: 'action' },
      title: '操作',
      width: 160,
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
      fieldName: 'name',
      label: '平台名称',
      component: 'Input',
      componentProps: { placeholder: '请输入平台名称' },
      rules: z.string().min(1, '请输入平台名称'),
    },
    {
      fieldName: 'type',
      label: '平台类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择平台类型',
        options: platformTypeOptions,
      },
      rules: z.string().min(1, '请选择平台类型'),
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        options: [
          { label: '启用', value: '1' },
          { label: '禁用', value: '0' },
        ],
      },
    },
    {
      fieldName: 'corpid',
      label: '企业ID(CorpId)',
      component: 'Input',
      componentProps: { placeholder: '请输入企业ID' },
    },
    {
      fieldName: 'corpsecret',
      label: 'Secret',
      component: 'Input',
      componentProps: {
        placeholder: '请输入Secret',
        type: 'password',
        showPassword: true,
      },
    },
    {
      fieldName: 'agentid',
      label: 'AgentId',
      component: 'Input',
      componentProps: { placeholder: '请输入AgentId' },
    },
    {
      fieldName: 'appKey',
      label: 'AppKey',
      component: 'Input',
      componentProps: { placeholder: '请输入AppKey（钉钉/飞书OAuth用）' },
    },
  ];
}
