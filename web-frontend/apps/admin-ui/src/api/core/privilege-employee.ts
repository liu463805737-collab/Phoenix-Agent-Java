import { requestClient } from '#/api/request';
import type { PageResult } from '#/api/core/privilege-types';

export interface PrivilegeEmployee {
  id?: string;
  empName?: string;
  empCode?: string;
  mobile?: string;
  companyId?: string;
  companyName?: string;
  deptId?: string;
  deptName?: string;
  status?: number;
  enableFlag?: number;
  thirdUserId?: string;
  createTime?: string;
  updateTime?: string;
}

export async function getEmployeePageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PrivilegeEmployee>>(
    '/api/privilege/employee/page',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}

export async function getEmployeeApi(id: string) {
  return requestClient.get<PrivilegeEmployee>(`/api/privilege/employee/${id}`, {
    responseReturn: 'body',
  });
}

export async function getEmployeeByEmpCodeApi(empCode: string) {
  return requestClient.get<PrivilegeEmployee>(
    `/api/privilege/employee/emp-code/${empCode}`,
    { responseReturn: 'body' },
  );
}

export async function createEmployeeApi(data: Record<string, any>) {
  return requestClient.post('/api/privilege/employee', data, {
    responseReturn: 'body',
  });
}

export async function updateEmployeeApi(data: Record<string, any>) {
  return requestClient.put('/api/privilege/employee', data, {
    responseReturn: 'body',
  });
}

export async function deleteEmployeeApi(id: string) {
  return requestClient.delete(`/api/privilege/employee/${id}`, {
    responseReturn: 'body',
  });
}
