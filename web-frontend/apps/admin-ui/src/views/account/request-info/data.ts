import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridColumns } from '#/adapter/vxe-table';

export function useSearchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'agentId',
      component: 'Input',
      label: '智能体ID',
      labelWidth: 60,
      componentProps: { placeholder: '输入智能体ID' },
    },
    {
      fieldName: 'keyword',
      component: 'Input',
      label: '关键字',
      labelWidth: 60,
      componentProps: { placeholder: '标题/内容/用户名/姓名' },
    },
    {
      fieldName: 'role',
      component: 'Select',
      label: '角色',
      componentProps: {
        placeholder: '全部',
        clearable: true,
        options: [
          { label: '用户(user)', value: 'user' },
          { label: '助手(assistant)', value: 'assistant' },
          { label: '系统(system)', value: 'system' },
        ],
      },
    },
    {
      fieldName: 'startTime',
      component: 'DatePicker',
      label: '开始时间',
      componentProps: {
        type: 'datetime',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '开始时间',
      },
    },
    {
      fieldName: 'endTime',
      component: 'DatePicker',
      label: '结束时间',
      componentProps: {
        type: 'datetime',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '结束时间',
      },
    },
  ];
}

export function useColumns(): VxeTableGridColumns {
  return [
    { prop: 'createTime', label: '时间', width: 180, align: 'center' },
    { prop: 'username', label: '用户名', width: 120 },
    { prop: 'realName', label: '真实姓名', width: 100 },
    { prop: 'agentName', label: '智能体', width: 150 },
    { prop: 'title', label: '会话标题', minWidth: 160, 'show-overflow-tooltip': true },
    { prop: 'role', label: '角色', width: 90, align: 'center' },
    { prop: 'messageType', label: '消息类型', width: 100, align: 'center' },
    { prop: 'content', label: '内容', minWidth: 250, 'show-overflow-tooltip': true },
    {
      prop: 'sessionId',
      label: '会话ID',
      width: 120,
      'show-overflow-tooltip': true,
    },
  ];
}

export function useAgentStatColumns(): VxeTableGridColumns {
  return [
    { prop: 'agentName', label: '智能体名称', width: 160 },
    { prop: 'agentId', label: '智能体ID', width: 90, align: 'center' },
    { prop: 'requestCount', label: '请求数', width: 100, align: 'center' },
    { prop: 'userCount', label: '使用人数', width: 100, align: 'center' },
    { prop: 'lastRequestTime', label: '最后请求时间', width: 180, align: 'center' },
  ];
}

export function useUserStatColumns(): VxeTableGridColumns {
  return [
    { prop: 'username', label: '用户名', width: 120 },
    { prop: 'realName', label: '真实姓名', width: 100 },
    { prop: 'requestCount', label: '请求数', width: 100, align: 'center' },
    { prop: 'agentCount', label: '涉及智能体', width: 100, align: 'center' },
    { prop: 'lastRequestTime', label: '最后请求时间', width: 180, align: 'center' },
  ];
}