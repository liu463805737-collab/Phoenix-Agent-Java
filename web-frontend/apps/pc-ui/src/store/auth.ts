import { defineStore } from 'pinia';
import { ref } from 'vue';

const TOKEN_KEY = 'pc-ui-auth-token';
const USER_KEY = 'pc-ui-auth-user';

interface StoredUser {
  username: string;
  loginAt: number;
}

function loadUser(): StoredUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as StoredUser) : null;
  } catch {
    return null;
  }
}

export const useAuthStore = defineStore('pc-ui-auth', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '');
  const user = ref<StoredUser | null>(loadUser());

  function login(username: string) {
    // 静态登录：仅在本地生成一个伪 token，后端联调时替换为接口返回
    const fakeToken = `mock-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
    const nextUser: StoredUser = { username, loginAt: Date.now() };
    token.value = fakeToken;
    user.value = nextUser;
    localStorage.setItem(TOKEN_KEY, fakeToken);
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
  }

  function logout() {
    token.value = '';
    user.value = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  function isLoggedIn() {
    return Boolean(token.value);
  }

  return { token, user, login, logout, isLoggedIn };
});
