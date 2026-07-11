import { useAuthStore } from '../stores/auth';
import type { LoginPayload } from '../types/user';

/**
 * 登录流程编排：收口表单提交 → 写入 auth store → 由 UI 自行做跳转。
 * 不耦合路由：mobile-ui 和 pc-ui 路由器实例不同，跳转留给 view 层。
 */
export function useLoginFlow() {
  const auth = useAuthStore();

  async function submit(payload: LoginPayload) {
    return auth.login(payload);
  }

  return {
    auth,
    submit,
  };
}
