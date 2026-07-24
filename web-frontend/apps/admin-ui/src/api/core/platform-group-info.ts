import type { PageResult } from '#/api/core/privilege-types';

import { requestClient } from '#/api/request';

export interface GroupInfo {
  id?: string;
  sn?: string;
  name?: string;
  description?: string;
  status?: number;
  createTime?: string;
  creator?: string;
  updateTime?: string;
  update?: string;
}

export async function getGroupInfoPageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<GroupInfo>>('/platform/group-info/page', {
    params: { page, size, ...params },
    responseReturn: 'body',
  });
}

export async function getGroupInfoApi(id: string) {
  return requestClient.get<GroupInfo>(`/platform/group-info/${id}`, {
    responseReturn: 'body',
  });
}

export async function createGroupInfoApi(data: Record<string, any>) {
  return requestClient.post('/platform/group-info', data, {
    responseReturn: 'body',
  });
}

export async function updateGroupInfoApi(data: Record<string, any>) {
  return requestClient.put('/platform/group-info', data, {
    responseReturn: 'body',
  });
}

export async function deleteGroupInfoApi(id: string) {
  return requestClient.delete(`/platform/group-info/${id}`, {
    responseReturn: 'body',
  });
}

export async function toggleStatusGroupInfoApi(id: string) {
  return requestClient.put(
    `/platform/group-info/${id}/toggle-status`,
    {},
    { responseReturn: 'body' },
  );
}
