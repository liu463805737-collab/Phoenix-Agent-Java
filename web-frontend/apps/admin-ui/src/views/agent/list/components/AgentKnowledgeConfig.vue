<script lang="ts" setup>
import type { AgentKnowledge } from '#/api/core/agentKnowledge';

import { onMounted, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  ElAlert,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElSelect,
  ElTable,
  ElTableColumn,
  ElTag,
  ElTooltip,
  ElUpload,
} from 'element-plus';

import { requestClient } from '#/api/request';

import {
  deleteAgentKnowledgeApi,
  queryAgentKnowledgePageApi,
  retryKnowledgeEmbeddingApi,
  updateAgentKnowledgeApi,
  updateKnowledgeRecallStatusApi,
} from '#/api';

defineOptions({ name: 'AgentKnowledgeConfig' });

const props = defineProps<{ agentId: number }>();

const knowledgeList = ref<AgentKnowledge[]>([]);
const loading = ref(false);
const searchKeyword = ref('');

// 添加/编辑知识弹窗
const dialogVisible = ref(false);
const isEdit = ref(false);
const currentEditId = ref<null | number>(null);
const saveLoading = ref(false);
const fileList = ref<{ name: string; size: number; raw: File }[]>([]);
const knowledgeForm = ref<AgentKnowledge & { question?: string; answer?: string; file?: File; splitterType?: string }>({
  agentId: props.agentId,
  title: '',
  content: '',
  type: 'DOCUMENT',
  isRecall: true,
  question: '',
  answer: '',
  splitterType: 'recursive',
});

function openCreateDialog() {
  isEdit.value = false;
  currentEditId.value = null;
  fileList.value = [];
  knowledgeForm.value = {
    agentId: props.agentId,
    title: '',
    content: '',
    type: 'DOCUMENT',
    isRecall: true,
    question: '',
    answer: '',
    splitterType: 'recursive',
  };
  dialogVisible.value = true;
}

function closeDialog() {
  dialogVisible.value = false;
}

function handleTypeChange() {
  knowledgeForm.value.content = '';
  knowledgeForm.value.question = '';
  knowledgeForm.value.answer = '';
  fileList.value = [];
}

function handleFileChange(uploadFile: any) {
  if (!uploadFile?.raw) return;
  fileList.value = [{ name: uploadFile.name, size: uploadFile.size, raw: uploadFile.raw }];
  knowledgeForm.value.file = uploadFile.raw;
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${Math.round((bytes / Math.pow(k, i)) * 100) / 100} ${sizes[i]}`;
}

async function saveKnowledge() {
  const form = knowledgeForm.value;
  if (!form.title || !form.title.trim()) {
    ElMessage.warning('请输入知识标题');
    return;
  }
  if (form.type === 'DOCUMENT') {
    if (!isEdit.value && !form.file && fileList.value.length === 0) {
      ElMessage.warning('请上传文件');
      return;
    }
  } else if (form.type === 'QA' || form.type === 'FAQ') {
    if (!form.question || !form.question.trim()) {
      ElMessage.warning('请输入问题');
      return;
    }
    if (!form.answer || !form.answer.trim()) {
      ElMessage.warning('请输入答案');
      return;
    }
  }

  saveLoading.value = true;
  try {
    if (isEdit.value && currentEditId.value) {
      await updateAgentKnowledgeApi(currentEditId.value, {
        title: form.title,
        content: form.answer || form.content,
        question: form.question,
        type: form.type,
        isRecall: form.isRecall,
      } as AgentKnowledge);
      ElMessage.success('更新成功');
    } else {
      const fd = new FormData();
      fd.append('agentId', String(props.agentId));
      fd.append('title', form.title);
      fd.append('type', form.type || 'DOCUMENT');
      fd.append('isRecall', form.isRecall ? '1' : '0');
      if (form.type === 'DOCUMENT' && form.file) {
        fd.append('file', form.file);
        if (form.splitterType) fd.append('splitterType', form.splitterType);
      } else if (form.type === 'QA' || form.type === 'FAQ') {
        fd.append('content', form.answer || '');
        if (form.question) fd.append('question', form.question);
      }
      const response = await requestClient.post<{ success: boolean; message?: string }>(
        '/api/agent-knowledge/create',
        fd,
        {
          headers: { 'Content-Type': 'multipart/form-data' },
          responseReturn: 'body',
        },
      );
      if (!response.success) {
        ElMessage.error(response.message || '创建失败');
        return;
      }
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    await loadKnowledgeList();
  } catch (e: any) {
    ElMessage.error(e?.message || `${isEdit.value ? '更新' : '创建'}失败`);
  } finally {
    saveLoading.value = false;
  }
}

async function loadKnowledgeList() {
  loading.value = true;
  try {
    const result = await queryAgentKnowledgePageApi({
      agentId: props.agentId,
      title: searchKeyword.value || undefined,
      pageNum: 1,
      pageSize: 9999,
    });
    knowledgeList.value = result.data ?? [];
  } catch {
    ElMessage.error('加载知识列表失败');
    knowledgeList.value = [];
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  loadKnowledgeList();
}

async function handleToggleRecall(knowledge: AgentKnowledge) {
  if (!knowledge.id) return;
  const newStatus = !knowledge.isRecall;
  const actionName = newStatus ? '召回' : '取消召回';
  try {
    await ElMessageBox.confirm(
      `确定要${actionName}知识 "${knowledge.title}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await updateKnowledgeRecallStatusApi(knowledge.id, newStatus);
    knowledge.isRecall = newStatus;
    ElMessage.success(`${actionName}成功`);
  } catch {
    // cancelled
  }
}

