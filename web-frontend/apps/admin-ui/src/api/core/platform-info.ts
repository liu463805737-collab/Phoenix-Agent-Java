import { requestClient } from '#/api/request';

export interface PlatformInfo {
  id?: string;
  type?: string;
  name?: string;
  status?: string;
  corpid?: string;
  corpsecret?: string;
  agentid?: string;
  createTime?: string;
  updateTime?: string;
}

export async function getPlatformInfoPageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get('/platform/platform-info/page', {
    params: { page, size, ...params },
    responseReturn: 'data',
  });
}

export async function getPlatformInfoApi(id: string) {
  return requestClient.get(`/platform/platform-info/${id}`, {
    responseReturn: 'body',
  });
}

export async function createPlatformInfoApi(data: Record<string, any>) {
  return requestClient.post('/platform/platform-info', data, {
    responseReturn: 'body',
  });
}

export async function updatePlatformInfoApi(data: Record<string, any>) {
  return requestClient.put('/platform/platform-info', data, {
    responseReturn: 'body',
  });
}

export async function deletePlatformInfoApi(id: string) {
  return requestClient.delete(`/platform/platform-info/${id}`, {
    responseReturn: 'body',
  });
}
