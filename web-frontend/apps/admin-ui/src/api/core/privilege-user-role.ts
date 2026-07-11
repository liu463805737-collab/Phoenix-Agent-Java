import { requestClient } from '#/api/request';

export interface UserRoleBatchDTO {
  roleId: string;
  userIds: string[];
}

export interface PrivilegeUserRoleVO {
  id?: string;
  roleId?: string;
  userId?: string;
  roleName?: string;
}

export async function saveUserRoleBatchApi(data: UserRoleBatchDTO) {
  return requestClient.post('/api/privilege/user-role/batch-save', data, {
    responseReturn: 'body',
  });
}

export async function removeUserRoleBatchApi(data: UserRoleBatchDTO) {
  return requestClient.delete('/api/privilege/user-role/batch-remove', {
    data,
    responseReturn: 'body',
  });
}

export async function getUserRolesByUserIdApi(userId: string) {
  return requestClient.get<PrivilegeUserRoleVO[]>(
    `/api/privilege/user-role/user/${userId}`,
    { responseReturn: 'body' },
  );
}
