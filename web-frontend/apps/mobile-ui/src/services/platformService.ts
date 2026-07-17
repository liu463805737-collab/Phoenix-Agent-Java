import type { PlatformInfo } from '#/types/platform';

import { http } from './http';

export function getEnabledPlatform(): Promise<null | PlatformInfo> {
  return http.get<null | PlatformInfo>('/platform/platform-info/getEnabledPlatform');
}
interface ThirdPartyLoginParams {
  platform: string;
  code: string;
}

interface ThirdPartyLoginResult {
  token: string;
  userId: string;
  username: string;
  realName: string;
  email?: string;
  phone?: string;
}

export function thirdPartyLogin(params: ThirdPartyLoginParams): Promise<ThirdPartyLoginResult> {
  return http.post<ThirdPartyLoginResult>('/auth/thirdLogin', params);
}
