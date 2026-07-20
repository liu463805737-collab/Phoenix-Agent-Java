export interface User {
  /** 用户名 / 账号 */
  username: string;
  /** 展示名，可与 username 不同 */
  displayName?: string;
  /** 真实姓名 */
  realName?: string;
  /** 手机号 */
  phone?: string;
  /** 邮箱 */
  email?: string;
  /** 头像字符（兜底）或图片地址 */
  avatar?: string;
  /** 本次登录时间戳 */
  loginAt: number;
}

export interface LoginPayload {
  username: string;
  password: string;
  remember?: boolean;
}

export interface LoginResult {
  token: string;
  user: User;
}
