<script lang="ts" setup>
import type { Agent } from '#/api/core/agent';

import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import {
  ElAvatar,
  ElButton,
  ElCol,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus';

import {
  deleteAgentApi,
  getAgentApi,
  updateAgentApi,
  uploadAvatarApi,
} from '#/api';

import AgentDataSourceConfig from './components/AgentDataSourceConfig.vue';

const route = useRoute();
const router = useRouter();
const agent = ref<Agent>({});
const loading = ref(false);
const uploading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);

const isDraftOrPending = computed(() =>
  ['draft', 'draft-pending'].includes(agent.value.status ?? ''),
);

async function loadAgent() {
  try {
    const id = Number(route.params.id);
    const result = await getAgentApi(id);
    if (!result) {
      ElMessage.error('智能体不存在');
      router.push('/agent/list');
      return;
    }
    agent.value = result;
  } catch {
    ElMessage.error('加载智能体失败');
    router.push('/agent/list');
  }
}

function goBack() {
  router.push('/agent/list');
}

function triggerFileUpload() {
  fileInput.value?.click();
}

async function handleFileUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件');
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB');
    return;
  }

  try {
    uploading.value = true;
    const reader = new FileReader();
    reader.addEventListener('load', (e) => {
      if (e.target?.result) agent.value.avatar = e.target.result as string;
    });
    reader.readAsDataURL(file);

    const result = await uploadAvatarApi(file);
    if (result.success && result.url) {
      agent.value.avatar = result.url;
      ElMessage.success('头像上传成功');
    } else {
      throw new Error(result.message || '上传失败');
    }
  } catch (error) {
    ElMessage.error(
      `头像上传失败: ${error instanceof Error ? error.message : '未知错误'}`,
    );
  } finally {
    uploading.value = false;
    if (fileInput.value) fileInput.value.value = '';
  }
}

async function saveAgent() {
  if (!agent.value.name?.trim()) {
    ElMessage.error('请输入智能体名称');
    return;
  }
  try {
    loading.value = true;
    await updateAgentApi(Number(route.params.id), {
      name: agent.value.name?.trim(),
      description: agent.value.description?.trim(),
      avatar: agent.value.avatar,
      category: agent.value.category?.trim(),
      tags: agent.value.tags?.trim(),
      prompt: agent.value.prompt?.trim(),
      status: agent.value.status,
    });
    ElMessage.success('保存成功');
  } catch {
    ElMessage.error('保存失败，请重试');
  } finally {
    loading.value = false;
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体"${agent.value.name}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteAgentApi(Number(route.params.id));
    ElMessage.success('智能体已删除');
    router.push('/agent/list');
  } catch {
    // cancelled
  }
}

function formatDateTime(time?: string) {
  if (!time) return '-';
  return time.replace('T', ' ').slice(0, 19);
}

const formattedCreateTime = computed(() =>
  formatDateTime(agent.value.createTime),
);
const formattedUpdateTime = computed(() =>
  formatDateTime(agent.value.updateTime),
);

onMounted(loadAgent);
</script>

<template>
  <Page :title="agent.name || '智能体详情'" description="编辑智能体配置">
    <div class="mx-auto flex max-w-[900px] flex-col gap-6">
      <div class="flex items-center gap-4 rounded-lg bg-white p-6 shadow-sm">
        <div class="relative group">
          <ElAvatar
            :size="60"
            class="cursor-pointer"
            style="font-size:24px;font-weight:600;color:#fff;background:#2f6bff"
          >
            {{ agent.name?.charAt(0) || 'A' }}
          </ElAvatar>
          <div
            class="absolute inset-0 flex cursor-pointer items-center justify-center rounded-full bg-black/40 opacity-0 transition-opacity group-hover:opacity-100"
            @click="triggerFileUpload"
          >
            <span class="text-xs text-white">替换</span>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleFileUpload"
          />
        </div>
        <div>
          <h2 class="m-0 text-xl font-semibold">{{ agent.name }}</h2>
          <p class="m-0 mt-1 text-sm text-gray-500">{{ agent.description }}</p>
        </div>
      </div>

      <div class="rounded-lg bg-white p-6 shadow-sm">
        <h3 class="m-0 mb-4 text-base font-semibold">基本信息</h3>

        <ElRow :gutter="20">
          <ElCol :span="12">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">智能体名称</label>
              <ElInput v-model="agent.name" placeholder="请输入智能体名称" />
            </div>
          </ElCol>
          <ElCol :span="12">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">分类</label>
              <ElInput
                v-model="agent.category"
                placeholder="请输入智能体分类"
              />
            </div>
          </ElCol>
        </ElRow>

        <ElRow :gutter="20">
          <ElCol :span="24">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">描述</label>
              <ElInput
                v-model="agent.description"
                :rows="3"
                type="textarea"
                placeholder="请输入智能体描述"
              />
            </div>
          </ElCol>
        </ElRow>

        <ElRow :gutter="20">
          <ElCol :span="24">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">智能体Prompt</label>
              <ElInput
                v-model="agent.prompt"
                :rows="4"
                type="textarea"
                placeholder="请输入智能体Prompt"
              />
            </div>
          </ElCol>
        </ElRow>

        <ElRow :gutter="20">
          <ElCol :span="12">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">标签</label>
              <ElInput v-model="agent.tags" placeholder="多个标签用逗号分隔" />
            </div>
          </ElCol>
          <ElCol :span="12">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">状态</label>
              <ElSelect
                v-model="agent.status"
                placeholder="请选择状态"
                :disabled="!isDraftOrPending"
              >
                <ElOption key="draft" label="待发布" value="draft" />
                <ElOption key="published" label="已发布" value="published" />
                <ElOption key="offline" label="已下线" value="offline" />
              </ElSelect>
            </div>
          </ElCol>
        </ElRow>

        <ElRow :gutter="20">
          <ElCol :span="12">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">创建时间</label>
              <ElInput disabled :model-value="formattedCreateTime" />
            </div>
          </ElCol>
          <ElCol :span="12">
            <div class="mb-5">
              <label class="mb-2 block text-sm font-medium text-gray-700">更新时间</label>
              <ElInput disabled :model-value="formattedUpdateTime" />
            </div>
          </ElCol>
        </ElRow>

        <div class="flex gap-3 border-t border-gray-100 pt-5">
          <ElButton type="primary" :loading="loading" @click="saveAgent">
            {{ loading ? '保存中...' : '保存' }}
          </ElButton>
          <ElButton type="danger" @click="handleDelete">删除智能体</ElButton>
          <ElButton @click="goBack">返回列表</ElButton>
        </div>
      </div>

      <div class="rounded-lg bg-white p-6 shadow-sm">
        <AgentDataSourceConfig :agent-id="Number(route.params.id)" />
      </div>
    </div>
  </Page>
</template>
