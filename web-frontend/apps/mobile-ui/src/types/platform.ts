export interface PlatformInfo {
  id: string;
  type: 'dingtalk' | 'weixin' | 'feishu';
  name: string;
  status: string;
  corpid: string;
  corpsecret: string;
  agentid: string;
  appKey: string;
}
