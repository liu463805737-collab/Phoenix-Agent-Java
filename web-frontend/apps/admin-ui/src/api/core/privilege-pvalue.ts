import { requestClient } from '#/api/request';
import type { PageResult } from '#/api/core/privilege-types';

export interface PrivilegePvalue {
  id?: string;
  systemId?: number;
  name?: string;
  position?: number;
  orderNo?: number;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export async function getPvaluePageApi(
  params?: Record<string, any>,
) {
  return requestClient.post<PageResult<PrivilegePvalue>>(
    '/privilege/pvalue/page',
    params,
    {
      responseReturn: 'data',
    },
  );
}

export async function getPvalueApi(id: string) {
  return requestClient.get<PrivilegePvalue>(`/privilege/pvalue/${id}`, {
    responseReturn: 'body',
  });
}

export async function getPvaluesBySystemApi() {
  return requestClient.get<PrivilegePvalue[]>('/privilege/pvalue/system', {
    responseReturn: 'body',
  });
}

export async function createPvalueApi(data: Record<string, any>) {
  return requestClient.post('/privilege/pvalue', data, {
    responseReturn: 'body',
  });
}

export async function updatePvalueApi(data: Record<string, any>) {
  return requestClient.put('/privilege/pvalue', data, {
    responseReturn: 'body',
  });
}

export async function deletePvalueApi(id: string) {
  return requestClient.delete(`/privilege/pvalue/${id}`, {
    responseReturn: 'body',
  });
}
