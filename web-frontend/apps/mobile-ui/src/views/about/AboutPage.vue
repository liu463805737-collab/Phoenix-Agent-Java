<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <div class="about">
    <header class="about__nav">
      <button
        type="button"
        class="about__back"
        aria-label="返回"
        @click="router.back()"
      >
        <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
          <path
            d="m14 6-6 6 6 6"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
      <div class="about__title">关于我们</div>
      <div class="about__nav-spacer" />
    </header>

    <section class="about__hero">
      <div class="about__logo">
        <img src="/imgs/logo.png" alt="Phoenix" class="about__logo-img" />
      </div>
      <h1 class="about__name">Phoenix — 企业级智能体管理平台</h1>
    </section>

    <section class="about__body">
      <p class="about__desc">
        Phoenix 是一个基于 Spring AI Alibaba Graph 打造的企业级智能体（AI Agent）管理与数据分析平台。平台分为两大核心模块：
      </p>
      <ul class="about__list">
        <li>
          <strong>phoenix-data</strong>（智能报表引擎）：以 StateGraph 状态机驱动，将自然语言转换为可执行的 SQL 与 Python 分析流程，超越传统 Text-to-SQL 工具，进化为具备多轮对话、意图识别、知识增强、人工反馈的全链路 AI 数据分析师。
        </li>
        <li>
          <strong>phoenix-agent</strong>（自定义智能体框架）：提供 React Agent、Workflow Graph、会话管理、长期记忆、用户画像、MCP 工具等完整的智能体开发能力，支持快速构建和发布企业级 AI 智能体。
        </li>
      </ul>
      <p class="about__desc">
        系统采用 Vben 5 现代前端框架，实现前台（数据分析师交互）与后台（智能体管理、数据源配置、用户权限）的统一管理，提供开箱即用的企业级管理界面。
      </p>
      <p class="about__desc">
        技术上，系统采用高度可扩展的多模块架构设计，全面兼容 OpenAI 接口规范的对话模型与 Embedding 模型，支持灵活挂载多种向量数据库，并提供 MySQL、PostgreSQL、Oracle、SQL Server、H2、Hive、达梦等主流数据库的即开即用支持。同时原生支持 MCP 协议，可无缝集成 Claude Desktop 等 MCP 生态工具。
      </p>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.about {
  min-height: 100dvh;
  padding-bottom: var(--m-safe-bottom);
  background: var(--m-bg);
}

.about__nav {
  display: flex;
  gap: 8px;
  align-items: center;
  height: calc(var(--m-navbar-h) + var(--m-safe-top));
  padding: 6px 8px;
  padding-top: calc(var(--m-safe-top) + 6px);
}

.about__back {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: var(--m-text-primary);
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 10px;
}

.about__back:active {
  background: var(--m-border-soft);
}

.about__title {
  flex: 1 1 auto;
  font-size: 16px;
  font-weight: 600;
  color: var(--m-text-primary);
  text-align: center;
}

.about__nav-spacer {
  flex: 0 0 auto;
  width: 40px;
}

.about__hero {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  padding: 32px 16px 24px;
}

.about__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8eeff 100%);
  border-radius: 18px;
  box-shadow: 0 8px 20px rgb(64 121 255 / 20%);
}

.about__logo-img {
  width: 48px;
  height: 48px;
}

.about__name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--m-text-primary);
  text-align: center;
  line-height: 1.4;
}

.about__body {
  padding: 0 16px 32px;
}

.about__desc {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--m-text-secondary);
}

.about__list {
  margin: 0 0 12px;
  padding-left: 18px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--m-text-secondary);
}

.about__list li {
  margin-bottom: 8px;
}
</style>
