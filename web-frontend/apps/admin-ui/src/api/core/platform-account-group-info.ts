import type { PageResult } from '#/api/core/privilege-types';

import { requestClient } from '#/api/request';

export interface PlatformAccountGroupInfo {
  id?: string;
  groupId?: string;
  accountId?: string;
  groupName?: string;
  accountName?: string;
  createTime?: string;
  creator?: string;
  updateTime?: string;
  update?: string;
}

export async function getAccountGroupInfoPageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PlatformAccountGroupInfo>>(
    '/platform/account-group-info/page',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}

export async function getAccountGroupInfoApi(id: string) {
  return requestClient.get<PlatformAccountGroupInfo>(
    `/platform/account-group-info/${id}`,
    { responseReturn: 'body' },
  );
}

export async function getAccountGroupInfoByGroupApi(groupId: string) {
  return requestClient.get<PlatformAccountGroupInfo[]>(
    `/platform/account-group-info/group/${groupId}`,
    { responseReturn: 'body' },
  );
}
// 查询用户的组
export async function getAccountGroupInfoByAccountApi(accountId: string) {
  return requestClient.get<PlatformAccountGroupInfo[]>(
    `/platform/account-group-info/account/${accountId}`,
    { responseReturn: 'body' },
  );
}

export async function createAccountGroupInfoApi(data: Record<string, any>) {
  return requestClient.post('/platform/account-group-info', data, {
    responseReturn: 'body',
  });
}

export async function updateAccountGroupInfoApi(data: Record<string, any>) {
  return requestClient.put('/platform/account-group-info', data, {
    responseReturn: 'body',
  });
}

export async function deleteAccountGroupInfoApi(id: string) {
  return requestClient.delete(`/platform/account-group-info/${id}`, {
    responseReturn: 'body',
  });
}

export async function deleteAccountGroupInfoByGroupAndAccountApi(
  groupId: string,
  accountId: string,
) {
  return requestClient.delete(
    `/platform/account-group-info/group/${groupId}/account/${accountId}`,
    { responseReturn: 'body' },
  );
}
