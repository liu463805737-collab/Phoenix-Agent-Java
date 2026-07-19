import type { Agent } from '../types/agent';

export const MOCK_AGENTS: Agent[] = [
  {
    id: 's-data',
    sn: '',
    name: '数据洞察官',
    description: '帮你做查询、透视和可视化分析',
    avatar: '数',
    tag: '常用',
  },
  {
    id: 's-doc',
    sn: '',
    name: '文档协作助手',
    description: '总结、改写、翻译长文与会议纪要',
    avatar: '文',
  },
  {
    id: 's-code',
    sn: '',
    name: '代码副驾',
    description: '代码生成、解释、Code Review',
    avatar: '码',
    tag: '新',
  },
  {
    id: 's-ops',
    sn: '',
    name: '运维巡检员',
    description: '排障建议、告警归因、SOP 引导',
    avatar: '运',
  },
  {
    id: 's-write',
    sn: '',
    name: '文案策划师',
    description: '营销文案、活动方案、品牌叙事',
    avatar: '创',
  },
  {
    id: 's-plan',
    sn: '',
    name: '出行规划官',
    description: '行程编排、景点推荐、预算估算',
    avatar: '行',
  },
];
