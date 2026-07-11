<script lang="ts" setup>
import type {
  BusinessKnowledgeVO,
  CreateBusinessKnowledgeDTO,
  UpdateBusinessKnowledgeDTO,
} from '#/api/core/businessKnowledge';

import { onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElTable,
  ElTableColumn,
  ElTag,
  ElTooltip,
} from 'element-plus';

import {
  createBusinessKnowledgeApi,
  deleteBusinessKnowledgeApi,
  getBusinessKnowledgeListApi,
  recallBusinessKnowledgeApi,
  refreshAllKnowledgeToVectorStoreApi,
  retryBusinessKnowledgeEmbeddingApi,
  updateBusinessKnowledgeApi,
} from '#/api';

defineOptions({ name: 'BusinessKnowledgeConfig' });

const props = defineProps<{ agentId: number }>();

const businessKnowledgeList = ref<BusinessKnowledgeVO[]>([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const searchKeyword = ref('');
const currentEditId = ref<null | number>(null);
const refreshLoading = ref(false);
const saveLoading = ref(false);
const retryLoadingMap = ref<Record<number, boolean>>({});

const knowledgeForm = ref<BusinessKnowledgeVO>({
  businessTerm: '',
  description: '',
  synonyms: '',
  isRecall: false,
} as BusinessKnowledgeVO);

function openCreateDialog() {
  isEdit.value = false;
  currentEditId.value = null;
  knowledgeForm.value = {
    businessTerm: '',
    description: '',
    synonyms: '',
    isRecall: true,
  } as BusinessKnowledgeVO;
  dialogVisible.value = true;
}

function editKnowledge(knowledge: BusinessKnowledgeVO) {
  isEdit.value = true;
  currentEditId.value = knowledge.id || null;
  knowledgeForm.value = { ...knowledge };
  dialogVisible.value = true;
}

function handleSearch() {
  loadBusinessKnowledge();
}

async function loadBusinessKnowledge() {
  try {
    businessKnowledgeList.value = (await getBusinessKnowledgeListApi(props.agentId, searchKeyword.value || undefined)) ?? [];
  } catch {
    ElMessage.error('加载业务知识列表失败');
  }
}

async function handleDeleteKnowledge(knowledge: BusinessKnowledgeVO) {
  if (!knowledge.id) return;
  try {
    await ElMessageBox.confirm(`确定要删除业务知识 "${knowledge.businessTerm}" 吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    await deleteBusinessKnowledgeApi(knowledge.id);
    ElMessage.success('删除成功');
    await loadBusinessKnowledge();
  } catch {
    // cancelled
  }
}

async function handleToggleRecall(knowledge: BusinessKnowledgeVO, isRecall: boolean) {
  if (!knowledge.id) return;
  try {
    await recallBusinessKnowledgeApi(knowledge.id, isRecall);
    ElMessage.success(`${isRecall ? '设为召回' : '取消召回'}成功`);
    knowledge.isRecall = isRecall;
  } catch {
    ElMessage.error(`${isRecall ? '设为召回' : '取消召回'}失败`);
  }
}

async function handleSave() {
  saveLoading.value = true;
  try {
    if (isEdit.value && currentEditId.value) {
      const updateData: UpdateBusinessKnowledgeDTO = {
        businessTerm: knowledgeForm.value.businessTerm,
        description: knowledgeForm.value.description,
        synonyms: knowledgeForm.value.synonyms,
        agentId: props.agentId,
      };
      await updateBusinessKnowledgeApi(currentEditId.value, updateData);
      ElMessage.success('更新成功');
    } else {
      const createData: CreateBusinessKnowledgeDTO = {
        businessTerm: knowledgeForm.value.businessTerm,
        description: knowledgeForm.value.description,
        synonyms: knowledgeForm.value.synonyms,
        isRecall: true,
        agentId: props.agentId,
      };
      await createBusinessKnowledgeApi(createData);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    await loadBusinessKnowledge();
  } catch {
    ElMessage.error(`${isEdit.value ? '更新' : '创建'}失败`);
  } finally {
    saveLoading.value = false;
  }
}

async function handleRetryEmbedding(knowledge: BusinessKnowledgeVO) {
  if (!knowledge.id) return;
  retryLoadingMap.value[knowledge.id] = true;
  try {
    await retryBusinessKnowledgeEmbeddingApi(knowledge.id);
    ElMessage.success('重试向量化成功');
    await loadBusinessKnowledge();
  } catch {
    ElMessage.error('重试向量化失败');
  } finally {
    retryLoadingMap.value[knowledge.id] = false;
  }
}

async function refreshVectorStore() {
  try {
    await ElMessageBox.confirm('确定要清除现有数据并重新同步到向量库吗？', '确认同步', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonType: 'danger',
    });
    refreshLoading.value = true;
    await refreshAllKnowledgeToVectorStoreApi(props.agentId.toString());
    ElMessage.success('同步到向量库成功');
  } catch {
    // cancelled
  } finally {
    refreshLoading.value = false;
  }
}

function getVectorStatusType(status?: string): 'danger' | 'info' | 'primary' | 'success' | 'warning' {
  switch (status) {
    case 'COMPLETED': return 'success';
    case 'FAILED': return 'danger';
    case 'PENDING': return 'warning';
    case 'PROCESSING': return 'primary';
    default: return 'info';
  }
}

onMounted(loadBusinessKnowledge);
</script>

<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <h3 class="m-0 text-base font-semibold">业务知识配置</h3>
        <p class="mt-1 text-sm text-gray-500">管理业务知识库，包括业务名词、描述和同义词</p>
      </div>
      <div class="flex gap-2">
        <ElInput
          v-model="searchKeyword"
          placeholder="搜索关键词..."
          clearable
          style="width: 200px"
          size="small"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <ElIcon><IconifyIcon icon="lucide:search" /></ElIcon>
          </template>
        </ElInput>
        <ElButton size="small" type="success" :loading="refreshLoading" @click="refreshVectorStore">
          同步到向量库
        </ElButton>
        <ElButton size="small" type="primary" @click="openCreateDialog">
          添加知识
        </ElButton>
      </div>
    </div>

    <ElTable :data="businessKnowledgeList" border stripe empty-text="暂无业务知识数据" class="w-full">
      <ElTableColumn prop="businessTerm" label="业务名词" min-width="120" />
      <ElTableColumn prop="description" label="描述" min-width="150" show-overflow-tooltip />
      <ElTableColumn prop="synonyms" label="同义词" min-width="120" />
      <ElTableColumn label="向量化状态" width="130">
        <template #default="{ row: r }">
          <ElTag :type="getVectorStatusType((r as any).embeddingStatus)" round>
            {{ (r as any).embeddingStatus || '未知' }}
            <ElTooltip v-if="(r as any).embeddingStatus === 'FAILED' && (r as any).errorMsg" :content="(r as any).errorMsg" placement="top">
              <ElIcon style="margin-left: 4px"><IconifyIcon icon="lucide:alert-triangle" /></ElIcon>
            </ElTooltip>
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="是否召回" width="90">
        <template #default="{ row: r }">
          <ElTag :type="(r as any).isRecall ? 'success' : 'info'" round>{{ (r as any).isRecall ? '是' : '否' }}</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="280" fixed="right">
        <template #default="{ row: r }">
          <ElButton size="small" link type="primary" @click="editKnowledge(r as any)">编辑</ElButton>
          <ElButton
            v-if="(r as any).embeddingStatus === 'FAILED'"
            size="small"
            link
            type="info"
            :loading="retryLoadingMap[(r as any).id!]"
            @click="handleRetryEmbedding(r as any)"
          >
            重试
          </ElButton>
          <ElButton
            v-if="(r as any).isRecall"
            size="small"
            link
            type="warning"
            @click="handleToggleRecall(r as any, false)"
          >
            取消召回
          </ElButton>
          <ElButton v-else size="small" link type="success" @click="handleToggleRecall(r as any, true)">
            设为召回
          </ElButton>
          <ElButton size="small" link type="danger" @click="handleDeleteKnowledge(r as any)">删除</ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog v-model="dialogVisible" :title="isEdit ? '编辑业务知识' : '添加业务知识'" width="700" :close-on-click-modal="false">
      <ElForm :model="knowledgeForm" label-width="100px">
        <ElFormItem label="业务名词" required>
          <ElInput v-model="knowledgeForm.businessTerm" placeholder="请输入业务名词" />
        </ElFormItem>
        <ElFormItem label="描述" required>
          <ElInput v-model="knowledgeForm.description" type="textarea" :rows="3" placeholder="请输入业务知识描述" />
        </ElFormItem>
        <ElFormItem label="同义词">
          <ElInput v-model="knowledgeForm.synonyms" type="textarea" :rows="2" placeholder="多个同义词用逗号分隔" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="saveLoading" :disabled="saveLoading" @click="handleSave">
          {{ isEdit ? '更新' : '创建' }}
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>
