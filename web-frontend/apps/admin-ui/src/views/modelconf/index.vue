<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { ModelConfig } from '#/api/core/modelConfig';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  ElButton,
  ElCard,
  ElDialog,
  ElDivider,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElSelect,
  ElSkeleton,
  ElSlider,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {
  activateModelConfigApi,
  addModelConfigApi,
  deleteModelConfigApi,
  getModelConfigListApi,
  testModelConfigConnectionApi,
  updateModelConfigApi,
} from '#/api';

defineOptions({ name: 'ModelConfig' });

const loading = ref(true);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);
const activatingId = ref<null | number>(null);
const testingId = ref<null | number>(null);
const activeFilter = ref('');
const configs = ref<ModelConfig[]>([]);
const formRef = ref<FormInstance | null>(null);

const defaultFormData: ModelConfig = {
  provider: '',
  apiKey: '',
  baseUrl: '',
  modelName: '',
  modelType: 'CHAT',
  temperature: 0,
  maxTokens: 2000,
  completionsPath: '',
  embeddingsPath: '',
  isActive: false,
  proxyEnabled: false,
  proxyHost: '',
  proxyPort: 7890,
  proxyUsername: '',
  proxyPassword: '',
};

const formData = reactive<ModelConfig>({ ...defaultFormData });

const providerBaseUrlMap: Record<string, string> = {
  deepseek: 'https://api.deepseek.com',
  qwen: 'https://dashscope.aliyuncs.com/compatible-mode',
  openai: 'https://api.openai.com',
  siliconflow: 'https://api.siliconflow.cn',
  custom: '',
};

function updateBaseUrlByProvider(provider: string) {
  if (provider && provider !== 'custom') {
    formData.baseUrl = providerBaseUrlMap[provider] || '';
  }
}

