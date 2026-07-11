import { requestClient } from '#/api/request';
import type { PageResult } from '#/api/core/privilege-types';
import type { AclPvalueItem } from '#/api/core/privilege-role';

export interface PrivilegeModule {
  id?: string;
  pid?: string;
  name?: string;
  url?: string;
  sn?: string;
  component?: string;
  image?: string;
  orderNo?: number;
  type?: string;
  status?: number;
  systemId?: number;
  isShow?: number;
  state?: string;
  pvalues?: AclPvalueItem[];
  children?: PrivilegeModule[];
}

export interface ModuleTreeItem {
  id?: string;
  name?: string;
  sn?: string;
  children?: ModuleTreeItem[];
}

export async function getModulePageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PrivilegeModule>>(
    '/api/privilege/module/page',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}

export async function getModuleTreeApi(systemId?: number) {
  return requestClient.get<PrivilegeModule[]>('/api/privilege/module/tree', {
    params: systemId === undefined ? {} : { systemId },
    responseReturn: 'body',
  });
}

export async function getModuleApi(id: string) {
  return requestClient.get<PrivilegeModule>(`/api/privilege/module/${id}`, {
    responseReturn: 'body',
  });
}

export async function getModulesBySystemApi(systemId: number) {
  return requestClient.get<PrivilegeModule[]>(
    `/api/privilege/module/system/${systemId}`,
    { responseReturn: 'body' },
  );
}

export async function getModulesByPidApi(pid: string) {
  return requestClient.get<PrivilegeModule[]>(
    `/api/privilege/module/pid/${pid}`,
    { responseReturn: 'body' },
  );
}

export async function createModuleApi(data: Record<string, any>) {
  return requestClient.post('/api/privilege/module', data, {
    responseReturn: 'body',
  });
}

export async function updateModuleApi(data: Record<string, any>) {
  return requestClient.put('/api/privilege/module', data, {
    responseReturn: 'body',
  });
}

export async function deleteModuleApi(id: string) {
  return requestClient.delete(`/api/privilege/module/${id}`, {
    responseReturn: 'body',
  });
}
