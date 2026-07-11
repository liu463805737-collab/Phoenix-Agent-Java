<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';

import { useAuthStore } from '../../store/auth';
import BrandPanel from './components/BrandPanel.vue';
import LoginForm from './components/LoginForm.vue';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

async function handleSubmit(payload: { username: string; remember: boolean }) {
  auth.login(payload.username);
  const redirect =
    typeof route.query.redirect === 'string' ? route.query.redirect : '/chat';
  await router.replace(redirect || '/chat');
}
</script>

<template>
  <div class="login-page">
    <div class="login-page__aurora" aria-hidden="true">
      <span class="aurora aurora--1" />
      <span class="aurora aurora--2" />
      <span class="aurora aurora--3" />
      <span class="grid-overlay" />
    </div>

    <div class="login-shell">
      <BrandPanel />
      <LoginForm @submit="handleSubmit" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 32px;
  overflow: hidden;
  color: #e6ecff;
  background: radial-gradient(
    circle at 18% 12%,
    #1d2c5b 0%,
    #0c1230 55%,
    #060a22 100%
  );
  isolation: isolate;

  &__aurora {
    position: absolute;
    inset: 0;
    z-index: -1;
    pointer-events: none;

    .aurora {
      position: absolute;
      border-radius: 50%;
      mix-blend-mode: screen;
      opacity: 0.7;
      filter: blur(90px);
      animation: aurora-float 18s ease-in-out infinite;
    }

    .aurora--1 {
      top: -120px;
      left: -80px;
      width: 480px;
      height: 480px;
      background: radial-gradient(circle, #2f6bff 0%, transparent 65%);
    }

    .aurora--2 {
      top: 30%;
      right: -120px;
      width: 520px;
      height: 520px;
      background: radial-gradient(circle, #7c5cff 0%, transparent 65%);
      animation-delay: -6s;
    }

    .aurora--3 {
      bottom: -140px;
      left: 30%;
      width: 560px;
      height: 560px;
      background: radial-gradient(circle, #36d6ff 0%, transparent 65%);
      opacity: 0.55;
      animation-delay: -12s;
    }

    .grid-overlay {
      position: absolute;
      inset: 0;
      background-image:
        linear-gradient(rgb(255 255 255 / 4%) 1px, transparent 1px),
        linear-gradient(90deg, rgb(255 255 255 / 4%) 1px, transparent 1px);
      background-size: 56px 56px;
      mask-image: radial-gradient(circle at 50% 50%, #000 0%, transparent 75%);
    }
  }
}

@keyframes aurora-float {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(40px, -30px, 0) scale(1.08);
  }
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(420px, 0.95fr);
  width: min(1080px, 100%);
  min-height: 620px;
  overflow: hidden;
  background: rgb(255 255 255 / 6%);
  border: 1px solid rgb(255 255 255 / 12%);
  border-radius: 24px;
  box-shadow:
    0 30px 80px rgb(6 10 34 / 55%),
    inset 0 0 0 1px rgb(255 255 255 / 4%);
  backdrop-filter: blur(22px);
}

@media (max-width: 960px) {
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }
}
</style>
