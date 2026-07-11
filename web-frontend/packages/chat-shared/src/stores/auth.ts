import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

import type { LoginPayload, User } from '../types/user';
import type { AuthTransport } from '../transport/types';

import { mockAuthTransport } from '../transport/mock';

interface AuthStoreOptions {
  /** localStorage 的 namespace 前缀，pc-ui 与 mobile-ui 各传不同值避免互踩 */
  storageKey?: string;
}

let storageKey = 'phoenix-chat-shared';

function tokenKey(): string {
  return `${storageKey}:auth:token`;
}
function userKey(): string {
  return `${storageKey}:auth:user`;
}

function safeReadUser(): User | null {
  try {
    const raw = localStorage.getItem(userKey());
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}

/** 由 app 在 createPinia 之前调用一次，配置本地存储 namespace */
export function configureAuthStorage(options: AuthStoreOptions) {
  if (options.storageKey) storageKey = options.storageKey;
}

export const useAuthStore = defineStore('phoenix-chat-shared/auth', () => {
  let transport: AuthTransport = mockAuthTransport;

  const token = ref<string>(
    typeof window === 'undefined' ? '' : localStorage.getItem(tokenKey()) ?? '',
  );
  const user = ref<User | null>(typeof window === 'undefined' ? null : safeReadUser());
  const loading = ref(false);

  const isLoggedIn = computed(() => Boolean(token.value));

  function setTransport(next: AuthTransport) {
    transport = next;
  }

  function persist() {
    if (typeof window === 'undefined') return;
    if (token.value) localStorage.setItem(tokenKey(), token.value);
    else localStorage.removeItem(tokenKey());
    if (user.value) localStorage.setItem(userKey(), JSON.stringify(user.value));
    else localStorage.removeItem(userKey());
  }

  async function login(payload: LoginPayload) {
    loading.value = true;
    try {
      const result = await transport.login(payload);
      token.value = result.token;
      user.value = result.user;
      persist();
      return result;
    } finally {
      loading.value = false;
    }
  }

  async function logout() {
    try {
      await transport.logout();
    } finally {
      token.value = '';
      user.value = null;
      persist();
    }
  }

  async function hydrate() {
    if (!token.value) return;
    const remote = await transport.fetchCurrentUser(token.value);
    if (remote) {
      user.value = remote;
      persist();
    }
  }

  return {
    token,
    user,
    loading,
    isLoggedIn,
    setTransport,
    login,
    logout,
    hydrate,
  };
});
