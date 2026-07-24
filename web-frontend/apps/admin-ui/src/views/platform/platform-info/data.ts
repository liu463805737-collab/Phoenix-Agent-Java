import type { VbenFormSchema } from '#/adapter/form';

import { z } from '#/adapter/form';

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
        placeholder: '请选择平台类型',
        options: platformTypeOptions,
        style: { width: '200px' },
      },
    },
  ];
}

export interface ColumnDef {
  prop?: string;
  label: string;
  width?: number;
  minWidth?: number;
  slot?: string;
}

export function useColumns(): ColumnDef[] {
  return [
    { prop: 'name', label: '平台名称', minWidth: 150 },
    { label: '平台类型', width: 110, slot: 'type' },
    { label: '状态', width: 80, slot: 'status' },
    { prop: 'corpid', label: '企业ID', width: 180 },
    { label: 'Secret', width: 220, slot: 'secret' },
    { prop: 'agentid', label: 'AgentId', width: 100 },
    { prop: 'appKey', label: 'AppKey', width: 200 },
    { label: '创建时间', width: 170, slot: 'time' },
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
