import { createApp } from 'vue';
import { createPinia } from 'pinia';
import {
  configureAuthStorage,
  useAgentStore,
  useAuthStore,
  useChatStore,
} from '@phoenix/chat-shared';

import App from './App.vue';
import router from './router';

// 与 mobile-ui 隔离的 localStorage namespace
configureAuthStorage({ storageKey: 'pc-ui' });

const app = createApp(App);

app.use(createPinia());
app.use(router);

// 预拉一次智能体 + 会话，让登录后首屏可见（未登录时由路由守卫拦截到 /login）
const auth = useAuthStore();
if (auth.token) {
  void Promise.all([useAgentStore().loadAll(), useChatStore().loadSessions()]);
}

app.mount('#app');
