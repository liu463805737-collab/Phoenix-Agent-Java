<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  useAgentStore,
  useChatStore,
  useLoginFlow,
} from '@phoenix/chat-shared';
import { showFailToast } from 'vant';

const router = useRouter();
const route = useRoute();
const { submit } = useLoginFlow();

const form = reactive({
  username: '',
  password: '',
});
const submitting = ref(false);

async function handleSubmit() {
  const username = form.username.trim();
  if (!username) {
    showFailToast('请输入账号');
    return;
  }
  submitting.value = true;
  try {
    await submit({
      username,
      password: form.password,
      remember: true,
    });
    await Promise.all([
      useAgentStore().loadAll(),
      useChatStore().loadSessions(),
    ]);
    const redirect =
      typeof route.query.redirect === 'string' ? route.query.redirect : '/chat';
    await router.replace(redirect || '/chat');
  } catch (e: any) {
    showFailToast(e?.message || '登录失败，请检查账号密码');
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="login">
    <header class="login__hero">
      <div class="login__logo">
        <img src="/imgs/logo.png" alt="Phoenix" class="login__logo-img" />
      </div>
      <div class="login__title">Phoenix 智能体助手</div>
      <div class="login__slogan">一句话，让数据为你做事</div>
    </header>

    <form class="login__form" @submit.prevent="handleSubmit">
      <label class="field">
        <span class="field__label">账号</span>
        <input
          v-model="form.username"
          class="field__input"
          type="text"
          autocomplete="username"
          inputmode="text"
          placeholder="请输入账号"
        />
      </label>
      <label class="field">
        <span class="field__label">密码</span>
        <input
          v-model="form.password"
          class="field__input"
          type="password"
          autocomplete="current-password"
          placeholder="请输入密码"
        />
      </label>

      <button type="submit" class="login__submit" :disabled="submitting">
        {{ submitting ? '登录中…' : '登录' }}
      </button>
    </form>

    <footer class="login__footer">
      登录即代表同意 <a href="#">用户协议</a> 与 <a href="#">隐私政策</a>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.login {
  display: flex;
  flex-direction: column;
  min-height: 100dvh;
  padding: calc(var(--m-safe-top) + 56px) 24px calc(var(--m-safe-bottom) + 20px);
  background: var(--m-bg);
}

.login__hero {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  margin-bottom: 40px;
}

.login__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #f0f4ff 0%, #e8eeff 100%);
  border-radius: 18px;
  box-shadow: 0 8px 20px rgb(64 121 255 / 20%);
}

.login__logo-img {
  width: 48px;
  height: 48px;
}

.login__title {
  font-size: 22px;
  font-weight: 600;
  color: var(--m-text-primary);
}

.login__slogan {
  font-size: 13px;
  color: var(--m-text-soft);
}

.login__form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.login__submit {
  height: 48px;
  margin-top: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  cursor: pointer;
  background: var(--m-brand-primary);
  border: none;
  border-radius: 14px;
}

.login__submit:disabled {
  opacity: 0.55;
}

.login__submit:active:not(:disabled) {
  opacity: 0.85;
}

.login__footer {
  margin-top: auto;
  font-size: 12px;
  color: var(--m-text-muted);
  text-align: center;
}

.login__footer a {
  color: var(--m-brand-primary);
  text-decoration: none;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field__label {
  padding-left: 4px;
  font-size: 13px;
  color: var(--m-text-soft);
}

.field__input {
  height: 48px;
  padding: 0 14px;
  font-size: 15px;
  color: var(--m-text-primary);
  outline: none;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 14px;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.field__input::placeholder {
  color: var(--m-text-muted);
}

.field__input:focus {
  border-color: var(--m-brand-primary);
  box-shadow: 0 0 0 3px rgb(47 107 255 / 12%);
}
</style>