const formRules: FormRules = {
  provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  apiKey: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (formData.provider === 'custom') {
          callback();
        } else if (!value || value.trim() === '') {
          callback(new Error('请输入API密钥'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
  baseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }],
  temperature: [
    {
      type: 'number',
      min: 0,
      max: 2,
      message: '温度值必须在0-2之间',
      trigger: 'blur',
    },
  ],
  maxTokens: [
    {
      type: 'number',
      min: 100,
      max: 10_000,
      message: '最大Token必须在100-10000之间',
      trigger: 'blur',
    },
  ],
  proxyHost: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (formData.proxyEnabled && (!value || value.trim() === '')) {
          callback(new Error('启用代理时，必须填写代理主机地址'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
  proxyPort: [
    {
      validator: (_rule: any, value: number, callback: any) => {
        if (formData.proxyEnabled && !value) {
          callback(new Error('启用代理时，必须填写代理端口'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};

const dialogTitle = computed(() =>
  isEditMode.value ? '编辑模型配置' : '新增模型配置',
);

const filteredConfigs = computed(() => {
  if (!activeFilter.value) return configs.value;
  return configs.value.filter((c) => c.modelType === activeFilter.value);
});

async function loadConfigs() {
  loading.value = true;
  try {
    const data = await getModelConfigListApi();
    configs.value = (data as any)?.data ?? data ?? [];
  } catch {
    ElMessage.error('获取模型配置列表失败，请检查网络！');
    configs.value = [];
  } finally {
    loading.value = false;
  }
}

function showAddDialog() {
  isEditMode.value = false;
  Object.assign(formData, defaultFormData);
  dialogVisible.value = true;
}

function handleEdit(config: ModelConfig) {
  isEditMode.value = true;
  Object.assign(formData, config);
  dialogVisible.value = true;
}

async function handleSubmit() {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
    submitting.value = true;

    if (isEditMode.value) {
      const result = await updateModelConfigApi({ ...formData });
      if (result) {
        ElMessage.success('配置更新成功');
        dialogVisible.value = false;
        await loadConfigs();
      }
    } else {
      const result = await addModelConfigApi({ ...formData });
      if (result) {
        ElMessage.success('配置添加成功');
        dialogVisible.value = false;
        await loadConfigs();
      }
    }
  } catch (error: any) {
    if (error?.message) {
      ElMessage.error(error.message);
    }
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(config: ModelConfig) {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置 "${config.provider} - ${config.modelName}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );

    if (config.id) {
      await deleteModelConfigApi(config.id);
      ElMessage.success('配置删除成功');
      await loadConfigs();
    }
  } catch {
    // cancelled
  }
}

async function handleActivate(id?: number, modelType?: string) {
  if (!id) return;

  if (modelType === 'EMBEDDING') {
    try {
      await ElMessageBox.confirm(
        '您正在更换嵌入模型，此操作风险较高！由于不同模型的向量空间不一致，切换后可能导致所有历史向量数据（含数据源、智能体知识、业务知识）将全部失效且无法检索。确定要执行吗？',
        '切换嵌入模型确认',
        {
          confirmButtonText: '确定继续',
          cancelButtonText: '取消',
          confirmButtonType: 'danger',
        },
      );
    } catch {
      return;
    }
  }

  try {
    activatingId.value = id;
    await activateModelConfigApi(id);
    ElMessage.success('模型启用成功');
    await loadConfigs();
  } catch {
    ElMessage.error('启用过程中发生错误');
  } finally {
    activatingId.value = null;
  }
}

async function handleTestConnection(config: ModelConfig) {
  if (!config.id) return;

  try {
    testingId.value = config.id;
    const result = await testModelConfigConnectionApi(config);
    if (result) {
      ElMessage.success('连接测试成功！');
    }
  } catch {
    ElMessage.error('连接测试过程中发生错误');
  } finally {
    testingId.value = null;
  }
}

function rowAs(row: any): ModelConfig {
  return row as ModelConfig;
}

function getProviderTagType(provider: string) {
  const typeMap: Record<
    string,
    'danger' | 'info' | 'primary' | 'success' | 'warning'
  > = {
    deepseek: 'success',
    qwen: 'warning',
    openai: 'primary',
    siliconflow: 'danger',
    custom: 'info',
  };
  return typeMap[provider] || 'info';
}

onMounted(loadConfigs);
</script>

<template>
  <Page
    title="模型配置管理"
    description="配置和管理AI模型参数，支持多种模型提供商"
  >
    <ElCard class="mb-4">
      <div class="flex items-center justify-between">
        <div class="flex gap-3">
          <ElButton type="primary" @click="showAddDialog"> 新增 </ElButton>
          <ElButton @click="loadConfigs">刷新</ElButton>
        </div>
        <ElSelect
          v-model="activeFilter"
          placeholder="筛选模型类型"
          clearable
          style="width: 300px"
        >
          <ElOption label="全部" value="" />
          <ElOption label="对话模型 (CHAT)" value="CHAT" />
          <ElOption label="嵌入模型 (EMBEDDING)" value="EMBEDDING" />
        </ElSelect>
      </div>
    </ElCard>

    <ElCard v-if="!loading">
      <ElTable :data="filteredConfigs" style="width: 100%" stripe>
        <ElTableColumn prop="id" label="ID" width="80" />
        <ElTableColumn prop="provider" label="提供商" width="120">
          <template #default="scope">
            <ElTag :type="getProviderTagType(scope.row.provider)" size="small">
              {{ scope.row.provider }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="modelName" label="模型名称" width="180" />
        <ElTableColumn prop="modelType" label="模型类型" width="120">
          <template #default="scope">
            <ElTag
              :type="scope.row.modelType === 'CHAT' ? 'primary' : 'success'"
              size="small"
            >
              {{ scope.row.modelType === 'CHAT' ? '对话模型' : '嵌入模型' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn
          prop="baseUrl"
          label="API地址"
          min-width="200"
          show-overflow-tooltip
        />
        <ElTableColumn label="路径配置" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <div
              v-if="scope.row.modelType === 'CHAT' && scope.row.completionsPath"
            >
              <ElTag type="primary" size="small">
                对话: {{ scope.row.completionsPath }}
              </ElTag>
            </div>
            <div
              v-else-if="
                scope.row.modelType === 'EMBEDDING' && scope.row.embeddingsPath
              "
            >
              <ElTag type="success" size="small">
                嵌入: {{ scope.row.embeddingsPath }}
              </ElTag>
            </div>
            <div v-else>
              <span class="text-gray-400 italic">使用默认路径</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="temperature" label="温度" width="100">
          <template #default="scope">
            {{ scope.row.temperature ?? 0.0 }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="maxTokens" label="最大Token" width="120">
          <template #default="scope">
            {{ scope.row.maxTokens ?? 2000 }}
          </template>
        </ElTableColumn>
        <ElTableColumn prop="isActive" label="状态" width="100">
          <template #default="scope">
            <ElTag
              :type="scope.row.isActive ? 'success' : 'info'"
              size="small"
              effect="light"
            >
              {{ scope.row.isActive ? '已启用' : '未启用' }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="320" fixed="right">
          <template #default="scope">
            <div class="flex gap-2">
              <ElButton
                type="info"
                size="small"
                :loading="testingId === scope.row.id"
                @click="handleTestConnection(rowAs(scope.row))"
              >
                连接测试
              </ElButton>
              <ElButton
                v-if="!scope.row.isActive"
                type="success"
                size="small"
                :loading="activatingId === scope.row.id"
                @click="handleActivate(scope.row.id, scope.row.modelType)"
              >
                启用
              </ElButton>
              <ElButton
                type="primary"
                size="small"
                @click="handleEdit(rowAs(scope.row))"
              >
                编辑
              </ElButton>
              <ElButton
                type="danger"
                size="small"
                @click="handleDelete(rowAs(scope.row))"
              >
                删除
              </ElButton>
            </div>
          </template>
        </ElTableColumn>
      </ElTable>
    </ElCard>

    <ElSkeleton v-if="loading" :rows="6" animated />

    <div v-if="!loading && filteredConfigs.length === 0" class="py-16">
      <ElEmpty description="暂无模型配置">
        <ElButton type="primary" @click="showAddDialog">新增</ElButton>
      </ElEmpty>
    </div>

    <ElDialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <ElForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="left"
      >
        <ElFormItem label="提供商" prop="provider">
          <ElSelect
            v-model="formData.provider"
            placeholder="请选择提供商"
            style="width: 100%"
            @change="updateBaseUrlByProvider"
          >
            <ElOption label="DeepSeek" value="deepseek" />
            <ElOption label="Qwen" value="qwen" />
            <ElOption label="OpenAI" value="openai" />
            <ElOption label="Siliconflow" value="siliconflow" />
            <ElOption label="Custom" value="custom" />
          </ElSelect>
        </ElFormItem>

        <ElFormItem label="模型类型" prop="modelType">
          <ElRadioGroup v-model="formData.modelType">
            <ElRadio label="CHAT">对话模型</ElRadio>
            <ElRadio label="EMBEDDING">嵌入模型</ElRadio>
          </ElRadioGroup>
        </ElFormItem>

        <ElFormItem label="模型名称" prop="modelName">
          <ElInput
            v-model="formData.modelName"
            placeholder="例如: gpt-4, deepseek-chat, qwen-plus, text-embedding-v4"
          />
        </ElFormItem>

        <ElFormItem
          label="API密钥"
          prop="apiKey"
          :required="formData.provider !== 'custom'"
        >
          <ElInput
            v-model="formData.apiKey"
            type="password"
            show-password
            :placeholder="
              formData.provider === 'custom' ? '可选填' : '请输入API密钥'
            "
          />
        </ElFormItem>

        <ElFormItem label="Base URL" prop="baseUrl">
          <ElInput
            v-model="formData.baseUrl"
            placeholder="请填写兼容 OpenAI 协议的 Base URL，通常不包含 /v1 后缀"
          />
        </ElFormItem>

        <ElFormItem
          v-if="formData.modelType === 'CHAT'"
          label="Completions路径"
          prop="completionsPath"
        >
          <ElInput
            v-model="formData.completionsPath"
            placeholder="附加到base-url的路径。留空则使用默认值/v1/chat/completions"
          />
        </ElFormItem>

        <ElFormItem
          v-if="formData.modelType === 'EMBEDDING'"
          label="Embeddings路径"
          prop="embeddingsPath"
        >
          <ElInput
            v-model="formData.embeddingsPath"
            placeholder="附加到base-url的路径。留空则使用默认值/v1/embeddings"
          />
        </ElFormItem>

        <ElFormItem label="温度" prop="temperature">
          <ElSlider
            v-model="formData.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            show-input
            show-input-controls
          />
          <div class="text-xs text-gray-500 mt-1">
            建议默认0。控制生成文本的随机性，值越高越随机
          </div>
        </ElFormItem>

        <ElFormItem label="最大Token" prop="maxTokens">
          <ElInputNumber
            v-model="formData.maxTokens"
            :min="100"
            :max="10000"
            :step="100"
            style="width: 100%"
          />
          <div class="text-xs text-gray-500 mt-1">控制生成文本的最大长度</div>
        </ElFormItem>

        <ElDivider content-position="left">网络代理配置</ElDivider>

        <ElFormItem label="启用代理">
          <ElSwitch v-model="formData.proxyEnabled" />
          <span class="text-xs text-gray-500 ml-2">
            如果您的服务器处于受限内网，请开启代理以连接 AI 服务
          </span>
        </ElFormItem>

        <template v-if="formData.proxyEnabled">
          <ElFormItem
            label="代理主机"
            prop="proxyHost"
            :required="formData.proxyEnabled"
          >
            <ElInput
              v-model="formData.proxyHost"
              placeholder="例如: 127.0.0.1 或 proxy.example.com"
            />
          </ElFormItem>

          <ElFormItem
            label="代理端口"
            prop="proxyPort"
            :required="formData.proxyEnabled"
          >
            <ElInputNumber
              v-model="formData.proxyPort"
              :min="1"
              :max="65535"
              controls-position="right"
              style="width: 100%"
            />
          </ElFormItem>

          <ElFormItem label="代理用户名" prop="proxyUsername">
            <ElInput
              v-model="formData.proxyUsername"
              placeholder="可选，代理服务器需要认证时填写"
            />
          </ElFormItem>

          <ElFormItem label="代理密码" prop="proxyPassword">
            <ElInput
              v-model="formData.proxyPassword"
              type="password"
              show-password
              placeholder="可选"
            />
          </ElFormItem>
        </template>
      </ElForm>

      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEditMode ? '更新' : '创建' }}
        </ElButton>
      </template>
    </ElDialog>
  </Page>
</template>
