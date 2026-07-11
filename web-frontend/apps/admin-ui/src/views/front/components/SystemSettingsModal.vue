<script setup lang="ts">
import type { VbenFormSchema } from '#/adapter/form';
import type { ThemeModeType } from '@vben/types';
import { computed, reactive, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { preferences, updatePreferences } from '@vben/preferences';
import { useVbenModal } from '@vben/common-ui';
import { useUserStore } from '@vben/stores';
import { ElMessage, ElButton } from 'element-plus';
import { useVbenForm, z } from '#/adapter/form';
import { useAuthStore } from '#/store';
import { updatePasswordApi } from '#/api/front/user';

defineOptions({ name: 'SystemSettingsModal' });

const [Modal, modalApi] = useVbenModal();

const activeTab = ref('general');
const currentView = ref<'settings' | 'changePassword'>('settings');

const { userInfo } = storeToRefs(useUserStore());

const modalTitle = computed(() => {
  return currentView.value === 'changePassword' ? '修改密码' : '系统设置';
});

const passwordSubmitting = ref(false);

const formSchema = computed((): VbenFormSchema[] => [
  {
    fieldName: 'oldPassword',
    label: '原密码',
    component: 'VbenInputPassword',
    componentProps: {
      placeholder: '请输入原密码',
    },
  },
  {
    fieldName: 'newPassword',
    label: '新密码',
    component: 'VbenInputPassword',
    componentProps: {
      passwordStrength: true,
      placeholder: '请输入新密码（至少6位）',
    },
  },
  {
    fieldName: 'confirmPassword',
    label: '确认密码',
    component: 'VbenInputPassword',
    componentProps: {
      passwordStrength: true,
      placeholder: '请再次输入新密码',
    },
    dependencies: {
      rules(values) {
        const { newPassword } = values;
        return z
          .string({ required_error: '请再次输入新密码' })
          .min(1, { message: '请再次输入新密码' })
          .refine((value) => value === newPassword, {
            message: '两次输入的密码不一致',
          });
      },
      triggerFields: ['newPassword'],
    },
  },
]);

const [Form, formApi] = useVbenForm(
  reactive({
    schema: formSchema,
    showDefaultActions: false,
    layout: 'horizontal',
    commonConfig: {
      labelWidth: 80,
      componentProps: {
        class: 'w-full',
      },
    },
  }),
);

const accountFields = [
  { label: '用户名', key: 'username' },
  { label: '真实姓名', key: 'realName' },
  { label: '邮箱', key: 'email' },
  { label: '手机号', key: 'phone' },
];

const agreementItems = [
  { label: '服务条款', path: '/front/terms-of-service' },
  { label: '隐私协议', path: '/front/privacy-agreement' },
];

function navigateTo(path: string) {
  window.open(path, '_blank');
}

function goToChangePassword() {
  formApi.resetForm();
  formApi.resetValidate();
  currentView.value = 'changePassword';
}

function goBackToSettings() {
  currentView.value = 'settings';
}

async function handleSubmitPassword() {
  const { valid } = await formApi.validate();
  if (!valid) return;
  const values = await formApi.getValues();
  passwordSubmitting.value = true;
  try {
    await updatePasswordApi({
      oldPassword: values.oldPassword,
      newPassword: values.newPassword,
    });
    modalApi.close();
    ElMessage.success({
      message: '密码修改成功',
      duration: 1500,
      onClose: () => {
        useAuthStore().logout(true);
      },
    });
  } catch {
    // Error message is already handled by the interceptor
  } finally {
    passwordSubmitting.value = false;
  }
}

const tabs = [
  { key: 'general', label: '通用设置' },
  { key: 'account', label: '账号管理' },
  { key: 'agreement', label: '服务协议' },
];

const currentThemeMode = computed(() => preferences.theme.mode);

const themeOptions: Array<{
  icon: string;
  name: ThemeModeType;
  label: string;
}> = [
  {
    icon: 'sun',
    name: 'light',
    label: '浅色',
  },
  {
    icon: 'moon',
    name: 'dark',
    label: '深色',
  },
  {
    icon: 'auto',
    name: 'auto',
    label: '跟随系统',
  },
];

function open() {
  currentView.value = 'settings';
  activeTab.value = 'general';
  modalApi.open();
}

function handleThemeChange(mode: ThemeModeType) {
  updatePreferences({ theme: { mode } });
}

defineExpose({ open, modalApi });
</script>

<template>
  <Modal
    :title="modalTitle"
    :overlay-blur="6"
    :footer="false"
    :closable="true"
    :close-on-click-modal="true"
    :fullscreen-button="false"
    centered
    class="w-[680px]"
  >
    <template #title>
      <div class="flex items-center">
        <span
          v-if="currentView === 'changePassword'"
          class="modal-title__back"
          @click="goBackToSettings"
        >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
      </span>
        {{ modalTitle }}
      </div>
    </template>

    <!-- Settings layout -->
    <div v-if="currentView === 'settings'" class="settings-layout">
      <aside class="settings-layout__sidebar">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          class="settings-tab"
          :class="{ 'is-active': activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </aside>

      <section class="settings-layout__content">
        <div v-show="activeTab === 'general'" class="settings-section">
          <h3 class="settings-section__title">主题切换</h3>
          <p class="settings-section__desc">
            选择您偏好的显示模式，或让系统自动跟随您的系统设置。
          </p>
          <div class="theme-options">
            <div
              v-for="opt in themeOptions"
              :key="opt.name"
              class="theme-option"
              :class="{ 'is-active': currentThemeMode === opt.name }"
              role="button"
              tabindex="0"
              @click="handleThemeChange(opt.name)"
              @keydown.enter="handleThemeChange(opt.name)"
            >
              <!-- Sun (light) -->
              <svg
                v-if="opt.icon === 'sun'"
                class="theme-option__icon"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <circle cx="12" cy="12" r="4" />
                <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41" />
              </svg>
              <!-- Moon (dark) -->
              <svg
                v-else-if="opt.icon === 'moon'"
                class="theme-option__icon"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
              <!-- Sun + Moon (auto) -->
              <svg
                v-else
                class="theme-option__icon"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <circle cx="12" cy="12" r="4" />
                <path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41" />
                <path d="M21 12.79A9 9 0 1 1 11.21 3" opacity="0.5" />
              </svg>
              <span class="theme-option__label">{{ opt.label }}</span>
            </div>
          </div>
        </div>

        <div v-show="activeTab === 'account'" class="settings-section">
          <h3 class="settings-section__title">账号管理</h3>
          <div class="account-fields">
            <div
              v-for="(field, index) in accountFields"
              :key="field.key"
              class="account-fields__row"
              :class="{ 'account-fields__row--last': index === accountFields.length - 1 }"
            >
              <span class="account-fields__label">{{ field.label }}</span>
              <span class="account-fields__value">{{ (userInfo as Record<string, any>)?.[field.key] || '-' }}</span>
            </div>
            <div class="account-fields__row">
              <span class="account-fields__label">修改密码</span>
              <button class="account-fields__btn" @click="goToChangePassword">
                修改
              </button>
            </div>
          </div>
        </div>

        <div v-show="activeTab === 'agreement'" class="settings-section">
          <h3 class="settings-section__title">服务协议</h3>
          <div class="account-fields">
            <div
              v-for="(item, index) in agreementItems"
              :key="item.label"
              class="account-fields__row"
              :class="{ 'account-fields__row--last': index === agreementItems.length - 1 }"
            >
              <span class="account-fields__label">{{ item.label }}</span>
              <button class="account-fields__btn" @click="navigateTo(item.path)">
                查看详情 →
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- Change password form -->
    <div v-else class="password-form">
<!--      <p class="password-form__desc">请输入原密码和新密码以修改您的登录密码。</p>-->
      <Form />
      <div class="password-form__actions">
        <el-button
          type="primary"
          :loading="passwordSubmitting"
          @click="handleSubmitPassword"
        >
          确认修改
        </el-button>
        <el-button
          :disabled="passwordSubmitting"
          @click="goBackToSettings"
        >
          取消
        </el-button>
      </div>
    </div>
  </Modal>
</template>

<style lang="scss" scoped>
.settings-layout {
  display: flex;
  min-height: 340px;

  &__sidebar {
    display: flex;
    flex: 0 0 128px;
    flex-direction: column;
    gap: 2px;
    padding: 0 8px;
    border-right: 1px solid hsl(var(--border));
  }

  &__content {
    flex: 1 1 auto;
    min-width: 0;
    padding: 5px 24px;
    overflow: hidden;
  }
}

.settings-tab {
  width: 100%;
  padding: 8px 12px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  color: hsl(var(--foreground));
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  transition: background 0.15s ease, border-color 0.15s ease;

  &:hover {
    background: hsl(var(--accent));
    border-color: hsl(var(--border));
  }

  &.is-active {
    font-weight: 600;
    color: hsl(var(--primary));
    background: hsl(var(--primary) / 12%);
    border-color: hsl(var(--primary) / 25%);
  }
}

.settings-section {
  &__title {
    margin: 0 0 4px;
    font-size: 15px;
    font-weight: 600;
    color: hsl(var(--foreground));
  }

  &__desc {
    margin: 0 0 20px;
    font-size: 13px;
    line-height: 1.5;
    color: hsl(var(--muted-foreground));
  }

}

.account-fields {
  margin-top: 16px;

  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 0;
    border-bottom: 1px solid hsl(var(--border));

    /* &--last {
      border-bottom: none;
    } */
  }

  &__label {
    font-size: 13px;
    color: hsl(var(--muted-foreground));
  }

  &__value {
    font-size: 13px;
    font-weight: 500;
    color: hsl(var(--foreground));
    text-align: right;
  }

  &__btn {
    padding: 6px 16px;
    font-family: inherit;
    font-size: 13px;
    font-weight: 500;
    color: hsl(var(--primary));
    cursor: pointer;
    background: hsl(var(--primary) / 8%);
    border: 1px solid hsl(var(--primary) / 20%);
    border-radius: 6px;
    transition: background 0.15s ease;

    &:hover {
      background: hsl(var(--primary) / 16%);
    }
  }
}

