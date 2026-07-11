import { requestClient } from '#/api/request';
import type { PageResult } from '#/api/core/privilege-types';

export interface PrivilegeDepartment {
  id?: string;
  pid?: string;
  parentName?: string;
  name?: string;
  code?: string;
  companyId?: string;
  orderNo?: number;
  status?: number;
  nature?: number;
  createTime?: string;
  updateTime?: string;
  children?: PrivilegeDepartment[];
}

export interface OrganizationTreeVO {
  id?: string;
  name?: string;
  children?: DepartmentTreeVO[];
}

export interface DepartmentTreeVO {
  id?: string;
  companyId?: string;
  name?: string;
  code?: string;
  note?: string;
  pid?: string;
  parentName?: string;
  orderNo?: number;
  leader?: number;
  departmentType?: number;
  status?: number;
  nature?: number;
  createTime?: string;
  children?: DepartmentTreeVO[];
}

export async function getDepartmentPageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PrivilegeDepartment>>(
    '/api/privilege/department/page',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}

export async function getDepartmentApi(id: string) {
  return requestClient.get<PrivilegeDepartment>(
    `/api/privilege/department/${id}`,
    { responseReturn: 'body' },
  );
}

export async function getDepartmentsByCompanyApi(companyId: string) {
  return requestClient.get<PrivilegeDepartment[]>(
    `/api/privilege/department/company/${companyId}`,
    { responseReturn: 'body' },
  );
}

export async function getDepartmentByCodeApi(code: string) {
  return requestClient.get<PrivilegeDepartment>(
    `/api/privilege/department/code/${code}`,
    { responseReturn: 'body' },
  );
}
export async function getDeptTreeApi(companyId?: string) {
  return requestClient.get<DepartmentTreeVO[]>(
    '/api/privilege/department/tree',
    {
      params: companyId === undefined ? {} : { companyId },
      responseReturn: 'body',
    },
  );
}
export async function getDepartmentTreeApi(companyId?: string) {
  return requestClient.get<OrganizationTreeVO[]>(
    '/api/privilege/department/orgTree',
    {
      params: companyId === undefined ? {} : { companyId },
      responseReturn: 'body',
    },
  );
}

export async function getDepartmentsByPidApi(pid: string) {
  return requestClient.get<PrivilegeDepartment[]>(
    `/api/privilege/department/pid/${pid}`,
    { responseReturn: 'body' },
  );
}

export async function createDepartmentApi(data: Record<string, any>) {
  return requestClient.post('/api/privilege/department', data, {
    responseReturn: 'body',
  });
}

export async function updateDepartmentApi(data: Record<string, any>) {
  return requestClient.put('/api/privilege/department', data, {
    responseReturn: 'body',
  });
}

export async function deleteDepartmentApi(id: string) {
  return requestClient.delete(`/api/privilege/department/${id}`, {
    responseReturn: 'body',
  });
}
