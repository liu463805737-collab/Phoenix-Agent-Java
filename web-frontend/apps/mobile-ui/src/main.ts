import type { User } from '@phoenix/chat-shared';

import { createApp } from 'vue';

import {
  configureAuthStorage,
  useAgentStore,
  useAuthStore,
  useChatStore,
} from '@phoenix/chat-shared';
import { createPinia } from 'pinia';

import App from './App.vue';
import router from './router';
import { realAgentTransport } from './services/agentTransport';
import { realAuthTransport } from './services/authTransport';
import { realChatTransport } from './services/chatTransport';
import { getEnabledPlatform, thirdPartyLogin } from './services/platformService';
import { detectPlatform, getAuthCode } from './utils/platformDetector';

import 'vant/es/popup/style';
import 'vant/es/toast/style';

import 'vant/lib/index.css';
import './styles/global.scss';

configureAuthStorage({ storageKey: 'mobile-ui' });

const TOKEN_KEY = 'mobile-ui:auth:token';
const USER_KEY = 'mobile-ui:auth:user';

const app = createApp(App);

app.use(createPinia());
app.use(router);

const auth = useAuthStore();
useAuthStore().setTransport(realAuthTransport);
useAgentStore().setTransport(realAgentTransport);
useChatStore().setTransport(realChatTransport);

async function trySsoLogin(): Promise<boolean> {
  const platform = detectPlatform();
  if (platform === 'browser') return false;

  try {
    const enabled = await getEnabledPlatform();
    if (!enabled || enabled.status !== '1' || enabled.type !== platform) return false;

    const code = await getAuthCode(enabled.corpid);
    const result = await thirdPartyLogin({ platform, code });

    const user: User = {
      username: result.username,
      displayName: result.realName || result.username,
      realName: result.realName,
      email: result.email,
      phone: result.phone,
      loginAt: Date.now(),
    };

    auth.token = result.token;
    auth.user = user;

    localStorage.setItem(TOKEN_KEY, result.token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));

    return true;
  } catch {
    return false;
  }
}

async function bootstrap() {
  if (!auth.token) {
    const ssoOk = await trySsoLogin();
    if (ssoOk) {
      await useAgentStore().loadAll();
    }
  } else {
    void useAgentStore().loadAll();
  }

  app.mount('#app');
}

bootstrap();