.modal-title__back {
  display: inline-flex;
  align-items: center;
  margin-right: 4px;
  vertical-align: middle;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s ease;

  &:hover {
    background: hsl(var(--accent));
  }
}

.password-form {
  width: 400px;
  min-height: 340px;
  padding: 8px 0;
  margin: auto;

  &__desc {
    margin: 0 0 20px;
    font-size: 13px;
    line-height: 1.5;
    color: hsl(var(--muted-foreground));
  }

  &__actions {
    display: flex;
    gap: 12px;
    justify-content: center;
    margin-top: 8px;
  }
}

.theme-options {
  display: flex;
  gap: 12px;
}

.theme-option {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  width: 110px;
  padding: 16px 24px;
  cursor: pointer;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  transition: background 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;

  &:hover {
    background: hsl(var(--accent));
    border-color: hsl(var(--border));
  }

  &.is-active {
    background: hsl(var(--primary) / 10%);
    border-color: hsl(var(--primary) / 40%);
    box-shadow: 0 0 0 2px hsl(var(--primary) / 20%);
  }

  &__icon {
    width: 28px;
    height: 28px;
    color: hsl(var(--foreground));

    .theme-option.is-active & {
      color: hsl(var(--primary));
    }
  }

  &__label {
    font-size: 13px;
    color: hsl(var(--muted-foreground));

    .theme-option.is-active & {
      font-weight: 500;
      color: hsl(var(--primary));
    }
  }
}
</style>
