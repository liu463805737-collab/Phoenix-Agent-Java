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

const auth = useAuthStore();
useAuthStore().setTransport(realAuthTransport);
useAgentStore().setTransport(realAgentTransport);
useChatStore().setTransport(realChatTransport);

async function trySsoLogin(): Promise<boolean> {
  const platform = detectPlatform();
  console.log('[bootstrap] detectPlatform:', platform);
  if (platform === 'browser') {
    console.log('[bootstrap] 浏览器环境，跳过 SSO 登录');
    return false;
  }

  try {
    console.log('[bootstrap] 开始查询平台信息...');
    const enabled = await getEnabledPlatform();
    console.log('[bootstrap] getEnabledPlatform 返回:', enabled);
    if (!enabled || enabled.status !== '1' || enabled.type !== platform) {
      console.log('[bootstrap] 平台未启用或不匹配，跳过 SSO');
      return false;
    }

    console.log('[bootstrap] 开始获取授权码...');
    const code = await getAuthCode(enabled.corpid);
    console.log('[bootstrap] 获取授权码成功');

    const result = await thirdPartyLogin({ platform, code });
    console.log('[bootstrap] thirdPartyLogin 成功:', result.username);

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
  } catch (e) {
    console.error('[bootstrap] SSO 登录失败:', e);
    return false;
  }
}

async function bootstrap() {
  console.log('[bootstrap] 启动，token:', auth.token ? '存在' : '为空');
  if (!auth.token) {
    console.log('[bootstrap] 无 token，尝试 SSO 登录...');
    const ssoOk = await trySsoLogin();
    console.log('[bootstrap] SSO 登录结果:', ssoOk);
    if (ssoOk) {
      console.log('[bootstrap] SSO 成功，加载智能体列表...');
      await useAgentStore().loadAll();
    }
  } else {
    console.log('[bootstrap] 已有 token，直接加载智能体列表...');
    void useAgentStore().loadAll();
  }

  console.log('[bootstrap] 安装路由');
  app.use(router);

  console.log('[bootstrap] 挂载 Vue 应用');
  app.mount('#app');
}

bootstrap();
