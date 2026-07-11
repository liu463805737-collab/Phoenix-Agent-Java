<script setup lang="ts">
import { ElDrawer } from 'element-plus';

defineProps<{
  user: Record<string, any> | null;
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

const genderMap: Record<string, string> = {
  '0': '女',
  '1': '男',
  '2': '保密',
};

const fieldLabelClass = 'text-sm text-gray-400 shrink-0';
const fieldValueClass = 'text-sm font-medium text-gray-700 text-right truncate ml-4 max-w-[60%]';
</script>

<template>
  <ElDrawer
    :model-value="visible"
    size="350px"
    direction="ltr"
    :show-close="false"
    @update:model-value="emit('update:visible', $event)"
  >
    <template #header>
      <div class="flex items-center gap-3 w-full bg-gradient-to-r from-[#2563eb] to-[#1d4ed8] px-5 py-7" style="margin: calc(var(--el-drawer-padding-primary) * -1)">
        <div class="flex items-center justify-center w-13 h-13 shrink-0 text-xl font-bold text-[#2563eb] bg-white rounded-full">
          {{ user?.realName?.charAt(0) || user?.username?.charAt(0) || 'U' }}
        </div>
        <div class="min-w-0">
          <div class="text-lg font-semibold text-white">{{ user?.realName || user?.username || '用户' }}</div>
          <div class="text-xs text-white/70 mt-0.5">基本信息</div>
        </div>
        <button class="absolute top-2 right-2 flex items-center justify-center w-8 h-8 ml-auto shrink-0 text-white/70 bg-white/15 rounded-full hover:text-white hover:bg-white/25 transition-colors" @click="emit('update:visible', false)">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path d="M18.3 5.7a1 1 0 0 0-1.4 0L12 10.6 7.1 5.7a1 1 0 1 0-1.4 1.4L10.6 12l-4.9 4.9a1 1 0 1 0 1.4 1.4L12 13.4l4.9 4.9a1 1 0 0 0 1.4-1.4L13.4 12l4.9-4.9a1 1 0 0 0 0-1.4" fill="currentColor" />
          </svg>
        </button>
      </div>
    </template>

    <!-- account info -->
    <div class="mb-1">
      <div class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">账户信息</div>
      <div class="flex justify-between items-center py-2.5 border-b border-gray-100">
        <span :class="fieldLabelClass">用户名</span>
        <span :class="fieldValueClass">{{ user?.username || '-' }}</span>
      </div>
      <div class="flex justify-between items-center py-2.5 border-b border-gray-100">
        <span :class="fieldLabelClass">真实姓名</span>
        <span :class="fieldValueClass">{{ user?.realName || '-' }}</span>
      </div>
      <div class="flex justify-between items-center py-2.5 border-b border-gray-100">
        <span :class="fieldLabelClass">昵称</span>
        <span :class="fieldValueClass">{{ user?.nickName || '-' }}</span>
      </div>
    </div>

    <div class="h-px bg-gray-100 my-2" />

    <!-- contact -->
    <div class="mb-1">
      <div class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">联系方式</div>
      <div class="flex justify-between items-center py-2.5 border-b border-gray-100">
        <span :class="fieldLabelClass">手机号</span>
        <span :class="fieldValueClass">{{ user?.phone || '-' }}</span>
      </div>
      <div class="flex justify-between items-center py-2.5 border-b border-gray-100">
        <span :class="fieldLabelClass">邮箱</span>
        <span :class="fieldValueClass">{{ user?.email || '-' }}</span>
      </div>
    </div>

    <div class="h-px bg-gray-100 my-2" />

    <!-- other -->
    <div>
      <div class="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-3">其他</div>
      <div class="flex justify-between items-center py-2.5 border-b border-gray-100">
        <span :class="fieldLabelClass">性别</span>
        <span :class="fieldValueClass">{{ user?.gender ? (genderMap[user.gender] || user.gender) : '-' }}</span>
      </div>
      <div class="flex justify-between items-center py-2.5 border-b border-gray-100">
        <span :class="fieldLabelClass">账号编码</span>
        <span :class="fieldValueClass">{{ user?.code || '-' }}</span>
      </div>
    </div>
  </ElDrawer>
</template>

<style>
.el-drawer__header {
  border-bottom: none !important;
}
</style>
