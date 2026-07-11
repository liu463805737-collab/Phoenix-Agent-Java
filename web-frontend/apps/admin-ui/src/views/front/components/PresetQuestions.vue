<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ElMessage, ElPopconfirm, ElPopover, ElIcon, ElTooltip, ElInput, ElLink, ElButton } from 'element-plus';
import { ArrowRight, ChatLineRound, Close, Loading, Plus } from '@element-plus/icons-vue';
import { storeToRefs } from 'pinia';
import { useAgentStore } from '@phoenix/chat-shared';
import { getPresetQuestionsApi, deletePresetQuestionApi, addPresetQuestionApi } from '#/api/front/chat';
import type { PresetQuestion } from '#/api/front/chat';

const emit = defineEmits<{
  select: [question: string];
}>();

const agentStore = useAgentStore();
const { activeAgent } = storeToRefs(agentStore);

const questions = ref<PresetQuestion[]>([]);
const loading = ref(false);
const addPopoverVisible = ref(false);
const newQuestionText = ref('');
const adding = ref(false);

const activeQuestions = computed(() => {
  return questions.value.filter((q: any) => q.isActive !== false);
});

async function loadPresetQuestions() {
  if (!activeAgent.value?.id) return;
  loading.value = true;
  try {
    questions.value = (await getPresetQuestionsApi(
      Number(activeAgent.value.id),
    )) as PresetQuestion[];
  } catch {
    ElMessage.error('加载预设问题失败');
  } finally {
    loading.value = false;
  }
}

async function handleDelete(question: PresetQuestion) {
  if (!question.id) return;
  try {
    await deletePresetQuestionApi(question.id);
    questions.value = questions.value.filter((q) => q.id !== question.id);
    ElMessage.success('删除成功');
  } catch {
    ElMessage.error('删除失败');
  }
}

function handleAddPresetQuestionClick() {
  newQuestionText.value = '';
}

function hideAddPopover() {
  addPopoverVisible.value = false;
}

async function handleAddConfirm() {
  const text = newQuestionText.value.trim();
  if (!text) {
    ElMessage.warning('请输入预设问题');
    return;
  }
  const agentId = Number(activeAgent.value?.id);
  if (!agentId) return;
  adding.value = true;
  try {
    const created = await addPresetQuestionApi({
      agentId,
      question: text,
      sortOrder: questions.value.length + 1,
    });
    questions.value.push(created);
    hideAddPopover();
    ElMessage.success('添加成功');
  } catch {
    ElMessage.error('添加失败');
  } finally {
    adding.value = false;
  }
}

function handleClick(question: PresetQuestion) {
  emit('select', question.question || '');
}

