<script setup lang="ts">
interface Highlight {
  tone: 1 | 2 | 3;
  title: string;
  desc: string;
}

const highlights: Highlight[] = [
  { tone: 1, title: '多智能体协同', desc: '按场景一键切换，专家级响应' },
  { tone: 2, title: '企业级安全', desc: '会话隔离、权限可控、数据加密' },
  { tone: 3, title: '知识沉淀', desc: '对话即资产，随时回溯与复用' },
];

const year = new Date().getFullYear();
</script>

<template>
  <section class="brand-panel">
    <div class="brand-panel__top">
      <div class="brand-logo">
        <span class="brand-logo__mark">
          <img src="/imgs/logo.png" />
        </span>
        <div class="brand-logo__text">
          <strong>Phoenix Agent</strong>
          <span>智能体协作平台</span>
        </div>
      </div>
    </div>

    <div class="brand-panel__hero">
      <h1>让每一次对话<br />都成为生产力</h1>
      <p>
        汇聚多领域智能体，从数据分析、文档协作到代码辅助，<br />
        为你的工作流提供更聪明、更可靠的协作伙伴。
      </p>

      <ul class="brand-highlights">
        <li v-for="item in highlights" :key="item.title">
          <span class="dot" :class="`dot--${item.tone}`" />
          <div>
            <strong>{{ item.title }}</strong>
            <em>{{ item.desc }}</em>
          </div>
        </li>
      </ul>
    </div>

    <div class="brand-panel__footer">
      <span>© {{ year }} Phoenix Agent</span>
      <span>v 1.0.0</span>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.brand-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px 44px;
  overflow: hidden;
  color: #f4f7ff;
  background: linear-gradient(
    160deg,
    rgb(47 107 255 / 35%) 0%,
    rgb(124 92 255 / 18%) 55%,
    rgb(54 214 255 / 18%) 100%
  );

  &::after {
    position: absolute;
    inset: auto -40% -50% auto;
    width: 380px;
    height: 380px;
    pointer-events: none;
    content: '';
    background: radial-gradient(
      circle,
      rgb(255 255 255 / 15%) 0%,
      transparent 70%
    );
    border-radius: 50%;
  }

  &__top {
    position: relative;
    z-index: 1;
  }

  &__hero {
    position: relative;
    z-index: 1;
  }

  &__footer {
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: rgb(244 247 255 / 65%);
  }
}

.brand-logo {
  display: flex;
  gap: 12px;
  align-items: center;

  &__mark {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 38px;
    height: 38px;

    /* background: rgb(255 255 255 / 15%);
    border-radius: 10px;
    box-shadow: 0 6px 18px rgb(47 107 255 / 35%); */

    svg {
      width: 24px;
      height: 24px;
    }
  }

  &__text {
    display: flex;
    flex-direction: column;
    line-height: 1.2;

    strong {
      font-size: 16px;
      color: #fff;
    }

    span {
      font-size: 12px;
      color: rgb(244 247 255 / 70%);
    }
  }
}

.brand-panel__hero {
  h1 {
    margin: 0 0 14px;
    font-size: 34px;
    font-weight: 700;
    line-height: 1.25;
    color: #fff;
  }

  p {
    margin: 0 0 28px;
    font-size: 14px;
    line-height: 1.7;
    color: rgb(244 247 255 / 78%);
  }
}

.brand-highlights {
  display: grid;
  gap: 14px;
  padding: 0;
  margin: 0;
  list-style: none;

  li {
    display: flex;
    gap: 12px;
    align-items: flex-start;
    padding: 12px 14px;
    background: rgb(255 255 255 / 8%);
    border: 1px solid rgb(255 255 255 / 8%);
    border-radius: 12px;
    backdrop-filter: blur(8px);
  }

  .dot {
    flex: none;
    width: 10px;
    height: 10px;
    margin-top: 6px;
    background: #7aa7ff;
    border-radius: 50%;
    box-shadow: 0 0 12px rgb(122 167 255 / 70%);

    &--2 {
      background: #c9b1ff;
      box-shadow: 0 0 12px rgb(201 177 255 / 70%);
    }

    &--3 {
      background: #76e7ff;
      box-shadow: 0 0 12px rgb(118 231 255 / 70%);
    }
  }

  strong {
    display: block;
    margin-bottom: 2px;
    font-size: 14px;
    color: #fff;
  }

  em {
    font-size: 12px;
    font-style: normal;
    color: rgb(244 247 255 / 70%);
  }
}

@media (max-width: 960px) {
  .brand-panel {
    min-height: 220px;
    padding: 32px 28px;
  }

  .brand-panel__hero h1 {
    font-size: 26px;
  }

  .brand-highlights {
    display: none;
  }
}
</style>
