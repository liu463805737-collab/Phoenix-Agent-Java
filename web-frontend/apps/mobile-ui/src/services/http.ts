const BASE_URL = import.meta.env.VITE_GLOB_API_URL || '/api';

const TOKEN_KEY = 'mobile-ui:auth:token';
const USER_KEY = 'mobile-ui:auth:user';

interface ApiResponse<T = any> {
  code: string | number;
  data: T;
  msg: string;
}

function isUnauthorized(code: string | number): boolean {
  return code === 401 || code === '401';
}

export function handleUnauthorized(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  import('vant').then(({ showFailToast }) => {
    showFailToast('登录已过期，请重新登录');
  });
  import('../router').then(({ default: router }) => {
    router.push('/login');
  });
}

export async function request<T>(
  url: string,
  options: RequestInit = {},
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    headers['phoenix-token'] = token;
  }

  const fullUrl = `${BASE_URL}${url}`;
  console.log(`[HTTP] ${options.method || 'GET'} ${fullUrl}`);
  const res = await fetch(fullUrl, { ...options, headers });
  console.log(`[HTTP] 响应状态:`, res.status, res.statusText);

  if (res.status === 401) {
    handleUnauthorized();
    throw new Error('未授权，请先登录');
  }

  if (!res.ok) {
    console.error(`[HTTP] 请求失败:`, res.status, res.statusText);
    throw new Error(`HTTP ${res.status}: ${res.statusText}`);
  }

  const body: ApiResponse<T> = await res.json();
  console.log(`[HTTP] 响应数据:`, body);

  if (body.code === '100' || body.code === '200') {
    return body.data;
  }

  if (isUnauthorized(body.code)) {
    handleUnauthorized();
    throw new Error(body.msg || '未授权，请先登录');
  }

  console.error(`[HTTP] 业务错误:`, body.code, body.msg);
  throw new Error(body.msg || '请求失败');
}

function buildUrl(url: string, params?: Record<string, string>): string {
  if (!params) return url;
  const qs = Object.entries(params)
    .filter(([, v]) => v != null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
  return qs ? `${url}?${qs}` : url;
}

export const http = {
  get<T>(url: string, params?: Record<string, string>) {
    return request<T>(buildUrl(url, params), { method: 'GET' });
  },
  post<T>(url: string, data?: unknown) {
    return request<T>(url, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  },
  put<T>(url: string, data?: unknown) {
    return request<T>(url, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  },
  delete<T>(url: string) {
    return request<T>(url, { method: 'DELETE' });
  },
};