watch(
  () => activeAgent.value?.id,
  (id) => {
    if (id) {
      loadPresetQuestions();
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="preset-questions">
    <div class="preset-questions__header flex gap-2">
      <el-icon class="preset-questions__header-icon"><ChatLineRound /></el-icon>
      <span class="preset-questions__header-title">预设问题</span>
    </div>

    <div v-if="loading" class="preset-questions__loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="activeQuestions.length === 0" class="preset-questions__empty flex-items-center">
      暂无预设问题，
      <el-popover
        v-model:visible="addPopoverVisible"
        :width="400"
        placement="top"
        trigger="click"
        :hide-after="0"
        @show="handleAddPresetQuestionClick"
      >
        <template #reference>
          <el-link type="primary">添加</el-link>
        </template>
        <div class="add-preset-popover">
          <el-input
            v-model="newQuestionText"
            type="textarea"
            :rows="3"
            placeholder="请输入预设问题"
            maxlength="500"
            show-word-limit
          />
          <div class="add-preset-popover__actions">
            <el-button size="small" @click="hideAddPopover">取消</el-button>
            <el-button size="small" type="primary" :loading="adding" @click="handleAddConfirm">确定</el-button>
          </div>
        </div>
      </el-popover>
    </div>

    <div v-else class="preset-questions__list">
      <div
        v-for="question in activeQuestions"
        :key="question.id"
        class="preset-questions__item"
        @click="handleClick(question)"
      >
        <span class="preset-questions__item-text">{{ question.question }}</span>
        <el-popconfirm
          title="确定删除该预设问题？"
          confirm-button-text="确定"
          cancel-button-text="取消"
          @confirm.stop="handleDelete(question)"
        >
          <template #reference>
            <el-icon
              class="preset-questions__item-delete"
              @click.stop
            >
              <Close />
            </el-icon>
          </template>
        </el-popconfirm>
        <el-icon class="preset-questions__item-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="flex items-center">
        <el-popover
          v-model:visible="addPopoverVisible"
          :width="400"
          placement="top"
          trigger="click"
          :hide-after="0"
          @show="handleAddPresetQuestionClick"
        >
          <template #reference>
            <el-link type="primary">
              添加
            </el-link>
          </template>
          <div class="add-preset-popover">
            <el-input
              v-model="newQuestionText"
              type="textarea"
              :rows="3"
              placeholder="请输入预设问题"
              maxlength="500"
              show-word-limit
            />
            <div class="add-preset-popover__actions">
              <el-button size="small" @click="hideAddPopover">取消</el-button>
              <el-button size="small" type="primary" :loading="adding" @click="handleAddConfirm">确定</el-button>
            </div>
          </div>
        </el-popover>
      </div>
    </div>
  </div>
</template>

<style scoped>
.preset-questions {
  padding: 12px 16px;
  background: hsl(var(--card));
  border: 1px solid hsl(var(--border));
  border-radius: 10px;
}

.preset-questions__header {
  display: flex;
  align-items: center;
  padding-bottom: 8px;
  margin-bottom: 10px;
  border-bottom: 1px solid hsl(var(--border));
}

.preset-questions__header-icon {
  font-size: 16px;
  color: hsl(var(--primary));
}

.preset-questions__header-title {
  font-size: 14px;
  font-weight: 500;
  color: hsl(var(--foreground));
}

.preset-questions__loading {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  font-size: 13px;
  color: hsl(var(--muted-foreground));
}

.preset-questions__loading .el-icon {
  font-size: 16px;
  color: hsl(var(--primary));
}

.preset-questions__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  font-size: 13px;
  color: hsl(var(--muted-foreground));
}

.preset-questions__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: calc(3 * (28px + 8px));
  overflow-y: auto;
  padding: 8px 0;
}

.preset-questions__item {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  max-width: calc(50% - 4px);
  padding: 6px 12px;
  cursor: pointer;
  background: hsl(var(--accent));
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  transition: all 0.2s ease;
}

.preset-questions__item:hover {
  background: hsl(var(--primary) / 8%);
  border-color: hsl(var(--primary));
  transform: translateY(0);
}

.preset-questions__item:active {
  transform: translateY(0);
}

.preset-questions__item-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  line-height: 1.4;
  color: hsl(var(--foreground));
  white-space: nowrap;
}

.preset-questions__item:hover .preset-questions__item-text {
  color: hsl(var(--primary));
}

.preset-questions__item-delete {
  flex-shrink: 0;
  display: none;
  font-size: 16px;
  color: hsl(var(--destructive) / 70%);
  background: hsl(var(--destructive) / 20%);
  border-radius: 50%;
  cursor: pointer;
  transition: color 0.2s ease;
  padding: 2px;
  position: absolute;
  right: -8px;
  top: -8px;
}

.preset-questions__item-delete:hover {
  color: hsl(var(--destructive));
  background: hsl(var(--destructive) / 40%);
}

.preset-questions__item:hover .preset-questions__item-delete {
  display: inline-flex;
}

.preset-questions__item-arrow {
  flex-shrink: 0;
  font-size: 14px;
  color: hsl(var(--muted-foreground));
  transition: all 0.2s ease;
}

.preset-questions__item:hover .preset-questions__item-arrow {
  color: hsl(var(--primary));
  transform: translateX(2px);
}

.add-preset-popover {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.add-preset-popover__actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 768px) {
  .preset-questions__item {
    max-width: 100%;
  }
}
</style>
