<script lang="ts" setup>
import type { Agent, PresetQuestion, PromptConfig } from '#/api/core/agent';

import { computed, nextTick, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import {
  ElAside,
  ElAvatar,
  ElButton,
  ElCheckbox,
  ElCol,
  ElContainer,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElMenuItemGroup,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElRow,
  ElSelect,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  batchSavePresetQuestionsApi,
  createAgentApi,
  deleteAgentApi,
  deletePresetQuestionApi,
  deletePromptConfigApi,
  getPresetQuestionsApi,
  getPromptConfigListApi,
  savePromptConfigApi,
  togglePromptConfigApi,
  updateAgentApi,
  uploadAvatarApi,
} from '#/api';

import AccessApi from '#/components/agent/AccessApi.vue';
import AgentDataSourceConfig from '../edit/components/AgentDataSourceConfig.vue';
import AgentKnowledgeConfig from './components/AgentKnowledgeConfig.vue';
import BusinessKnowledgeConfig from './components/BusinessKnowledgeConfig.vue';
import SemanticsConfig from './components/SemanticsConfig.vue';

interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

const emit = defineEmits<{
  success: [];
}>();

function unwrapData<T>(resp: unknown): T {
  if (resp && typeof resp === 'object' && 'data' in resp) {
    return (resp as ApiResponse<T>).data;
  }
  return resp as T;
}

const uploading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const isEdit = ref(false);
const editId = ref<number | undefined>();
const submitting = ref(false);
const activeMenu = ref('basic');

const form = reactive({
  name: '',
  description: '',
  avatar: '',
  category: '',
  tags: '',
  prompt: '',
  status: 'draft',
  createTime: '',
  updateTime: '',
  apiKey: '',
  apiKeyEnabled: 0,
  humanReviewEnabled: 0,
});

const isDraftOrPending = computed(() =>
  ['draft', 'draft-pending'].includes(form.status),
);

// ===== 各模块加载状态和数据 =====
const loadingPrompt = ref(false);
const loadingPreset = ref(false);

const promptConfigs = ref<PromptConfig[]>([]);
const presetQuestions = ref<PresetQuestion[]>([]);

function generateFallbackAvatar(): string {
  const colors = [
    '3B82F6',
    '8B5CF6',
    '10B981',
    'F59E0B',
    'EF4444',
    '6366F1',
    'EC4899',
    '14B8A6',
  ];
  const color = colors[Math.floor(Math.random() * colors.length)];
  const letters = ['AI', '数据', '智能', 'DA', 'BI', 'ML', 'DL', 'NL'];
  const letter = letters[Math.floor(Math.random() * letters.length)];
  const svg = `<svg width="200" height="200" xmlns="http://www.w3.org/2000/svg"><rect width="200" height="200" fill="#${color}"/><text x="100" y="120" font-family="Arial,sans-serif" font-size="48" font-weight="bold" text-anchor="middle" fill="white">${letter}</text></svg>`;
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
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
      if (e.target?.result) form.avatar = e.target.result as string;
    });
    reader.readAsDataURL(file);

    const result = await uploadAvatarApi(file);
    if (result.success && result.url) {
      form.avatar = result.url;
      ElMessage.success('头像上传成功');
    } else {
      throw new Error(result.message || '上传失败');
    }
  } catch (error) {
    ElMessage.error(
      `头像上传失败: ${error instanceof Error ? error.message : '未知错误'}`,
    );
    form.avatar = generateFallbackAvatar();
  } finally {
    uploading.value = false;
    if (fileInput.value) fileInput.value.value = '';
  }
}

function formatDateTime(time?: string) {
  if (!time) return '-';
  return time.replace('T', ' ').slice(0, 19);
}

function resetForm() {
  form.name = '';
  form.description = '';
  form.avatar = generateFallbackAvatar();
  form.category = '';
  form.tags = '';
  form.prompt = '';
  form.status = 'draft';
  form.createTime = '';
  form.updateTime = '';
}

function handleMenuSelect(index: string) {
  if (index === 'basic') {
    activeMenu.value = index;
    return;
  }

  if (!isEdit.value && !form.name.trim()) {
    ElMessage.warning('请先填写智能体名称等基本信息');
    return;
  }

  activeMenu.value = index;
}

