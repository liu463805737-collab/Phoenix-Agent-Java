<script setup lang="ts">
import { computed, reactive, ref } from 'vue';

import PolicyDialog from './PolicyDialog.vue';

type PolicyTab = 'privacy' | 'terms';

const emit = defineEmits<{
  (e: 'submit', payload: { username: string; remember: boolean }): void;
}>();

const form = reactive({
  username: '',
  password: '',
  remember: true,
});

const agreed = ref(false);
const submitting = ref(false);
const errorMsg = ref('');
const agreementShake = ref(false);
const capsOn = ref(false);
const showPassword = ref(false);

const showPolicy = ref(false);
const policyTab = ref<PolicyTab>('privacy');

const canSubmit = computed(
  () => form.username.trim().length > 0 && form.password.length > 0,
);

function openPolicy(tab: PolicyTab) {
  policyTab.value = tab;
  showPolicy.value = true;
}

function triggerShake() {
  agreementShake.value = false;
  requestAnimationFrame(() => {
    agreementShake.value = true;
    window.setTimeout(() => (agreementShake.value = false), 500);
  });
}

function handleCapsLock(event: KeyboardEvent) {
  if (typeof event.getModifierState === 'function') {
    capsOn.value = event.getModifierState('CapsLock');
  }
}

async function handleSubmit() {
  errorMsg.value = '';
  if (!canSubmit.value) {
    errorMsg.value = '请输入用户名和密码';
    return;
  }
  if (!agreed.value) {
    errorMsg.value = '请先阅读并同意《隐私协议》与《服务条款》';
    triggerShake();
    return;
  }

  submitting.value = true;
  try {
    await new Promise((resolve) => setTimeout(resolve, 480));
    emit('submit', {
      username: form.username.trim(),
      remember: form.remember,
    });
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <section class="login-card">
    <header class="login-card__head">
      <h2>欢迎回来</h2>
      <p>请使用账号密码登录，与你的智能体团队继续协作</p>
    </header>

    <form class="login-form" novalidate @submit.prevent="handleSubmit">
      <label class="field">
        <span class="field__label">用户名</span>
        <div class="field__control">
          <svg class="field__icon" viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-3.33 0-8 1.67-8 5v1h16v-1c0-3.33-4.67-5-8-5Z"
              fill="currentColor"
            />
          </svg>
          <input
            v-model.trim="form.username"
            type="text"
            autocomplete="username"
            placeholder="请输入用户名 / 邮箱"
            maxlength="64"
          />
        </div>
      </label>

      <label class="field">
        <span class="field__label">密码</span>
        <div class="field__control">
          <svg class="field__icon" viewBox="0 0 24 24" aria-hidden="true">
            <path
              d="M17 9V7a5 5 0 0 0-10 0v2H5v12h14V9Zm-8 0V7a3 3 0 0 1 6 0v2Z"
              fill="currentColor"
            />
          </svg>
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            autocomplete="current-password"
            placeholder="请输入登录密码"
            maxlength="64"
            @keyup="handleCapsLock"
            @keydown="handleCapsLock"
          />
          <button
            type="button"
            class="field__toggle"
            :title="showPassword ? '隐藏密码' : '显示密码'"
            @click="showPassword = !showPassword"
          >
            <svg v-if="showPassword" viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M12 5c-5 0-9 4.5-10 7 1 2.5 5 7 10 7s9-4.5 10-7c-1-2.5-5-7-10-7Zm0 11a4 4 0 1 1 4-4 4 4 0 0 1-4 4Z"
                fill="currentColor"
              />
            </svg>
            <svg v-else viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M2.1 4.5 3.5 3.1l18.4 18.4-1.4 1.4-3.3-3.3A12 12 0 0 1 12 21C7 21 3 16.5 2 14a14 14 0 0 1 4-5.4Zm9.9 4a3.5 3.5 0 0 1 3.5 3.5 3.4 3.4 0 0 1-.3 1.4l-4.6-4.6a3.4 3.4 0 0 1 1.4-.3ZM12 5a12 12 0 0 1 10 7 13.6 13.6 0 0 1-3.1 4.1l-2.1-2.1A3.5 3.5 0 0 0 12 8.5a3.4 3.4 0 0 0-1.9.6L8.3 7.3A11.5 11.5 0 0 1 12 5Z"
                fill="currentColor"
              />
            </svg>
          </button>
          <span v-if="capsOn" class="field__caps" title="大写锁定已开启"
            >Caps</span
          >
        </div>
      </label>

      <div class="login-form__row">
        <label class="checkbox">
          <input v-model="form.remember" type="checkbox" />
          <span class="checkbox__box" />
          <span>记住登录</span>
        </label>
      </div>

      <p v-if="errorMsg" class="login-form__error" role="alert">
        {{ errorMsg }}
      </p>

      <button
        type="submit"
        class="submit-btn"
        :class="{ 'is-loading': submitting }"
        :disabled="submitting"
      >
        <span v-if="submitting" class="submit-btn__spinner" />
        <span>{{ submitting ? '登录中…' : '登 录' }}</span>
      </button>

      <label class="agreement" :class="{ 'is-shake': agreementShake }">
        <input v-model="agreed" type="checkbox" />
        <span class="checkbox__box" />
        <span class="agreement__text">
          我已阅读并同意
          <button
            type="button"
            class="agreement__link"
            @click.prevent="openPolicy('privacy')"
          >
            《隐私协议》
          </button>
          与
          <button
            type="button"
            class="agreement__link"
            @click.prevent="openPolicy('terms')"
          >
            《服务条款》
          </button>
        </span>
      </label>

      <p class="login-form__tip">还没有账号？请联系管理员开通访问权限</p>
    </form>

    <PolicyDialog
      v-model="showPolicy"
      :initial-tab="policyTab"
      @agree="agreed = true"
    />
  </section>
</template>

<style lang="scss" scoped>
.login-card {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 56px;
  color: #1f2937;
  background: rgb(255 255 255 / 96%);

  &__head {
    margin-bottom: 28px;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 700;
      color: #0f172a;
    }

    p {
      margin: 0;
      font-size: 13px;
      color: #6b7280;
    }
  }
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;

  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: -4px;
    font-size: 12px;
    color: #6b7280;
  }

  &__error {
    padding: 8px 12px;
    margin: -4px 0 0;
    font-size: 12px;
    color: #b42318;
    background: #fef3f2;
    border: 1px solid #fee4e2;
    border-radius: 8px;
  }

  &__tip {
    margin: 4px 0 0;
    font-size: 12px;
    color: #9ca3af;
    text-align: center;
  }
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;

  &__label {
    padding-left: 2px;
    font-size: 12px;
    color: #6b7280;
  }

  &__control {
    position: relative;
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 12px 0 40px;
    background: #f7f9fc;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    transition:
      border-color 0.18s ease,
      box-shadow 0.18s ease,
      background 0.18s ease;

    &:hover {
      border-color: #c7d2fe;
    }

    &:focus-within {
      background: #fff;
      border-color: #2f6bff;
      box-shadow: 0 0 0 4px rgb(47 107 255 / 12%);
    }

    input {
      flex: 1;
      height: 100%;
      font-size: 14px;
      color: #111827;
      outline: none;
      background: transparent;
      border: none;

      &::placeholder {
        color: #9ca3af;
      }
    }
  }

  &__icon {
    position: absolute;
    top: 50%;
    left: 12px;
    width: 18px;
    height: 18px;
    color: #9ca3af;
    transform: translateY(-50%);
  }

  &__toggle {
    flex: none;
    padding: 4px;
    color: #9ca3af;
    cursor: pointer;
    background: transparent;
    border: none;
    border-radius: 6px;
    transition:
      color 0.15s ease,
      background 0.15s ease;

    svg {
      display: block;
      width: 18px;
      height: 18px;
    }

    &:hover {
      color: #2f6bff;
      background: rgb(47 107 255 / 8%);
    }
  }

  &__caps {
    flex: none;
    padding: 2px 6px;
    margin-left: 6px;
    font-size: 11px;
    color: #b45309;
    background: #fef3c7;
    border-radius: 4px;
  }
}

