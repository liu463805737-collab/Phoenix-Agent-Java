<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@phoenix/chat-shared';

const router = useRouter();
const auth = useAuthStore();
const { user } = storeToRefs(auth);
</script>

<template>
  <div class="setting">
    <header class="setting__nav">
      <button
        type="button"
        class="setting__back"
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
      <div class="setting__title">个人设置</div>
      <div class="setting__nav-spacer" />
    </header>

    <section class="setting__group">
      <div class="setting-cell">
        <span class="setting-cell__label">用户名</span>
        <span class="setting-cell__value">{{ user?.username }}</span>
      </div>
      <div class="setting-cell">
        <span class="setting-cell__label">真实姓名</span>
        <span class="setting-cell__value">{{ user?.realName || '-' }}</span>
      </div>
      <div class="setting-cell">
        <span class="setting-cell__label">手机号</span>
        <span class="setting-cell__value">{{ user?.phone || '-' }}</span>
      </div>
      <div class="setting-cell">
        <span class="setting-cell__label">邮箱</span>
        <span class="setting-cell__value">{{ user?.email || '-' }}</span>
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.setting {
  min-height: 100dvh;
  padding-bottom: var(--m-safe-bottom);
  background: var(--m-bg);
}

.setting__nav {
  display: flex;
  gap: 8px;
  align-items: center;
  height: calc(var(--m-navbar-h) + var(--m-safe-top));
  padding: 6px 8px;
  padding-top: calc(var(--m-safe-top) + 6px);
}

.setting__back {
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

.setting__back:active {
  background: var(--m-border-soft);
}

.setting__title {
  flex: 1 1 auto;
  font-size: 16px;
  font-weight: 600;
  color: var(--m-text-primary);
  text-align: center;
}

.setting__nav-spacer {
  flex: 0 0 auto;
  width: 40px;
}

.setting__group {
  margin: 12px 14px;
  overflow: hidden;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border-soft);
  border-radius: 14px;
}

.setting-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 16px;
  font-size: 15px;
  color: var(--m-text-primary);
}

.setting-cell + .setting-cell {
  border-top: 1px solid var(--m-border-soft);
}

.setting-cell__label {
  color: var(--m-text-muted);
}

.setting-cell__value {
  font-weight: 500;
}
</style>
