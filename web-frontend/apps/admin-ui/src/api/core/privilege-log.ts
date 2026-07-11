import { requestClient } from '#/api/request';
import type { PageResult } from '#/api/core/privilege-types';

export interface PrivilegeLoginLog {
  id?: string;
  operationId?: string;
  ip?: string;
  operationUsername?: string;
  operationPerson?: string;
  operationContent?: string;
  createTime?: string;
}

export async function getLoginLogPageApi(
  page: number,
  size: number,
  params?: Record<string, any>,
) {
  return requestClient.get<PageResult<PrivilegeLoginLog>>(
    '/api/privilege/login-log/page',
    {
      params: { page, size, ...params },
      responseReturn: 'body',
    },
  );
}
