import type { RouteRecordStringComponent } from '@vben/types';

import { requestClient } from '#/api/request';

/**
 * 获取用户所有菜单
 */
export async function getAllMenusApi() {
  return requestClient.get<RouteRecordStringComponent[]>('/api/privilege/auth/menus', {responseReturn: 'raw'});
}

/** 权限值信息（模块级） */
export interface PvalueInfo {
  pvalueId: string;
  pvalueName: string;
  position: number;
  enabled: boolean;
  orderNo: number;
}

/** 模块树节点 */
export interface ModuleTreeVO {
  children: ModuleTreeVO[];
  component: string;
  id: string;
  image: string;
  isShow: number;
  name: string;
  orderNo: number;
  pid: string;
  pvalues: PvalueInfo[];
  sn: string;
  type: string;
  url: string;
  [key: string]: any;
}

/** 用户菜单及权限 */
interface UserMenuVO {
  menus: ModuleTreeVO[];
  pvalues: { id: string; name: string; position: number; orderNo: number }[];
}

/**
 * 获取当前登录用户的菜单及权限
 */
export async function getPrivilegeMenusApi() {
  return requestClient.get<UserMenuVO>('/api/privilege/auth/menus', {responseReturn: 'data'});
}