async function handleRetry(knowledge: AgentKnowledge) {
  if (!knowledge.id) return;
  try {
    await retryKnowledgeEmbeddingApi(knowledge.id);
    ElMessage.success('重试请求已发送');
    await loadKnowledgeList();
  } catch {
    ElMessage.error('重试失败');
  }
}

async function handleDelete(knowledge: AgentKnowledge) {
  if (!knowledge.id) return;
  try {
    await ElMessageBox.confirm(
      `确定要删除知识 "${knowledge.title}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        confirmButtonType: 'danger',
      },
    );
    await deleteAgentKnowledgeApi(knowledge.id);
    ElMessage.success('删除成功');
    await loadKnowledgeList();
  } catch {
    // cancelled
  }
}

onMounted(loadKnowledgeList);
</script>

<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div>
        <h3 class="m-0 text-base font-semibold">智能体知识配置</h3>
        <p class="mt-1 text-sm text-gray-500">管理用于增强智能体能力的知识源</p>
      </div>
      <div class="flex gap-2">
        <ElInput
          v-model="searchKeyword"
          placeholder="搜索知识标题..."
          clearable
          style="width: 250px"
          size="small"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <ElIcon><IconifyIcon icon="lucide:search" /></ElIcon>
          </template>
        </ElInput>
        <ElButton size="small" type="primary" @click="openCreateDialog">添加知识</ElButton>
      </div>
    </div>

    <ElTable
      :data="knowledgeList"
      border
      stripe
      v-loading="loading"
      empty-text="暂无知识数据"
      class="w-full"
    >
      <ElTableColumn prop="title" label="标题" min-width="150" />
      <ElTableColumn label="类型" width="100">
        <template #default="{ row: r }">
          <span v-if="(r as any).type === 'DOCUMENT'">文档</span>
          <span v-else-if="(r as any).type === 'QA'">问答对</span>
          <span v-else-if="(r as any).type === 'FAQ'">常见问题</span>
          <span v-else>{{ (r as any).type }}</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="处理状态" width="130">
        <template #default="{ row: r }">
          <ElTag
            v-if="(r as any).embeddingStatus === 'COMPLETED'"
            type="success"
            round
          >
            {{ (r as any).embeddingStatus }}
          </ElTag>
          <ElTag
            v-else-if="(r as any).embeddingStatus === 'PROCESSING'"
            type="primary"
            round
          >
            {{ (r as any).embeddingStatus }}
          </ElTag>
          <ElTag
            v-else-if="(r as any).embeddingStatus === 'FAILED'"
            type="danger"
            round
          >
            <ElTooltip
              v-if="(r as any).errorMsg"
              :content="(r as any).errorMsg"
              placement="top"
            >
              <span>{{ (r as any).embeddingStatus }}</span>
            </ElTooltip>
            <span v-else>{{ (r as any).embeddingStatus }}</span>
          </ElTag>
          <ElTag v-else type="info" round>
            {{ (r as any).embeddingStatus }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="召回状态" width="100">
        <template #default="{ row: r }">
          <ElTag :type="(r as any).isRecall ? 'success' : 'info'" round>
            {{ (r as any).isRecall ? '已召回' : '未召回' }}
          </ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="280" fixed="right">
        <template #default="{ row: r }">
          <ElButton
            v-if="(r as any).embeddingStatus === 'FAILED'"
            size="small"
            link
            type="info"
            @click="handleRetry(r as any)"
          >
            重试
          </ElButton>
          <ElButton
            v-if="(r as any).isRecall"
            size="small"
            link
            type="warning"
            @click="handleToggleRecall(r as any)"
          >
            取消召回
          </ElButton>
          <ElButton
            v-else
            size="small"
            link
            type="success"
            @click="handleToggleRecall(r as any)"
          >
            召回
          </ElButton>
          <ElButton
            size="small"
            link
            type="danger"
            @click="handleDelete(r as any)"
          >
            删除
          </ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog v-model="dialogVisible" :title="isEdit ? '编辑知识' : '添加新知识'" width="800" :close-on-click-modal="false">
      <ElForm :model="knowledgeForm" label-width="100px">
        <ElFormItem label="知识类型" prop="type" required>
          <ElSelect v-model="knowledgeForm.type" placeholder="请选择知识类型" @change="handleTypeChange" :disabled="isEdit" style="width: 100%">
            <ElOption label="文档 (文件上传)" value="DOCUMENT" />
            <ElOption label="问答对 (Q&A)" value="QA" />
            <ElOption label="常见问题 (FAQ)" value="FAQ" />
          </ElSelect>
        </ElFormItem>

        <ElFormItem v-if="knowledgeForm.type === 'QA'">
          <ElAlert type="info" :closable="false" show-icon>
            <template #title>
              <div style="line-height: 1.6">请录入具体的分析需求作为问题，并在答案中写出详细的思考步骤与数据查找逻辑（而非直接给结果），以此教会 AI 如何拆解任务。</div>
            </template>
          </ElAlert>
        </ElFormItem>
        <ElFormItem v-if="knowledgeForm.type === 'FAQ'">
          <ElAlert type="info" :closable="false" show-icon>
            <template #title>
              <div style="line-height: 1.6">请针对特定的业务术语、指标口径或常见歧义进行提问和定义（例如：什么是有效日活），以此统一 AI 的判断标准。</div>
            </template>
          </ElAlert>
        </ElFormItem>
        <ElFormItem v-if="knowledgeForm.type === 'DOCUMENT'">
          <ElAlert type="info" :closable="false" show-icon>
            <template #title>
              <div style="line-height: 1.6">请上传完整的数据库表结构、码表映射字典或业务背景说明，供 AI 在分析时检索字段含义和数据关系。</div>
            </template>
          </ElAlert>
        </ElFormItem>

        <ElFormItem label="知识标题" prop="title" required>
          <ElInput v-model="knowledgeForm.title" placeholder="为这份知识起一个易于识别的名称" />
        </ElFormItem>

        <ElFormItem v-if="knowledgeForm.type === 'DOCUMENT' && !isEdit" label="分块策略" prop="splitterType">
          <ElSelect v-model="knowledgeForm.splitterType" placeholder="请选择分块策略" style="width: 100%">
            <ElOption label="Token 分块" value="token" />
            <ElOption label="递归分块" value="recursive" />
            <ElOption label="句子分块" value="sentence" />
            <ElOption label="段落分块" value="paragraph" />
            <ElOption label="语义分块" value="semantic" />
          </ElSelect>
          <div style="margin-top: 8px; font-size: 12px; color: #909399">
            <div v-if="knowledgeForm.splitterType === 'token'">⚡ 速度最快，按固定 token 数切分，适合代码和日志</div>
            <div v-else-if="knowledgeForm.splitterType === 'recursive'">📚 平衡之选，保留文档结构（段落、章节），适合技术文档</div>
            <div v-else-if="knowledgeForm.splitterType === 'sentence'">✨ 保证句子完整性，语义不被截断，适合新闻和文章</div>
            <div v-else-if="knowledgeForm.splitterType === 'paragraph'">📝 按自然段落分块，保留段落完整性，适合博客、书籍等</div>
            <div v-else-if="knowledgeForm.splitterType === 'semantic'">🧠 基于语义相似度智能分块，自动识别主题边界，适合论文和长文（会产生 embedding API 调用成本）</div>
          </div>
        </ElFormItem>

        <ElFormItem v-if="knowledgeForm.type === 'DOCUMENT'" label="上传文件" required>
          <div v-if="!isEdit" style="width: 100%">
            <ElUpload :auto-upload="false" :limit="1" accept=".pdf,.docx,.txt,.md" :on-change="handleFileChange" :file-list="fileList as any" :on-remove="() => (fileList = [])" drag>
              <ElIcon class="el-icon--upload"><IconifyIcon icon="lucide:upload" /></ElIcon>
              <div class="el-upload__text">将文件拖到此处，或<em>点击选择文件</em></div>
              <template #tip>
                <div class="el-upload__tip">支持 PDF, DOCX, TXT, MD 等格式</div>
                <div v-if="fileList.length > 0 && fileList[0]" class="el-upload__tip" style="color: #409eff">文件大小: {{ formatFileSize(fileList[0].size) }}</div>
              </template>
            </ElUpload>
          </div>
          <div v-else>
            <ElAlert type="info" :closable="false" show-icon title="文档类型知识不支持修改文件内容，如需修改请删除后重新创建" />
          </div>
        </ElFormItem>

        <template v-if="knowledgeForm.type === 'QA' || knowledgeForm.type === 'FAQ'">
          <ElFormItem label="问题" prop="question" required>
            <ElInput v-model="knowledgeForm.question" type="textarea" :rows="2" placeholder="输入用户可能会问的问题..." />
          </ElFormItem>
          <ElFormItem label="答案" prop="answer" required>
            <ElInput v-model="knowledgeForm.answer" type="textarea" :rows="5" placeholder="输入标准答案..." />
          </ElFormItem>
        </template>
      </ElForm>

      <template #footer>
        <ElButton @click="closeDialog">取消</ElButton>
        <ElButton type="primary" :loading="saveLoading" @click="saveKnowledge">{{ isEdit ? '更新' : '添加并处理' }}</ElButton>
      </template>
    </ElDialog>
  </div>
</template>
