import { requestClient } from '#/api/request';

/** 全量同步所有部门及人员 */
export async function syncAllApi() {
  return requestClient.post('/platform/sync/all', {}, { responseReturn: 'body' });
}

/** 同步所有部门数据 */
export async function syncDepartmentsApi() {
  return requestClient.post('/platform/sync/departments', {}, { responseReturn: 'body' });
}

/** 同步所有人员数据 */
export async function syncUsersApi() {
  return requestClient.post('/platform/sync/users', {}, { responseReturn: 'body' });
}

/** 同步指定部门的下级部门 */
export async function syncSubDepartmentsApi(deptId: string) {
  return requestClient.post(`/platform/sync/depts/${deptId}`, {}, { responseReturn: 'body' });
}

/** 同步指定部门下的人员 */
export async function syncUsersByDeptApi(deptId: string) {
  return requestClient.post(`/platform/sync/depts/users/${deptId}`, {}, { responseReturn: 'body' });
}

/** 根据三方用户ID同步单个人员信息 */
export async function syncUserApi(userId: string) {
  return requestClient.post(`/platform/sync/users/${userId}`, {}, { responseReturn: 'body' });
}
