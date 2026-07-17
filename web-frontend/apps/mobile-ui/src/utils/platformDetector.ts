export type PlatformType = 'dingtalk' | 'weixin' | 'feishu' | 'browser';

declare const dd: any;
declare const ww: any;
declare const tt: any;

export function detectPlatform(): PlatformType {
  const ua = navigator.userAgent.toLowerCase();

  if (ua.includes('dingtalk')) {
    return 'dingtalk';
  }

  if (ua.includes('wxwork') || ua.includes('micro messenger')) {
    return 'weixin';
  }

  if (ua.includes('lark') || ua.includes('feishu')) {
    return 'feishu';
  }

  return 'browser';
}

export function isInApp(): boolean {
  return detectPlatform() !== 'browser';
}

export function getAuthCode(corpId?: string): Promise<string> {
  const platform = detectPlatform();

  switch (platform) {
    case 'dingtalk':
      return getDingTalkAuthCode(corpId);
    case 'weixin':
      return getWeComAuthCode();
    case 'feishu':
      return getFeishuAuthCode();
    default:
      return Promise.reject(new Error('当前环境不支持获取授权码'));
  }
}

function getDingTalkAuthCode(corpId?: string): Promise<string> {
  return new Promise((resolve, reject) => {
    if (typeof dd === 'undefined') {
      reject(new Error('钉钉JSAPI未加载'));
      return;
    }

    dd.ready(() => {
      dd.runtime.permission.requestAuthCode({
        corpId: corpId || '',
        onSuccess(result: { code: string }) {
          resolve(result.code);
        },
        onFail(err: any) {
          reject(new Error(err?.message || '钉钉授权失败'));
        },
      });
    });
  });
}

function getWeComAuthCode(): Promise<string> {
  const params = new URLSearchParams(window.location.search);
  const code = params.get('code');
  if (code) {
    return Promise.resolve(code);
  }

  return Promise.reject(new Error('未获取到企业微信授权码，请确保通过OAuth链接进入'));
}

function getFeishuAuthCode(): Promise<string> {
  return new Promise((resolve, reject) => {
    if (typeof tt === 'undefined') {
      reject(new Error('飞书JSAPI未加载'));
      return;
    }

    tt.login({
      success(res: { code: string }) {
        resolve(res.code);
      },
      fail(err: any) {
        reject(new Error(err?.errMsg || '飞书授权失败'));
      },
    });
  });
}
