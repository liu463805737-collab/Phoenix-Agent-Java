import type { PageResult } from '#/api/core/privilege-types';

import { requestClient } from '#/api/request';

import type { AccountInfo } from './user';
import type {Agent} from "#/api";

export interface PlatformAccountTenantInfo {
  id?: string;
  accountId?: string;
  tenantId?: string;
  createTime?: string;
  creator?: string;
  updateTime?: string;
  update?: string;
}

export interface GroupAgentInfo {
  id?: string;
  name?: string;
  category?: string;
  description?: string;
}

export async function getAccountTenantInfoPageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PlatformAccountTenantInfo>>(
    '/platform/account-tenant-info/page',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}

export async function getAccountTenantInfoApi(id: string) {
  return requestClient.get<PlatformAccountTenantInfo>(
    `/platform/account-tenant-info/${id}`,
    { responseReturn: 'body' },
  );
}

export async function getTenantInfoByAccountIdApi(accountId: string) {
  return requestClient.get(
    `/platform/account-tenant-info/account/${accountId}`,
    { responseReturn: 'body' },
  );
}

export async function createAccountTenantInfoApi(data: Record<string, any>) {
  return requestClient.post('/platform/account-tenant-info', data, {
    responseReturn: 'body',
  });
}

export async function updateAccountTenantInfoApi(data: Record<string, any>) {
  return requestClient.put('/platform/account-tenant-info', data, {
    responseReturn: 'body',
  });
}

export async function deleteAccountTenantInfoApi(id: string) {
  return requestClient.delete(`/platform/account-tenant-info/${id}`, {
    responseReturn: 'body',
  });
}

export async function getAccountInfoListApi() {
  return requestClient.get<AccountInfo[]>('/platform/account-info/list', {
    responseReturn: 'body',
  });
}

export async function getGroupAgentInfoListApi(params?: { status?: string; keyword?: string }) {
  return requestClient.get<Agent[]>('/api/agent/list', {
    params,
    responseReturn: 'body',
  });
}
