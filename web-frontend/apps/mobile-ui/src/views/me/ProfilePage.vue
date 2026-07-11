<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore, useChatStore } from '@phoenix/chat-shared';
import { showConfirmDialog } from 'vant';

const router = useRouter();
const auth = useAuthStore();
const chat = useChatStore();
const { user } = storeToRefs(auth);

const displayName = computed(
  () => user.value?.displayName || user.value?.username || '未登录用户',
);
const avatarChar = computed(() => {
  const ch = [...displayName.value][0] ?? 'U';
  return ch.toUpperCase();
});
const loginAt = computed(() => {
  if (!user.value?.loginAt) return '本地体验账号';
  const d = new Date(user.value.loginAt);
  const hh = String(d.getHours()).padStart(2, '0');
  const mm = String(d.getMinutes()).padStart(2, '0');
  return `本次登录 ${hh}:${mm}`;
});

async function handleLogout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确认要退出当前账号吗？',
    });
    chat.reset();
    await auth.logout();
    await router.replace('/login');
  } catch {
    /* canceled */
  }
}
</script>

<template>
  <div class="profile">
    <header class="profile__nav">
      <button
        type="button"
        class="profile__back"
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
      <div class="profile__title">我的</div>
      <div class="profile__nav-spacer" />
    </header>

    <section class="profile__hero">
      <div class="profile__avatar">{{ avatarChar }}</div>
      <div class="profile__name">{{ displayName }}</div>
      <div class="profile__sub">{{ loginAt }}</div>
    </section>

    <section class="profile__group">
      <button type="button" class="profile-cell" @click="router.push('/me/setting')">
        <span class="profile-cell__label">个人设置</span>
        <span class="profile-cell__caret">›</span>
      </button>
      <button type="button" class="profile-cell" @click="router.push('/about')">
        <span class="profile-cell__label">关于我们</span>
        <span class="profile-cell__caret">›</span>
      </button>
    </section>

    <section class="profile__group">
      <button
        type="button"
        class="profile-cell profile-cell--danger"
        @click="handleLogout"
      >
        <span class="profile-cell__label">退出登录</span>
        <span class="profile-cell__caret">›</span>
      </button>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.profile {
  min-height: 100dvh;
  padding-bottom: var(--m-safe-bottom);
  background: var(--m-bg);
}

.profile__nav {
  display: flex;
  gap: 8px;
  align-items: center;
  height: calc(var(--m-navbar-h) + var(--m-safe-top));
  padding: 6px 8px;
  padding-top: calc(var(--m-safe-top) + 6px);
}

.profile__back {
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

.profile__back:active {
  background: var(--m-border-soft);
}

.profile__title {
  flex: 1 1 auto;
  font-size: 16px;
  font-weight: 600;
  color: var(--m-text-primary);
  text-align: center;
}

.profile__nav-spacer {
  flex: 0 0 auto;
  width: 40px;
}

.profile__hero {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 16px 16px 24px;
}

.profile__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  font-size: 26px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #4079ff 0%, #2f6bff 60%, #1d4ed8 100%);
  border-radius: 50%;
  box-shadow: 0 12px 24px rgb(47 107 255 / 25%);
}

.profile__name {
  font-size: 18px;
  font-weight: 600;
  color: var(--m-text-primary);
}

.profile__sub {
  font-size: 12px;
  color: var(--m-text-muted);
}

.profile__group {
  margin: 12px 14px;
  overflow: hidden;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border-soft);
  border-radius: 14px;
}

.profile-cell {
  display: flex;
  align-items: center;
  width: 100%;
  height: 52px;
  padding: 0 16px;
  font-size: 15px;
  color: var(--m-text-primary);
  cursor: pointer;
  background: transparent;
  border: none;
  transition: background 0.15s ease;
}

.profile-cell + .profile-cell {
  border-top: 1px solid var(--m-border-soft);
}

.profile-cell:active {
  background: var(--m-border-soft);
}

.profile-cell__label {
  flex: 1 1 auto;
  text-align: left;
}

.profile-cell__caret {
  font-size: 18px;
  color: var(--m-text-muted);
}

.profile-cell--danger {
  color: var(--m-danger);
}
</style>
