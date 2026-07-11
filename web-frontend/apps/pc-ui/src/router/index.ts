import { createRouter, createWebHistory } from 'vue-router';

import { useAuthStore } from '@phoenix/chat-shared';
import ChatLayout from '../views/chat/ChatLayout.vue';
import LoginPage from '../views/auth/LoginPage.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: { public: true, title: '登录' },
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatLayout,
      meta: { requiresAuth: true, title: '智能体对话' },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login',
    },
  ],
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  const loggedIn = Boolean(auth.token);

  if (to.meta.requiresAuth && !loggedIn) {
    return {
      path: '/login',
      query: to.fullPath === '/login' ? undefined : { redirect: to.fullPath },
    };
  }

  if (to.name === 'login' && loggedIn) {
    return { path: '/chat' };
  }

  return true;
});

export default router;
