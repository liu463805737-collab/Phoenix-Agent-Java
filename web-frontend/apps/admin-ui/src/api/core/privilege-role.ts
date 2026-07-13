import { requestClient } from '#/api/request';
import type { PageResult } from '#/api/core/privilege-types';

export interface PrivilegeRole {
  id?: string;
  name?: string;
  sn?: string;
  companyId?: string;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export interface AclPvalueItem {
  pvalueId: string;
  pvalueName: string;
  name?: string;
  position: number;
  enabled: boolean;
}

export interface AclTreeNode {
  id: string;
  pid: string | null;
  name: string;
  url: string;
  sn: string;
  state: string;
  component: string | null;
  systemId: number;
  status: number;
  image: string | null;
  orderNo: number;
  isShow: number;
  categoryId: number | null;
  type: string;
  children: AclTreeNode[];
  pvalues: AclPvalueItem[];
}

export async function getRolePageApi(
  params?: Record<string, any>,
) {
  return requestClient.post<PageResult<PrivilegeRole>>(
    '/api/privilege/role/page',
      params,
    {
      responseReturn: 'data',
    },
  );
}

export async function getRoleApi(id: string) {
  return requestClient.get<PrivilegeRole>(`/api/privilege/role/${id}`, {
    responseReturn: 'body',
  });
}

export async function getRolesByCompanyApi(companyId: number) {
  return requestClient.get<PrivilegeRole[]>(
    `/api/privilege/role/company/${companyId}`,
    { responseReturn: 'body' },
  );
}

export async function createRoleApi(data: Record<string, any>) {
  return requestClient.post('/api/privilege/role', data, {
    responseReturn: 'body',
  });
}

export async function updateRoleApi(data: Record<string, any>) {
  return requestClient.put('/api/privilege/role', data, {
    responseReturn: 'body',
  });
}

export async function deleteRoleApi(id: string) {
  return requestClient.delete(`/api/privilege/role/${id}`, {
    responseReturn: 'body',
  });
}

export interface AclRecord {
  id?: string;
  releaseId?: string;
  releaseSn?: string;
  systemSn?: string;
  moduleId?: string;
  moduleSn?: string;
  aclState?: string;
}

export async function getRoleAclsApi(roleId: string) {
  return requestClient.get<AclTreeNode[]>(
    `/api/privilege/module/tree/acl`,
    {
      params: { roleId },
      responseReturn: 'body',
    },
  );
}

export async function getAclsByReleaseIdApi(releaseId: string) {
  return requestClient.get<AclRecord[]>(
    `/api/privilege/acl/release/${releaseId}`,
    { responseReturn: 'body' },
  );
}

export async function saveAclApi(data: Record<string, any>) {
  return requestClient.post('/api/privilege/acl', data, {
    responseReturn: 'body',
  });
}

export async function updateAclApi(data: Record<string, any>) {
  return requestClient.put('/api/privilege/acl', data, {
    responseReturn: 'body',
  });
}

export async function deleteAclApi(id: string) {
  return requestClient.delete(`/api/privilege/acl/${id}`, {
    responseReturn: 'body',
  });
}

export async function saveAllAclApi(
  releaseId: string,
  checkStatus?: boolean,
) {
  const url = checkStatus !== undefined
    ? `/api/privilege/acl/saveAll/${releaseId}/${checkStatus}`
    : `/api/privilege/acl/saveAll/${releaseId}`;
  return requestClient.post(url, undefined, {
    responseReturn: 'body',
  });
}

export interface SaveModuleAclRequest {
  releaseId: string;
  releaseSn: string;
  systemSn?: string;
  moduleId: string;
  moduleSn: string;
  aclState?: number;
  status: 'check' | 'uncheck';
}

export async function saveModuleAclApi(data: SaveModuleAclRequest) {
  return requestClient.post('/api/privilege/acl/saveModule', data, {
    responseReturn: 'body',
  });
}
