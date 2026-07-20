import type {
  AuthTransport,
  LoginPayload,
  LoginResult,
  User,
} from '@phoenix/chat-shared';

import { http } from './http';

interface BackendLoginResult {
  token: string;
  userId: string;
  username: string;
  realName: string;
  email?: string;
  phone?: string;
  userType?: number;
}

export const realAuthTransport: AuthTransport = {
  async login(payload: LoginPayload): Promise<LoginResult> {
    const data = await http.post<BackendLoginResult>('/auth/login', {
      username: payload.username,
      password: payload.password,
    });

    return {
      token: data.token,
      user: {
        username: data.username,
        displayName: data.realName || data.username,
        realName: data.realName,
        email: data.email,
        phone: data.phone,
        loginAt: Date.now(),
      },
    };
  },

  async logout(): Promise<void> {
    try {
      await http.post('/auth/logout');
    } catch {
      // 即使服务端登出失败，客户端也清理本地状态
    }
  },

  async fetchCurrentUser(_token: string): Promise<User | null> {
    return null;
  },
};
