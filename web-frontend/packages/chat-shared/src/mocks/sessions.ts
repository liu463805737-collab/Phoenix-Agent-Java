import type { ChatSession } from '../types/chat';

const HOUR = 60 * 60 * 1000;
const DAY = 24 * HOUR;
const now = Date.now();

export const MOCK_SESSIONS: ChatSession[] = [
  {
    id: 'h1',
    title: '上周 GMV 同比为什么下降',
    preview: '帮我对比一下上周和上上周华东大区的成交额…',
    agentId: 's-data',
    updatedAt: now - 2 * HOUR,
  },
  {
    id: 'h2',
    title: '生成 Q2 城市渗透率热力图',
    preview: '想要一张带省份下钻能力的热力图…',
    agentId: 's-data',
    updatedAt: now - 4 * HOUR,
  },
  {
    id: 'h3',
    title: '新客 30 日留存异常排查',
    preview: '近 7 天新客留存掉到 18%，原因…',
    agentId: 's-data',
    updatedAt: now - 1 * DAY,
  },
  {
    id: 'h4',
    title: '门店补货预测脚本',
    preview: '帮我写一段 SQL，输出未来 7 天补货建议…',
    agentId: 's-code',
    updatedAt: now - 1 * DAY - 3 * HOUR,
  },
  {
    id: 'h5',
    title: '会员分层 RFM 方案',
    preview: '面向高客单复购客群，权重应该怎么调…',
    agentId: 's-data',
    updatedAt: now - 4 * DAY,
  },
  {
    id: 'h6',
    title: '直播间观众转化漏斗',
    preview: '从进入到下单这一段流失最严重…',
    agentId: 's-data',
    updatedAt: now - 6 * DAY,
  },
];