async function checkSuccess(resp: unknown): Promise<boolean> {
  if (resp && typeof resp === 'object' && 'success' in resp) {
    return (resp as Record<string, unknown>).success as boolean;
  }
  return true;
}

async function ensureSuccess<T>(resp: T): Promise<T> {
  if (!(await checkSuccess(resp))) {
    throw new Error(
      ((resp as Record<string, unknown>).message as string) || '操作失败',
    );
  }
  return resp;
}

// ===== 各模块数据加载 =====

async function loadPromptConfigs() {
  if (!editId.value) return;
  loadingPrompt.value = true;
  try {
    promptConfigs.value = unwrapData<PromptConfig[]>(
      await getPromptConfigListApi('system', editId.value),
    );
  } catch {
    promptConfigs.value = [];
  } finally {
    loadingPrompt.value = false;
  }
}

async function loadPresetQuestions() {
  if (!editId.value) return;
  loadingPreset.value = true;
  try {
    presetQuestions.value = unwrapData<PresetQuestion[]>(
      await getPresetQuestionsApi(editId.value),
    );
  } catch {
    presetQuestions.value = [];
  } finally {
    loadingPreset.value = false;
  }
}

// ===== Prompt 配置 CRUD =====
const showPromptDialog = ref(false);
const submittingPrompt = ref(false);
const editingPromptId = ref<number | undefined>();
const showPriorityDialog = ref(false);
const editingPriorityConfig = ref<null | PromptConfig>(null);
const showBatchActions = ref(false);
const selectedPromptConfigs = ref<number[]>([]);
const promptForm = reactive({
  promptType: 'system',
  name: '',
  optimizationPrompt: '',
  description: '',
  enabled: true,
  priority: 0,
  displayOrder: 0,
});
const priorityForm = reactive({
  priority: 0,
  displayOrder: 0,
});

function openAddPromptDialog() {
  editingPromptId.value = undefined;
  promptForm.promptType = 'system';
  promptForm.name = '';
  promptForm.optimizationPrompt = '';
  promptForm.description = '';
  promptForm.enabled = true;
  promptForm.priority = 0;
  promptForm.displayOrder = 0;
  showPromptDialog.value = true;
}

function openEditPromptDialog(config: PromptConfig) {
  editingPromptId.value = config.id;
  promptForm.promptType = config.promptType || 'system';
  promptForm.name = (config as any).name || '';
  promptForm.optimizationPrompt = config.optimizationPrompt || '';
  promptForm.description = config.description || '';
  promptForm.enabled = config.enabled ?? true;
  promptForm.priority = config.priority ?? 0;
  promptForm.displayOrder = (config as any).displayOrder ?? 0;
  showPromptDialog.value = true;
}

function openPriorityDialog(config: PromptConfig) {
  editingPriorityConfig.value = config;
  priorityForm.priority = config.priority ?? 0;
  priorityForm.displayOrder = (config as any).displayOrder ?? 0;
  showPriorityDialog.value = true;
}

function closePriorityDialog() {
  showPriorityDialog.value = false;
  editingPriorityConfig.value = null;
  priorityForm.priority = 0;
  priorityForm.displayOrder = 0;
}

const isAllPromptSelected = computed(() => {
  return (
    promptConfigs.value.length > 0 &&
    selectedPromptConfigs.value.length === promptConfigs.value.length
  );
});

const isPromptIndeterminate = computed(() => {
  return (
    selectedPromptConfigs.value.length > 0 &&
    selectedPromptConfigs.value.length < promptConfigs.value.length
  );
});

function toggleSelectAllPrompt() {
  if (isAllPromptSelected.value) {
    selectedPromptConfigs.value = [];
  } else {
    selectedPromptConfigs.value = promptConfigs.value.map((c) => c.id!);
  }
}

function clearPromptSelection() {
  selectedPromptConfigs.value = [];
  showBatchActions.value = false;
}

function togglePromptSelection(id: number, checked: boolean) {
  if (checked) {
    if (!selectedPromptConfigs.value.includes(id)) {
      selectedPromptConfigs.value.push(id);
    }
  } else {
    selectedPromptConfigs.value = selectedPromptConfigs.value.filter(
      (i) => i !== id,
    );
  }
}

