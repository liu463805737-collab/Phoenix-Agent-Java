import type { BasicUserInfo } from '@vben-core/typings';

/** 用户信息 */
interface UserInfo extends BasicUserInfo {
  /**
   * 用户描述
   */
  desc: string;
  /**
   * 首页地址
   */
  homePath: string;

  /**
   * accessToken
   */
  token: string;

  /**
   * 邮箱
   */
  email?: string;

  /**
   * 手机号
   */
  phone?: string;

  /**
   * 用户类型（1 = 前端用户跳转 chat 页面）
   */
  userType?: number;
}

export type { UserInfo };
