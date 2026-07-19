import type { PlatformInfo } from '#/types/platform';

import { http } from './http';

export function getEnabledPlatform(): Promise<null | PlatformInfo> {
  return http.get<null | PlatformInfo>('/platform/platform-info/getEnabledPlatform');
}

export interface DingTalkJsApiConfig {
  agentId: string;
  corpId: string;
  timeStamp: string;
  nonceStr: string;
  signature: string;
}

export function getDingTalkJsApiConfig(url: string, corpId?: string): Promise<DingTalkJsApiConfig> {
  const params: Record<string, string> = { url };
  if (corpId) params.corpId = corpId;
  return http.get<DingTalkJsApiConfig>('/platform/platform-info/getDingTalkJsApiConfig', params);
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
