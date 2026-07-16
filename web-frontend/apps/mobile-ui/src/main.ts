import { createApp } from 'vue';

import {
  configureAuthStorage,
  useAgentStore,
  useAuthStore,
  useChatStore,
} from '@phoenix/chat-shared';
import { createPinia } from 'pinia';
import 'vant/es/popup/style';
import 'vant/es/toast/style';

import App from './App.vue';
import router from './router';
import { realAgentTransport } from './services/agentTransport';
import { realAuthTransport } from './services/authTransport';
import { realChatTransport } from './services/chatTransport';

import 'vant/lib/index.css';
import './styles/global.scss';

configureAuthStorage({ storageKey: 'mobile-ui' });

const app = createApp(App);

app.use(createPinia());
app.use(router);

const auth = useAuthStore();
useAuthStore().setTransport(realAuthTransport);
useAgentStore().setTransport(realAgentTransport);
useChatStore().setTransport(realChatTransport);

if (auth.token) {
  void useAgentStore().loadAll();
}

app.mount('#app');