async function batchTogglePrompt(enabled: boolean) {
  if (selectedPromptConfigs.value.length === 0) return;
  try {
    for (const id of selectedPromptConfigs.value) {
      await ensureSuccess(await togglePromptConfigApi(id, enabled));
    }
    ElMessage.success(enabled ? '批量启用成功' : '批量禁用成功');
    clearPromptSelection();
    await loadPromptConfigs();
  } catch {
    ElMessage.error('批量操作失败');
  }
}

async function handleSavePrompt() {
  if (!promptForm.optimizationPrompt.trim()) {
    ElMessage.error('请输入 Prompt 内容');
    return;
  }
  submittingPrompt.value = true;
  try {
    const data: Partial<PromptConfig> = {
      id: editingPromptId.value,
      promptType: promptForm.promptType,
      optimizationPrompt: promptForm.optimizationPrompt.trim(),
      description: promptForm.description.trim(),
      enabled: promptForm.enabled,
      priority: promptForm.priority,
      agentId: editId.value,
    };
    (data as any).name = promptForm.name.trim();
    (data as any).displayOrder = promptForm.displayOrder;
    await ensureSuccess(await savePromptConfigApi(data as PromptConfig));
    ElMessage.success(
      editingPromptId.value ? 'Prompt 配置更新成功' : 'Prompt 配置创建成功',
    );
    showPromptDialog.value = false;
    await loadPromptConfigs();
  } catch {
    ElMessage.error('操作失败');
  } finally {
    submittingPrompt.value = false;
  }
}

async function handleTogglePromptConfig(id?: number, enabled?: boolean) {
  if (!id) return;
  try {
    await ensureSuccess(await togglePromptConfigApi(id, !!enabled));
    ElMessage.success(enabled ? '已启用' : '已禁用');
    await loadPromptConfigs();
  } catch {
    ElMessage.error('操作失败');
  }
}

