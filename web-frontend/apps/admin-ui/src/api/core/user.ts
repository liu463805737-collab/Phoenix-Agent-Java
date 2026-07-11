import type { UserInfo } from '@vben/types';

import { requestClient } from '#/api/request';

export interface AccountInfo {
  id?: string;
  username?: string;
  realName?: string;
  nickName?: string;
  phone?: string;
  email?: string;
  gender?: string;
  birthday?: string;
  avatarUrl?: string;
  status?: string;
  code?: string;
}

/**
 * 获取用户信息
 */
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/api/privilege/auth/getLoginUserInfo');
}

/**
 * 获取普通用户详细信息
 */
export async function getAccountInfoApi(id: string) {
  return requestClient.get<AccountInfo>(`/api/platform/account-info/${id}`);
}
