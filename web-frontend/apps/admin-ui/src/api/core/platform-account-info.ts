import type { PageResult } from '#/api/core/privilege-types';

import { requestClient } from '#/api/request';

export interface UserGroupVO {
  groupId?: string;
  groupName?: string;
}

export interface PlatformAccountInfo {
  id?: string;
  code?: string;
  username?: string;
  password?: string;
  realName?: string;
  nickName?: string;
  birthday?: string;
  email?: string;
  phone?: string;
  avatarUrl?: string;
  gender?: string;
  status?: string;
  deptId?: string;
  deptName?: string;
  thirdPartyId?: string;
  groups?: UserGroupVO[];
  createTime?: string;
  create?: string;
  updateTime?: string;
  update?: string;
}

export async function getAccountInfoPageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PlatformAccountInfo>>(
    '/platform/account-info/page',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}
export async function getUnGroupPageByGroupId(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PlatformAccountInfo>>(
    '/platform/account-info/getUnGroupPageByGroupId',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}

export async function getAccountInfoApi(id: string) {
  return requestClient.get<PlatformAccountInfo>(
    `/platform/account-info/${id}`,
    { responseReturn: 'body' },
  );
}

export async function getAccountInfoByUsernameApi(username: string) {
  return requestClient.get<PlatformAccountInfo>(
    `/platform/account-info/username/${username}`,
    { responseReturn: 'body' },
  );
}

export async function getAccountInfoByCodeApi(code: string) {
  return requestClient.get<PlatformAccountInfo>(
    `/platform/account-info/code/${code}`,
    { responseReturn: 'body' },
  );
}

export async function createAccountInfoApi(data: Record<string, any>) {
  return requestClient.post('/platform/account-info', data, {
    responseReturn: 'body',
  });
}

export async function updateAccountInfoApi(data: Record<string, any>) {
  return requestClient.put('/platform/account-info', data, {
    responseReturn: 'body',
  });
}

export async function deleteAccountInfoApi(id: string) {
  return requestClient.delete(`/platform/account-info/${id}`, {
    responseReturn: 'body',
  });
}