.checkbox,
.agreement {
  display: inline-flex;
  gap: 8px;
  align-items: flex-start;
  font-size: 12px;
  line-height: 1.6;
  color: #4b5563;
  cursor: pointer;
  user-select: none;

  input {
    position: absolute;
    pointer-events: none;
    opacity: 0;
  }

  .checkbox__box {
    position: relative;
    flex: none;
    width: 16px;
    height: 16px;
    margin-top: 2px;
    background: #fff;
    border: 1.5px solid #cbd5e1;
    border-radius: 4px;
    transition: all 0.18s ease;

    &::after {
      position: absolute;
      top: 1px;
      left: 4px;
      width: 5px;
      height: 9px;
      content: '';
      border: solid #fff;
      border-width: 0 2px 2px 0;
      transform: rotate(45deg) scale(0);
      transition: transform 0.18s ease;
    }
  }

  input:checked + .checkbox__box {
    background: #2f6bff;
    border-color: #2f6bff;

    &::after {
      transform: rotate(45deg) scale(1);
    }
  }

  &:hover .checkbox__box {
    border-color: #2f6bff;
  }
}

.agreement {
  align-items: center;

  &__text {
    flex: 1;
    font-size: 12px;
    color: #6b7280;
  }

  &__link {
    padding: 0;
    font-size: 12px;
    color: #2f6bff;
    cursor: pointer;
    background: transparent;
    border: none;
    transition: color 0.15s ease;

    &:hover {
      color: #1d4ed8;
      text-decoration: underline;
    }
  }

  &.is-shake {
    animation: agreement-shake 0.45s ease;

    .checkbox__box {
      border-color: #f04438;
      box-shadow: 0 0 0 4px rgb(240 68 56 / 12%);
    }
  }
}

@keyframes agreement-shake {
  0%,
  100% {
    transform: translateX(0);
  }

  20% {
    transform: translateX(-6px);
  }

  40% {
    transform: translateX(6px);
  }

  60% {
    transform: translateX(-4px);
  }

  80% {
    transform: translateX(4px);
  }
}

.submit-btn {
  position: relative;
  display: inline-flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  height: 46px;
  margin-top: 4px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 4px;
  cursor: pointer;
  background: linear-gradient(135deg, #4079ff 0%, #2f6bff 50%, #1d4ed8 100%);
  background-position: 0% 50%;
  background-size: 200% 200%;
  border: none;
  border-radius: 12px;
  box-shadow:
    0 10px 24px rgb(47 107 255 / 35%),
    inset 0 -2px 0 rgb(0 0 0 / 8%);
  transition:
    transform 0.15s ease,
    box-shadow 0.18s ease,
    background-position 0.4s ease,
    opacity 0.18s ease;

  &:hover:not(:disabled) {
    background-position: 100% 50%;
    box-shadow: 0 14px 30px rgb(47 107 255 / 42%);
    transform: translateY(-1px);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.7;
  }

  &__spinner {
    width: 14px;
    height: 14px;
    border: 2px solid rgb(255 255 255 / 40%);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 960px) {
  .login-card {
    padding: 32px 28px;
  }
}
</style>
