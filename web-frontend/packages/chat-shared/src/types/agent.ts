export interface Agent {
  id: string;
  sn: string;
  name: string;
  /** 一句话描述 */
  description: string;
  /** 头像字符（兜底）或图片地址 */
  avatar: string;
  /** 角标，如 "常用" / "新" */
  tag?: string;
  /** 智能体类型（如 sql 表示 SQL 智能体） */
  type?: string;
}
