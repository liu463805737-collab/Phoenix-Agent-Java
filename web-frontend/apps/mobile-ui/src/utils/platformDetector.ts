import * as dd from 'dingtalk-jsapi';
import { getDingTalkJsApiConfig } from '#/services/platformService';
declare const tt: any;

export type PlatformType = 'dingtalk' | 'weixin' | 'feishu' | 'browser';

export function detectPlatform(): PlatformType {
  const ua = navigator.userAgent.toLowerCase();
  if (ua.includes('dingtalk')) return 'dingtalk';
  if (ua.includes('wxwork') || ua.includes('micro messenger')) return 'weixin';
  if (ua.includes('lark') || ua.includes('feishu')) return 'feishu';
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

function getAuthCodeFromUrl(): string | null {
  try {
    const sp = new URLSearchParams(window.location.search);
    const c = sp.get('authCode') || sp.get('code');
    if (c) return c;
    const hash = window.location.hash;
    if (hash?.includes('authCode=')) {
      const m = hash.match(/[?&]authCode=([^&]+)/);
      if (m?.[1]) return decodeURIComponent(m[1]);
    }
  } catch { /* ignore */ }
  return null;
}

function waitForDdReady(timeoutMs = 5000): Promise<void> {
  return new Promise((resolve) => {
    const timer = setTimeout(() => {
      console.warn('[钉钉] dd.ready 超时，强制继续');
      resolve();
    }, timeoutMs);
    try {
      dd.ready(() => {
        clearTimeout(timer);
        resolve();
      });
    } catch (e) {
      clearTimeout(timer);
      console.error('[钉钉] dd.ready 调用异常:', e);
      resolve();
    }
  });
}

async function getDingTalkAuthCode(corpId?: string): Promise<string> {
  const fromUrl = getAuthCodeFromUrl();
  if (fromUrl) {
    console.log('[钉钉] 从 URL 获取到授权码');
    return fromUrl;
  }

  // 1. 获取签名配置
  const currentUrl = location.href.split('#')[0] ?? location.href;
  console.log('[钉钉] 获取 JSAPI 配置，URL:', currentUrl, 'corpId:', corpId);
  try {
    const config = await getDingTalkJsApiConfig(currentUrl, corpId);
    console.log('[钉钉] JSAPI 配置:', config);

    // 2. 调用 dd.config（使用后端返回的参数，corpId 优先使用从后端平台信息获取的值）
    dd.config({
      agentId: config.agentId,
      corpId: corpId || config.corpId,
      timeStamp: config.timeStamp,
      nonceStr: config.nonceStr,
      signature: config.signature,
      jsApiList: ['runtime.permission.requestAuthCode'],
    });
    console.log('[钉钉] dd.config 完成');
  } catch (e) {
    console.warn('[钉钉] 获取 JSAPI 配置失败，尝试无配置调用:', e);
  }

  // 3. 等待 dd.ready
  console.log('[钉钉] 等待 dd.ready...');
  const readyStart = Date.now();
  await waitForDdReady();
  console.log('[钉钉] dd.ready 完成，耗时', Date.now() - readyStart, 'ms');

  // 4. 调用 requestAuthCode
  const params: any = {};
  if (corpId) params.corpId = corpId;

  try {
    const result = await dd.runtime.permission.requestAuthCode(params);
    if (result?.code) return result.code;
  } catch (err) {
    console.error('[钉钉] requestAuthCode 失败:', err);
  }

  // 5. 重试一次（不带 corpId）
  if (corpId) {
    try {
      const result = await (dd.runtime.permission.requestAuthCode as any)({});
      if (result?.code) return result.code;
    } catch (err) {
      console.error('[钉钉] requestAuthCode 重试失败:', err);
    }
  }

  throw new Error('钉钉授权失败');
}

function getWeComAuthCode(): Promise<string> {
  const params = new URLSearchParams(window.location.search);
  const code = params.get('code');
  if (code) return Promise.resolve(code);
  return Promise.reject(new Error('未获取到企业微信授权码，请确保通过OAuth链接进入'));
}

function getFeishuAuthCode(): Promise<string> {
  return new Promise((resolve, reject) => {
    if (typeof tt === 'undefined') {
      reject(new Error('飞书JSAPI未加载'));
      return;
    }
    tt.login({
      success(res: { code: string }) { resolve(res.code); },
      fail(err: any) { reject(new Error(err?.errMsg || '飞书授权失败')); },
    });
  });
}
