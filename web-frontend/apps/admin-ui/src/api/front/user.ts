import { requestClient } from '#/api/request';

export interface UpdatePasswordData {
  oldPassword: string;
  newPassword: string;
}

export async function updatePasswordApi(data: UpdatePasswordData) {
  return requestClient.put<void>('/auth/updatePassword', data);
}