async function handleDeletePromptConfig(id?: number) {
  if (!id) return;
  try {
    await ElMessageBox.confirm('确定要删除该 Prompt 配置吗？', '确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    await ensureSuccess(await deletePromptConfigApi(id));
    ElMessage.success('Prompt 配置已删除');
    await loadPromptConfigs();
  } catch {
    // cancelled
  }
}

async function updatePriority() {
  if (!editingPriorityConfig.value?.id) return;
  try {
    const resp = await savePromptConfigApi({
      id: editingPriorityConfig.value.id,
      priority: priorityForm.priority,
    } as PromptConfig);
    await ensureSuccess(resp);
    ElMessage.success('优先级更新成功');
    closePriorityDialog();
    await loadPromptConfigs();
  } catch {
    ElMessage.error('优先级更新失败');
  }
}

// ===== 预设问题管理 CRUD =====
const showPresetDialog = ref(false);
const submittingPreset = ref(false);
const editingPresetId = ref<number | undefined>();
const presetForm = reactive({
  question: '',
  answer: '',
  sortOrder: 0,
});

function openAddPresetDialog() {
  editingPresetId.value = undefined;
  presetForm.question = '';
  presetForm.answer = '';
  presetForm.sortOrder = presetQuestions.value.length + 1;
  showPresetDialog.value = true;
}

function openEditPresetDialog(item: PresetQuestion) {
  editingPresetId.value = item.id;
  presetForm.question = item.question || '';
  presetForm.answer = item.answer || '';
  presetForm.sortOrder = item.sortOrder ?? 0;
  showPresetDialog.value = true;
}

async function handleSavePresetQuestion() {
  if (!presetForm.question.trim()) {
    ElMessage.error('请输入问题');
    return;
  }
  submittingPreset.value = true;
  try {
    const questions: PresetQuestion[] = editId.value
      ? [
          ...presetQuestions.value.map((q) => ({
            id: q.id,
            question: q.question,
            answer: q.answer,
            sortOrder: q.sortOrder,
          })),
        ]
      : [];

    if (editingPresetId.value) {
      const idx = questions.findIndex((q) => q.id === editingPresetId.value);
      if (idx !== -1) {
        questions[idx] = {
          id: editingPresetId.value,
          question: presetForm.question.trim(),
          answer: presetForm.answer.trim(),
          sortOrder: presetForm.sortOrder,
        };
      }
    } else {
      questions.push({
        question: presetForm.question.trim(),
        answer: presetForm.answer.trim(),
        sortOrder: presetForm.sortOrder,
      });
    }

    if (editId.value) {
      await ensureSuccess(
        await batchSavePresetQuestionsApi(editId.value, questions),
      );
      ElMessage.success('预设问题保存成功');
      showPresetDialog.value = false;
      await loadPresetQuestions();
    }
  } catch {
    ElMessage.error('操作失败');
  } finally {
    submittingPreset.value = false;
  }
}

async function handleDeletePresetQuestion(id?: number) {
  if (!id) return;
  try {
    await ElMessageBox.confirm('确定要删除该预设问题吗？', '确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    if (editId.value) {
      await ensureSuccess(await deletePresetQuestionApi(editId.value, id));
      ElMessage.success('预设问题已删除');
      await loadPresetQuestions();
    }
  } catch {
    // cancelled
  }
}

watch(activeMenu, (menu) => {
  if (menu === 'go-run') {
    goToRunPage();
    return;
  }
  if (!isEdit.value || !editId.value) return;
  switch (menu) {
    case 'preset-questions': {
      loadPresetQuestions();
      break;
    }
    case 'prompt': {
      loadPromptConfigs();
      break;
    }
  }
});

const router = useRouter();

function goToRunPage() {
  const id = editId.value;
  if (id) {
    drawerApi.close();
    nextTick(() => {
      router.push(`/agent/${id}/run`);
    });
  }
}

async function handleSave() {
  if (!form.name.trim()) {
    ElMessage.error('请输入智能体名称');
    return;
  }

  submitting.value = true;
  try {
    const data: Partial<Agent> = {
      name: form.name.trim(),
      description: form.description.trim(),
      avatar: form.avatar,
      category: form.category.trim(),
      tags: form.tags.trim(),
      prompt: form.prompt.trim(),
      status: form.status,
    };

    if (isEdit.value && editId.value) {
      await ensureSuccess(await updateAgentApi(editId.value, data));
      ElMessage.success('智能体更新成功！');
      emit('success');
      drawerApi.close();
    } else {
      const createdAgent = (await ensureSuccess(
        await createAgentApi(data),
      )) as Agent;
      if (createdAgent?.id) {
        editId.value = createdAgent.id;
        isEdit.value = true;
        form.createTime = createdAgent.createTime || '';
        form.updateTime = createdAgent.updateTime || '';
      }
      ElMessage.success('智能体创建成功！');
      emit('success');
    }
  } catch {
    ElMessage.error('操作失败，请重试');
  } finally {
    submitting.value = false;
  }
}

async function handleDelete() {
  if (!editId.value) return;
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体"${form.name}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await ensureSuccess(await deleteAgentApi(editId.value));
    ElMessage.success('智能体已删除');
    emit('success');
    drawerApi.close();
  } catch {
    // cancelled
  }
}

const drawerTitle = computed(() =>
  isEdit.value ? '编辑智能体' : '创建智能体',
);

const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-200',
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (isOpen) {
      const data = drawerApi.getData<Agent>();
      activeMenu.value = 'basic';

      if (data?.id) {
        isEdit.value = true;
        editId.value = data.id;
        form.name = data.name || '';
        form.description = data.description || '';
        form.avatar = data.avatar || generateFallbackAvatar();
        form.category = data.category || '';
        form.tags = data.tags || '';
        form.prompt = data.prompt || '';
        form.status = data.status || 'draft';
        form.createTime = data.createTime || '';
        form.updateTime = data.updateTime || '';
        form.apiKey = data.apiKey || '';
        form.apiKeyEnabled = data.apiKeyEnabled ?? 0;
        form.humanReviewEnabled = data.humanReviewEnabled ?? 0;
      } else {
        isEdit.value = false;
        editId.value = undefined;
        resetForm();
      }
    }
  },
});
</script>

