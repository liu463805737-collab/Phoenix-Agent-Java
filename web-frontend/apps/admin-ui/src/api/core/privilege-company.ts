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
    '/privilege/company/page',
    params,
    { responseReturn: 'data' },
  );
}

export async function getCompanyApi(id: number) {
  return requestClient.get<PrivilegeCompany>(`/privilege/company/${id}`, {
    responseReturn: 'body',
  });
}

export async function getCompanyByCodeApi(code: string) {
  return requestClient.get<PrivilegeCompany>(
    `/privilege/company/code/${code}`,
    { responseReturn: 'body' },
  );
}

export async function createCompanyApi(data: Record<string, any>) {
  return requestClient.post('/privilege/company', data, {
    responseReturn: 'body',
  });
}

export async function updateCompanyApi(data: Record<string, any>) {
  return requestClient.put('/privilege/company', data, {
    responseReturn: 'body',
  });
}

export async function deleteCompanyApi(id: number) {
  return requestClient.delete(`/privilege/company/${id}`, {
    responseReturn: 'body',
  });
}
