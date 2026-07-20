import { createRouter, createWebHistory } from 'vue-router';

import { useAuthStore } from '@phoenix/chat-shared';

import LoginPage from '../views/auth/LoginPage.vue';
import ChatPage from '../views/chat/ChatPage.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/chat' },
    {
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: { public: true, title: '登录' },
    },
    {
      path: '/chat',
      name: 'chat',
      component: ChatPage,
      meta: { requiresAuth: true, title: '智能体对话' },
    },
    {
      path: '/me',
      name: 'me',
      component: () => import('../views/me/ProfilePage.vue'),
      meta: { requiresAuth: true, title: '我的' },
    },
    {
      path: '/me/setting',
      name: 'me-setting',
      component: () => import('../views/me/UserSetting.vue'),
      meta: { requiresAuth: true, title: '个人设置' },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/about/AboutPage.vue'),
      meta: { requiresAuth: true, title: '关于我们' },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/chat',
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
