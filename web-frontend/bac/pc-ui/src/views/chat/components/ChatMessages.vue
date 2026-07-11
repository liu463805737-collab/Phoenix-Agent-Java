<script setup lang="ts">
interface ChatMessage {
  id: string;
  role: 'assistant' | 'user';
  content: string;
}

const messages: ChatMessage[] = [
  {
    id: 'm1',
    role: 'assistant',
    content:
      '你好，我是数据洞察官。把你想问的指标或场景告诉我，我可以帮你拉数据、做对比、出可视化。',
  },
  {
    id: 'm2',
    role: 'user',
    content: '帮我看下上周华东大区的 GMV 同比变化',
  },
  {
    id: 'm3',
    role: 'assistant',
    content:
      '好的。上周（06-16 ~ 06-22）华东大区 GMV ¥ 1,284 万，同比下降 6.3%，主要受上海地区下滑 11% 拖累。要不要我按城市再下钻一层？',
  },
  { id: 'm4', role: 'user', content: '下钻到城市，并标出下滑超过 10% 的' },
  {
    id: 'm5',
    role: 'assistant',
    content:
      '已生成城市维度表：上海 -11.2%、杭州 -10.5%、宁波 -8.1%、苏州 +3.4%。其中上海、杭州达到预警阈值，建议进一步看品类与渠道结构。',
  },
  { id: 'm6', role: 'user', content: '把这个结果导出成图表，发我一份' },
  {
    id: 'm7',
    role: 'assistant',
    content:
      '已为你生成柱状图 + 同比标注，文件名 east_gmv_w25.png，正在上传到你的工作空间。需要我同时附一份解读纪要吗？',
  },
];
</script>

<template>
  <div class="chat-messages">
    <div class="chat-messages__inner">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="chat-message"
        :class="`chat-message--${msg.role}`"
      >
        <div
          v-if="msg.role === 'assistant'"
          class="chat-message__avatar chat-message__avatar--bot"
        >
          智
        </div>

        <div class="chat-message__bubble">
          {{ msg.content }}
        </div>

        <div
          v-if="msg.role === 'user'"
          class="chat-message__avatar chat-message__avatar--user"
        >
          U
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chat-messages {
  height: 100%;
  overflow-y: auto;
  background: #f7f9fc;

  &__inner {
    display: flex;
    flex-direction: column;
    gap: 18px;
    max-width: var(--pc-chat-content-width);
    padding: 24px 24px 16px;
    margin: 0 auto;
  }
}

.chat-message {
  display: flex;
  gap: 10px;
  align-items: flex-start;

  &--user {
    justify-content: flex-end;
  }

  &__avatar {
    display: flex;
    flex: 0 0 auto;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    font-size: 13px;
    font-weight: 600;
    border-radius: 50%;

    &--bot {
      color: var(--pc-primary);
      background: var(--pc-primary-soft);
    }

    &--user {
      color: #fff;
      background: var(--pc-bubble-user);
    }
  }

  &__bubble {
    max-width: 70%;
    padding: 10px 14px;
    font-size: 14px;
    line-height: 1.65;
    word-break: break-word;
    white-space: pre-wrap;
    border-radius: 10px;
  }

  &--assistant &__bubble {
    color: var(--pc-text);
    background: var(--pc-bubble-bot);
    border: 1px solid var(--pc-border-soft);
  }

  &--user &__bubble {
    color: #fff;
    background: var(--pc-bubble-user);
  }
}
</style>