<template>
  <Drawer :title="drawerTitle" class="w-[80vw]">
    <ElContainer class="h-full gap-4">
      <ElAside
        width="210px"
        class="flex-shrink-0 overflow-auto rounded-lg bg-white p-2 shadow-sm"
      >
        <ElMenu
          :default-active="activeMenu"
          class="!border-none"
          @select="handleMenuSelect"
        >
          <ElMenuItemGroup title="基本信息">
            <ElMenuItem index="basic">
              <ElIcon><IconifyIcon icon="lucide:info" /></ElIcon>
              <span>基本信息</span>
            </ElMenuItem>
          </ElMenuItemGroup>
          <ElMenuItemGroup title="数据源配置">
            <ElMenuItem index="datasource">
              <ElIcon><IconifyIcon icon="lucide:database" /></ElIcon>
              <span>数据源配置</span>
            </ElMenuItem>
          </ElMenuItemGroup>
          <ElMenuItemGroup title="PROMPT配置">
            <ElMenuItem index="prompt">
              <ElIcon><IconifyIcon icon="lucide:message-square" /></ElIcon>
              <span>自定义 PROMPT 配置</span>
            </ElMenuItem>
          </ElMenuItemGroup>
          <ElMenuItemGroup title="知识配置">
            <ElMenuItem index="agent-knowledge">
              <ElIcon><IconifyIcon icon="lucide:book-open" /></ElIcon>
              <span>智能体知识配置</span>
            </ElMenuItem>
            <ElMenuItem index="business-knowledge">
              <ElIcon><IconifyIcon icon="lucide:users" /></ElIcon>
              <span>业务知识配置</span>
            </ElMenuItem>
            <ElMenuItem index="semantic-model">
              <ElIcon><IconifyIcon icon="lucide:briefcase" /></ElIcon>
              <span>语义模型配置</span>
            </ElMenuItem>
          </ElMenuItemGroup>
          <ElMenuItemGroup title="预设问题管理">
            <ElMenuItem index="preset-questions">
              <ElIcon><IconifyIcon icon="lucide:settings" /></ElIcon>
              <span>预设问题管理</span>
            </ElMenuItem>
          </ElMenuItemGroup>
          <ElMenuItemGroup title="运行与发布">
            <ElMenuItem index="go-run">
              <ElIcon><IconifyIcon icon="lucide:play" /></ElIcon>
              <span>前往运行页面</span>
            </ElMenuItem>
            <ElMenuItem index="access-api">
              <ElIcon><IconifyIcon icon="lucide:link" /></ElIcon>
              <span>访问 API</span>
            </ElMenuItem>
          </ElMenuItemGroup>
        </ElMenu>
      </ElAside>

      <ElMain class="overflow-auto rounded-lg bg-white p-6 shadow-sm">
        <!-- 基本信息 -->
        <div v-if="activeMenu === 'basic'">
          <div class="mb-6 flex items-center gap-4 rounded-lg bg-gray-50 p-4">
            <div class="relative group">
              <ElAvatar
                :size="60"
                :src="form.avatar || undefined"
                class="cursor-pointer"
              >
                {{ form.name }}
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
              <h2 class="m-0 text-xl font-semibold">
                {{ form.name || '未命名智能体' }}
              </h2>
              <p class="m-0 mt-1 text-sm text-gray-500">
                {{ form.description }}
              </p>
            </div>
          </div>

          <div>
            <h3 class="m-0 mb-4 text-base font-semibold">基本信息</h3>
            <ElRow :gutter="20">
              <ElCol :span="12">
                <div class="mb-5">
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    智能体名称
                  </label>
                  <ElInput v-model="form.name" placeholder="请输入智能体名称" />
                </div>
              </ElCol>
              <ElCol :span="12">
                <div class="mb-5">
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    分类
                  </label>
                  <ElInput
                    v-model="form.category"
                    placeholder="请输入智能体分类"
                  />
                </div>
              </ElCol>
            </ElRow>

            <ElRow :gutter="20">
              <ElCol :span="24">
                <div class="mb-5">
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    描述
                  </label>
                  <ElInput
                    v-model="form.description"
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
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    智能体Prompt
                  </label>
                  <ElInput
                    v-model="form.prompt"
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
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    标签
                  </label>
                  <ElInput
                    v-model="form.tags"
                    placeholder="多个标签用逗号分隔"
                  />
                </div>
              </ElCol>
              <ElCol :span="12">
                <div class="mb-5">
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    状态
                  </label>
                  <ElSelect
                    v-model="form.status"
                    placeholder="请选择状态"
                    :disabled="isEdit && !isDraftOrPending"
                  >
                    <ElOption key="draft" label="待发布" value="draft" />
                    <ElOption
                      key="published"
                      label="已发布"
                      value="published"
                    />
                    <ElOption key="offline" label="已下线" value="offline" />
                  </ElSelect>
                </div>
              </ElCol>
            </ElRow>

            <ElRow v-if="isEdit" :gutter="20">
              <ElCol :span="12">
                <div class="mb-5">
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    创建时间
                  </label>
                  <ElInput
                    disabled
                    :model-value="formatDateTime(form.createTime)"
                  />
                </div>
              </ElCol>
              <ElCol :span="12">
                <div class="mb-5">
                  <label class="mb-2 block text-sm font-medium text-gray-700">
                    更新时间
                  </label>
                  <ElInput
                    disabled
                    :model-value="formatDateTime(form.updateTime)"
                  />
                </div>
              </ElCol>
            </ElRow>

            <div class="flex gap-3 border-t border-gray-100 pt-5">
              <ElButton
                type="primary"
                :loading="submitting"
                @click="handleSave"
              >
                {{ submitting ? '保存中...' : '保存' }}
              </ElButton>
              <ElButton v-if="isEdit" type="danger" @click="handleDelete">
                删除智能体
              </ElButton>
              <ElButton @click="drawerApi.close()">取消</ElButton>
            </div>
          </div>
        </div>

        <!-- 数据源配置 -->
        <AgentDataSourceConfig
          v-else-if="activeMenu === 'datasource'"
          :agent-id="editId!"
        />

        <!-- PROMPT配置 -->
        <div v-else-if="activeMenu === 'prompt'">
          <div class="prompt-optimization-config">
            <div class="config-header">
              <h3 class="m-0 text-base font-semibold">增强式Prompt优化配置</h3>
              <p class="mt-1 mb-2 text-sm text-gray-500">
                配置的Prompt仅用作效果优化，支持多个提示词配置，在原始模板基础上进行增强。示例配置：
              </p>
              <ul class="mb-4 list-disc pl-5 text-sm text-gray-500">
                <li>1. 查询的年销售额精确到小数点后两位。</li>
                <li>2. 报告格式第一章节请先总结年销售额</li>
              </ul>
            </div>

            <div class="mb-4 rounded-lg border border-gray-200 bg-gray-50 p-3">
              <label class="mb-1 block text-sm font-medium text-gray-600">智能体Prompt</label>
              <div class="text-sm text-gray-800">
                {{
                  form.prompt ||
                  '你是一个销售数据分析专家，能够帮助用户分析销售趋势，客户行为和业务指标。'
                }}
              </div>
            </div>

            <div class="optimization-configs" v-loading="loadingPrompt">
              <div class="mb-3 flex items-center justify-between">
                <h4 class="m-0 text-sm font-semibold">优化配置列表</h4>
                <div class="flex gap-2">
                  <ElButton
                    v-if="promptConfigs.length > 0"
                    size="small"
                    @click="showBatchActions = !showBatchActions"
                  >
                    批量操作
                  </ElButton>
                  <ElButton
                    type="primary"
                    size="small"
                    @click="openAddPromptDialog"
                  >
                    添加优化配置
                  </ElButton>
                </div>
              </div>

              <div
                v-if="showBatchActions"
                class="mb-3 rounded-lg border border-blue-200 bg-blue-50 p-3"
              >
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <ElCheckbox
                      :model-value="isAllPromptSelected"
                      :indeterminate="isPromptIndeterminate"
                      @change="toggleSelectAllPrompt"
                    />
                    <span class="text-sm">
                      {{
                        isAllPromptSelected
                          ? '已全选'
                          : `已选择 ${selectedPromptConfigs.length} 个配置`
                      }}
                    </span>
                  </div>
                  <div class="flex gap-2">
                    <ElButton
                      size="small"
                      type="primary"
                      @click="batchTogglePrompt(true)"
                      :disabled="selectedPromptConfigs.length === 0"
                    >
                      批量启用
                    </ElButton>
                    <ElButton
                      size="small"
                      @click="batchTogglePrompt(false)"
                      :disabled="selectedPromptConfigs.length === 0"
                    >
                      批量禁用
                    </ElButton>
                    <ElButton size="small" @click="clearPromptSelection">
                      取消选择
                    </ElButton>
                  </div>
                </div>
              </div>

              <div
                v-if="promptConfigs.length === 0"
                class="py-8 text-center text-sm text-gray-400"
              >
                暂无优化配置，点击"添加优化配置"开始配置
              </div>

              <div v-else class="space-y-3">
                <div
                  v-for="config in promptConfigs"
                  :key="config.id"
                  class="rounded-lg border border-gray-200 p-4"
                  :class="{
                    'opacity-60': !config.enabled,
                    'border-blue-200': selectedPromptConfigs.includes(
                      config.id!,
                    ),
                  }"
                >
                  <div class="mb-2 flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <input
                        v-if="showBatchActions"
                        type="checkbox"
                        :checked="selectedPromptConfigs.includes(config.id!)"
                        @change="
                          (e) =>
                            togglePromptSelection(
                              config.id!,
                              (e.target as HTMLInputElement).checked,
                            )
                        "
                        class="checkbox"
                      />
                      <span class="font-medium">{{
                        (config as any).name || '未命名配置'
                      }}</span>
                      <ElTag
                        v-if="config.priority !== undefined"
                        size="small"
                        type="info"
                      >
                        优先级: {{ config.priority }}
                      </ElTag>
                    </div>
                    <div class="flex gap-1">
                      <ElSwitch
                        size="small"
                        :model-value="config.enabled"
                        @change="
                          (val) =>
                            handleTogglePromptConfig(config.id, val as boolean)
                        "
                      />
                      <ElButton
                        size="small"
                        link
                        @click="openEditPromptDialog(config)"
                      >
                        编辑
                      </ElButton>
                      <ElButton
                        size="small"
                        link
                        @click="openPriorityDialog(config)"
                      >
                        优先级
                      </ElButton>
                      <ElButton
                        size="small"
                        link
                        type="danger"
                        @click="handleDeletePromptConfig(config.id)"
                      >
                        删除
                      </ElButton>
                    </div>
                  </div>
                  <div
                    v-if="config.description"
                    class="mb-1 text-sm text-gray-500"
                  >
                    {{ config.description }}
                  </div>
                  <div class="rounded-md bg-gray-50 p-2 text-sm text-gray-700">
                    {{ config.optimizationPrompt }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 智能体知识配置 -->
        <div v-else-if="activeMenu === 'agent-knowledge'">
          <AgentKnowledgeConfig v-if="editId" :agent-id="editId" />
          <div
            v-else
            class="rounded-lg border border-gray-200 p-4 text-center text-gray-400"
          >
            请先创建并保存智能体
          </div>
        </div>

        <!-- 业务知识配置 -->
        <div v-else-if="activeMenu === 'business-knowledge'">
          <BusinessKnowledgeConfig v-if="editId" :agent-id="editId" />
          <div
            v-else
            class="rounded-lg border border-gray-200 p-4 text-center text-gray-400"
          >
            请先创建并保存智能体
          </div>
        </div>

        <!-- 语义模型配置 -->
        <div v-else-if="activeMenu === 'semantic-model'">
          <SemanticsConfig v-if="editId" :agent-id="editId" />
          <div
            v-else
            class="rounded-lg border border-gray-200 p-4 text-center text-gray-400"
          >
            请先创建并保存智能体
          </div>
        </div>

        <!-- 预设问题管理 -->
        <div v-else-if="activeMenu === 'preset-questions'">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <h3 class="m-0 text-base font-semibold">预设问题管理</h3>
              <p class="mt-1 text-sm text-gray-500">管理预设的提问</p>
            </div>
            <ElButton type="primary" size="small" @click="openAddPresetDialog">
              添加问题
            </ElButton>
          </div>
          <ElTable
            :data="presetQuestions"
            v-loading="loadingPreset"
            border
            stripe
            empty-text="暂无预设问题"
            class="w-full"
          >
            <ElTableColumn prop="question" label="问题" min-width="200" />
            <ElTableColumn label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <ElButton size="small" link @click="openEditPresetDialog(row)">
                  编辑
                </ElButton>
                <ElButton
                  type="danger"
                  size="small"
                  link
                  @click="handleDeletePresetQuestion(row.id)"
                >
                  删除
                </ElButton>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>

        <!-- 运行与发布 -->
        <div v-else-if="activeMenu === 'access-api'">
          <AccessApi v-if="editId" :agent-id="editId" />
          <div
            v-else
            class="rounded-lg border border-gray-200 p-4 text-center text-gray-400"
          >
            请先创建并保存智能体
          </div>
        </div>
      </ElMain>
    </ElContainer>

    <!-- Prompt 配置对话框 -->
    <ElDialog
      v-model="showPromptDialog"
      :title="editingPromptId ? '编辑优化配置' : '添加优化配置'"
      width="600px"
      :close-on-click-modal="false"
    >
      <ElForm label-position="top">
        <ElFormItem label="配置名称">
          <ElInput v-model="promptForm.name" placeholder="请输入配置名称" />
        </ElFormItem>
        <ElFormItem label="配置描述">
          <ElInput
            v-model="promptForm.description"
            placeholder="请输入配置描述"
          />
        </ElFormItem>
        <ElFormItem label="优化提示词内容">
          <ElInput
            v-model="promptForm.optimizationPrompt"
            :rows="6"
            type="textarea"
            placeholder="请输入优化提示词内容，支持模板变量如 {user_requirements_and_plan}"
          />
        </ElFormItem>
        <div class="flex gap-4">
          <ElFormItem label="优先级" class="flex-1">
            <ElInputNumber
              v-model="promptForm.priority"
              :min="0"
              :max="100"
              class="w-full"
              placeholder="0-100，数字越大优先级越高"
            />
          </ElFormItem>
          <ElFormItem label="显示顺序" class="flex-1">
            <ElInputNumber
              v-model="promptForm.displayOrder"
              :min="0"
              class="w-full"
              placeholder="显示顺序，数字越小越靠前"
            />
          </ElFormItem>
        </div>
        <ElFormItem label="启用">
          <ElSwitch v-model="promptForm.enabled" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="showPromptDialog = false">取消</ElButton>
        <ElButton
          type="primary"
          :loading="submittingPrompt"
          @click="handleSavePrompt"
        >
          保存配置
        </ElButton>
      </template>
    </ElDialog>

    <!-- 优先级设置对话框 -->
    <ElDialog
      v-model="showPriorityDialog"
      title="设置优先级"
      width="400px"
      :close-on-click-modal="false"
    >
      <ElForm label-position="top">
        <ElFormItem label="优先级 (0-100)">
          <ElInputNumber
            v-model="priorityForm.priority"
            :min="0"
            :max="100"
            class="w-full"
            placeholder="数字越大优先级越高"
          />
          <div class="mt-1 text-xs text-gray-400">
            优先级越高，该配置在多个配置中的执行顺序越靠前
          </div>
        </ElFormItem>
        <ElFormItem label="显示顺序">
          <ElInputNumber
            v-model="priorityForm.displayOrder"
            :min="0"
            class="w-full"
            placeholder="数字越小越靠前"
          />
          <div class="mt-1 text-xs text-gray-400">
            控制配置在列表中的显示顺序
          </div>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="closePriorityDialog">取消</ElButton>
        <ElButton type="primary" @click="updatePriority">保存</ElButton>
      </template>
    </ElDialog>

    <!-- 预设问题对话框 -->
    <ElDialog
      v-model="showPresetDialog"
      :title="editingPresetId ? '编辑预设问题' : '添加预设问题'"
      width="600px"
      :close-on-click-modal="false"
    >
      <ElForm label-position="top">
        <ElFormItem label="问题">
          <ElInput v-model="presetForm.question" placeholder="请输入预设问题" />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber
            v-model="presetForm.sortOrder"
            :min="1"
            class="w-full"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="showPresetDialog = false">取消</ElButton>
        <ElButton
          type="primary"
          :loading="submittingPreset"
          @click="handleSavePresetQuestion"
        >
          保存
        </ElButton>
      </template>
    </ElDialog>
  </Drawer>
</template>
