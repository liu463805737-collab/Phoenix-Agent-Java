import { requestClient } from '#/api/request';
import type { PageResult } from '#/api/core/privilege-types';

export interface PrivilegeCompany {
  id?: string;
  cname?: string;
  shortName?: string;
  code?: string;
  sort?: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

export async function getCompanyPageApi(
  params?: Record<string, any>,
) {
  return requestClient.post<PageResult<PrivilegeCompany>>(
    '/api/privilege/company/page',
    params,
    { responseReturn: 'data' },
  );
}

export async function getCompanyApi(id: number) {
  return requestClient.get<PrivilegeCompany>(`/api/privilege/company/${id}`, {
    responseReturn: 'body',
  });
}

export async function getCompanyByCodeApi(code: string) {
  return requestClient.get<PrivilegeCompany>(
    `/api/privilege/company/code/${code}`,
    { responseReturn: 'body' },
  );
}

export async function createCompanyApi(data: Record<string, any>) {
  return requestClient.post('/api/privilege/company', data, {
    responseReturn: 'body',
  });
}

export async function updateCompanyApi(data: Record<string, any>) {
  return requestClient.put('/api/privilege/company', data, {
    responseReturn: 'body',
  });
}

export async function deleteCompanyApi(id: number) {
  return requestClient.delete(`/api/privilege/company/${id}`, {
    responseReturn: 'body',
  });
}
