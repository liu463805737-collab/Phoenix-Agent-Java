import type { Recordable, UserInfo } from '@vben/types';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { ElNotification } from 'element-plus';
import { defineStore } from 'pinia';

import { loginApi, logoutApi, userLoginApi } from '#/api';
import { $t } from '#/locales';

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();

  const loginLoading = ref(false);

  /**
   * 异步处理登录操作
   * Asynchronously handle the login process
   * @param params 登录表单数据
   */
  async function authLogin(
    params: Recordable<any>,
    onSuccess?: () => Promise<void> | void,
  ) {
    let userInfo: null | UserInfo = null;
    try {
      loginLoading.value = true;
      const isUser = params.roleType === 'user';
      const result = isUser
        ? await userLoginApi({ username: params.username, password: params.password })
        : await loginApi(params);
      const { token, userId, username, realName, email, phone, userType } = result;

      if (token) {
        localStorage.setItem('phoenix-token', token);
        accessStore.setAccessToken(token);

        const homePath = isUser || userType === 1 ? '/front/chat' : '/agent/list';
        userInfo = {
          avatar: '',
          desc: '',
          homePath,
          realName,
          token,
          userId,
          username,
          email,
          phone,
          userType,
        };

        userStore.setUserInfo(userInfo);

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else {
          onSuccess
            ? await onSuccess?.()
            : await router.push(homePath);
        }

        if (userInfo?.realName) {
          ElNotification({
            message: `${$t('authentication.loginSuccessDesc')}:${userInfo?.realName}`,
            title: $t('authentication.loginSuccess'),
            type: 'success',
          });
        }
      }
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  async function logout(redirect: boolean = true) {
    try {
      await logoutApi();
    } catch {
      // 不做任何处理
    }
    try {
      localStorage.removeItem('phoenix-token');
    } catch {}
    try {
      resetAllStores();
    } catch (error) {
      console.error('resetAllStores error:', error);
    }
    try {
      accessStore.setLoginExpired(false);
    } catch {}

    // 回登录页带上当前路由地址
    const redirectQuery = redirect
      ? `?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
      : '';
    window.location.href = LOGIN_PATH + redirectQuery;
  }

  async function fetchUserInfo() {
    const { getUserInfoApi } = await import('#/api');
    const userInfo = await getUserInfoApi();
    userStore.setUserInfo(userInfo);
    return userInfo;
  }

  function $reset() {
    loginLoading.value = false;
  }

  return {
    $reset,
    authLogin,
    fetchUserInfo,
    loginLoading,
    logout,
  };
});
