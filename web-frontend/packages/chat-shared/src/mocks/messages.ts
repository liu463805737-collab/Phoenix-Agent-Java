import type { ChatMessage } from '../types/chat';

const base = Date.now() - 60 * 60 * 1000;

/** 按 sessionId 分组的初始消息历史 */
export const MOCK_MESSAGES: Record<string, ChatMessage[]> = {
  h1: [
    {
      id: 'h1-m1',
      role: 'assistant',
      content:
        '你好，我是数据洞察官。把你想问的指标或场景告诉我，我可以帮你拉数据、做对比、出可视化。',
      createdAt: base,
    },
    {
      id: 'h1-m2',
      role: 'user',
      content: '帮我看下上周华东大区的 GMV 同比变化',
      createdAt: base + 60_000,
    },
    {
      id: 'h1-m3',
      role: 'assistant',
      content:
        '好的。上周（06-16 ~ 06-22）华东大区 GMV ¥ 1,284 万，同比下降 6.3%，主要受上海地区下滑 11% 拖累。要不要我按城市再下钻一层？',
      createdAt: base + 120_000,
    },
    {
      id: 'h1-m4',
      role: 'user',
      content: '下钻到城市，并标出下滑超过 10% 的',
      createdAt: base + 180_000,
    },
    {
      id: 'h1-m5',
      role: 'assistant',
      content:
        '已生成城市维度表：上海 -11.2%、杭州 -10.5%、宁波 -8.1%、苏州 +3.4%。其中上海、杭州达到预警阈值，建议进一步看品类与渠道结构。',
      createdAt: base + 240_000,
    },
    {
      id: 'h1-m6',
      role: 'user',
      content: '把这个结果导出成图表，发我一份',
      createdAt: base + 300_000,
    },
    {
      id: 'h1-m7',
      role: 'assistant',
      content:
        '已为你生成柱状图 + 同比标注，文件名 east_gmv_w25.png，正在上传到你的工作空间。需要我同时附一份解读纪要吗？',
      createdAt: base + 360_000,
    },
  ],
  h2: [
    {
      id: 'h2-m1',
      role: 'user',
      content: '想要一张带省份下钻能力的 Q2 城市渗透率热力图',
      createdAt: base + 60_000,
    },
    {
      id: 'h2-m2',
      role: 'assistant',
      content:
        '可以。先按省级 GMV / 活跃门店数加权得到渗透率，再用分层等距色阶。下钻到城市后保留同色系深浅，避免阅读跳变。要不要我先给你看一版省份预览？',
      createdAt: base + 120_000,
    },
  ],
  h3: [
    {
      id: 'h3-m1',
      role: 'user',
      content: '近 7 天新客 30 日留存掉到 18%，帮我看看可能的原因',
      createdAt: base + 60_000,
    },
    {
      id: 'h3-m2',
      role: 'assistant',
      content:
        '快速看了三类信号：渠道结构里短视频投放占比涨了 12pct、首单优惠门槛从 39 提到 49、新客次日唤醒 push 失败率上升。建议先复核第二条，价格门槛通常对新客 D7 影响最直接。',
      createdAt: base + 120_000,
    },
  ],
  h4: [
    {
      id: 'h4-m1',
      role: 'user',
      content: '帮我写一段 SQL，输出未来 7 天的门店补货建议',
      createdAt: base + 60_000,
    },
    {
      id: 'h4-m2',
      role: 'assistant',
      content:
        '思路是用过去 28 天移动平均 + 季节系数预测日销，再减去在途库存。下面给你一版 Hive SQL 模板，按门店和 SKU 维度输出补货量。',
      createdAt: base + 120_000,
    },
  ],
  h5: [
    {
      id: 'h5-m1',
      role: 'user',
      content: '面向高客单复购客群，RFM 三项权重应该怎么调？',
      createdAt: base + 60_000,
    },
    {
      id: 'h5-m2',
      role: 'assistant',
      content:
        '该客群对 M（客单）和 F（频次）更敏感，R（最近一次）相对弱。建议 R:F:M ≈ 2:3:5，并把 M 的分箱区间在 75 分位以上再切一刀，能更好区分核心人群。',
      createdAt: base + 120_000,
    },
  ],
  h6: [
    {
      id: 'h6-m1',
      role: 'user',
      content: '直播间从进入到下单流失最严重，怎么定位？',
      createdAt: base + 60_000,
    },
    {
      id: 'h6-m2',
      role: 'assistant',
      content:
        '把漏斗拆成进入 → 停留 30s → 点击商品卡 → 进入详情 → 下单五段，再叠加“讲解时段”维度看。一般最大的坎是“点击商品卡 → 进入详情”，往往是货架排序或卡片转化文案问题。',
      createdAt: base + 120_000,
    },
  ],
};
