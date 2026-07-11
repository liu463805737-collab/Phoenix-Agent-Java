<script setup lang="ts">
import { ref, watch } from 'vue';

type PolicyTab = 'privacy' | 'terms';

const props = defineProps<{
  modelValue: boolean;
  initialTab?: PolicyTab;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'agree'): void;
}>();

const tab = ref<PolicyTab>(props.initialTab ?? 'privacy');

watch(
  () => props.initialTab,
  (value) => {
    if (value) tab.value = value;
  },
);

function close() {
  emit('update:modelValue', false);
}

function accept() {
  emit('agree');
  close();
}
</script>

<template>
  <transition name="policy">
    <div v-if="modelValue" class="policy-mask" @click.self="close">
      <div class="policy-dialog" role="dialog" aria-modal="true">
        <header class="policy-dialog__head">
          <div class="policy-tabs">
            <button
              type="button"
              :class="{ active: tab === 'privacy' }"
              @click="tab = 'privacy'"
            >
              隐私协议
            </button>
            <button
              type="button"
              :class="{ active: tab === 'terms' }"
              @click="tab = 'terms'"
            >
              服务条款
            </button>
          </div>
          <button
            type="button"
            class="policy-close"
            aria-label="关闭"
            @click="close"
          >
            ×
          </button>
        </header>

        <div class="policy-dialog__body">
          <template v-if="tab === 'privacy'">
            <h3>隐私协议</h3>
            <p>
              我们重视并尊重每一位用户的隐私。本协议说明 Phoenix Agent
              在你使用智能体服务过程中如何收集、使用、存储与保护你的信息。
            </p>
            <h4>1. 我们收集的信息</h4>
            <p>
              账户登录信息、对话内容、操作日志，以及为提供更优体验所必需的设备与浏览器基础信息。
            </p>
            <h4>2. 信息的使用</h4>
            <p>
              用于身份认证、会话记录、智能体能力调用与持续优化，不会用于与服务无关的第三方营销。
            </p>
            <h4>3. 信息的存储与保护</h4>
            <p>
              采用传输加密与访问控制，对敏感字段进行脱敏处理；你可随时通过管理员申请数据导出或注销账号。
            </p>
            <h4>4. 你的权利</h4>
            <p>你有权访问、更正、删除你的个人信息，并对授权范围进行调整。</p>
          </template>
          <template v-else>
            <h3>服务条款</h3>
            <p>
              欢迎使用 Phoenix
              Agent。请在使用前仔细阅读以下条款，使用即视为你已接受相关约束。
            </p>
            <h4>1. 服务说明</h4>
            <p>
              本平台提供多智能体协作能力，覆盖文档、数据、代码、运维等多个场景。
            </p>
            <h4>2. 账号与责任</h4>
            <p>请妥善保管账号与密码，由账号产生的所有操作均视为你本人行为。</p>
            <h4>3. 内容合规</h4>
            <p>
              请勿利用平台进行任何违反法律法规或公序良俗的行为，平台保留中止或终止服务的权利。
            </p>
            <h4>4. 服务变更</h4>
            <p>
              平台可能基于业务需要对功能进行调整或升级，重要变更将通过站内消息或公告告知。
            </p>
          </template>
        </div>

        <footer class="policy-dialog__foot">
          <button type="button" class="policy-btn ghost" @click="close">
            关闭
          </button>
          <button type="button" class="policy-btn primary" @click="accept">
            我已阅读并同意
          </button>
        </footer>
      </div>
    </div>
  </transition>
</template>

<style lang="scss" scoped>
.policy-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(8 12 36 / 55%);
  backdrop-filter: blur(6px);
}

.policy-dialog {
  display: flex;
  flex-direction: column;
  width: min(560px, calc(100% - 32px));
  max-height: min(80vh, 640px);
  overflow: hidden;
  color: #1f2937;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 24px 60px rgb(8 12 36 / 35%);

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #eef0f4;
  }

  &__body {
    flex: 1;
    padding: 20px 24px;
    overflow-y: auto;
    font-size: 13px;
    line-height: 1.75;
    color: #374151;

    h3 {
      margin: 0 0 12px;
      font-size: 16px;
      color: #0f172a;
    }

    h4 {
      margin: 16px 0 6px;
      font-size: 13px;
      color: #111827;
    }

    p {
      margin: 0 0 8px;
    }
  }

  &__foot {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
    padding: 12px 16px;
    background: #fafbff;
    border-top: 1px solid #eef0f4;
  }
}

.policy-tabs {
  display: inline-flex;
  padding: 4px;
  background: #f1f5f9;
  border-radius: 8px;

  button {
    padding: 6px 14px;
    font-size: 13px;
    color: #6b7280;
    cursor: pointer;
    background: transparent;
    border: none;
    border-radius: 6px;
    transition: all 0.18s ease;

    &.active {
      color: #2f6bff;
      background: #fff;
      box-shadow: 0 1px 2px rgb(15 23 42 / 8%);
    }
  }
}

.policy-close {
  width: 28px;
  height: 28px;
  font-size: 22px;
  line-height: 1;
  color: #9ca3af;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 6px;
  transition:
    background 0.18s ease,
    color 0.18s ease;

  &:hover {
    color: #1f2937;
    background: #f1f5f9;
  }
}

.policy-btn {
  height: 36px;
  padding: 0 16px;
  font-size: 13px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.18s ease;

  &.ghost {
    color: #4b5563;
    background: transparent;
    border: 1px solid #e5e7eb;

    &:hover {
      color: #1f2937;
      border-color: #cbd5e1;
    }
  }

  &.primary {
    color: #fff;
    background: #2f6bff;
    border: 1px solid #2f6bff;
    box-shadow: 0 6px 16px rgb(47 107 255 / 28%);

    &:hover {
      background: #1d4ed8;
      border-color: #1d4ed8;
    }
  }
}

.policy-enter-active,
.policy-leave-active {
  transition: opacity 0.18s ease;
}

.policy-enter-from,
.policy-leave-to {
  opacity: 0;
}
</style>
