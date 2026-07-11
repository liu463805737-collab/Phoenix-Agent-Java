import { createApp } from 'vue';
import { createPinia } from 'pinia';
import {
  configureAuthStorage,
  useAgentStore,
  useAuthStore,
  useChatStore,
} from '@phoenix/chat-shared';

import 'vant/lib/index.css';
import './styles/global.scss';

import App from './App.vue';
import router from './router';

configureAuthStorage({ storageKey: 'mobile-ui' });

const app = createApp(App);

app.use(createPinia());
app.use(router);

const auth = useAuthStore();
if (auth.token) {
  void Promise.all([useAgentStore().loadAll(), useChatStore().loadSessions()]);
}

app.mount('#app');
