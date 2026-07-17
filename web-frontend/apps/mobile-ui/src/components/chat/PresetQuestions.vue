<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant';
import { storeToRefs } from 'pinia';
import { useAgentStore } from '@phoenix/chat-shared';
import {
  getPresetQuestionsApi,
  addPresetQuestionApi,
  deletePresetQuestionApi,
} from '../../services/chat';
import type { PresetQuestion } from '../../services/chat';

const emit = defineEmits<{
  select: [question: string];
}>();

const agentStore = useAgentStore();
const { activeAgent } = storeToRefs(agentStore);

const questions = ref<PresetQuestion[]>([]);
const loading = ref(false);
const addDialogShow = ref(false);
const addDialogText = ref('');
const adding = ref(false);

const activeQuestions = computed(() => {
  return questions.value.filter((q: any) => q.isActive !== false);
});

async function loadPresetQuestions() {
  if (!activeAgent.value?.id) return;
  loading.value = true;
  try {
    questions.value = (await getPresetQuestionsApi(
      String(activeAgent.value.id),
    )) as PresetQuestion[];
  } catch {
    showFailToast('加载预设问题失败');
  } finally {
    loading.value = false;
  }
}

async function handleDelete(question: PresetQuestion) {
  if (!question.id) return;
  try {
    await showConfirmDialog({ title: '提示', message: '确定删除该预设问题？' });
    await deletePresetQuestionApi(question.id);
    questions.value = questions.value.filter((q) => q.id !== question.id);
    showSuccessToast('删除成功');
  } catch {
    // cancelled or error
  }
}

function handleAddOpen() {
  addDialogText.value = '';
  addDialogShow.value = true;
}

async function handleAddConfirm() {
  const text = addDialogText.value.trim();
  if (!text) {
    showFailToast('请输入预设问题');
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
    addDialogShow.value = false;
    showSuccessToast('添加成功');
  } catch {
    showFailToast('添加失败');
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
    <div class="preset-questions__header">
      <van-icon name="chat-o" class="preset-questions__header-icon" />
      <span class="preset-questions__header-title">预设问题</span>
    </div>

    <div v-if="loading" class="preset-questions__loading">
      <van-loading type="spinner" size="16" />
      <span>加载中...</span>
    </div>

    <template v-else>
      <button
        type="button"
        class="preset-questions__add-btn"
        @click="handleAddOpen"
      >
        <van-icon name="plus" size="14" />
        <span>添加预设问题</span>
      </button>

      <div v-if="activeQuestions.length === 0" class="preset-questions__empty">
        暂无预设问题
      </div>

      <div v-else class="preset-questions__list">
        <div
          v-for="question in activeQuestions"
          :key="question.id"
          class="preset-questions__item"
          @click="handleClick(question)"
        >
          <span class="preset-questions__item-text">{{ question.question }}</span>
          <van-icon
            name="cross"
            class="preset-questions__item-delete"
            @click.stop="handleDelete(question)"
          />
          <van-icon name="arrow" class="preset-questions__item-arrow" />
        </div>
      </div>
    </template>

    <van-dialog
      v-model:show="addDialogShow"
      title="添加预设问题"
      show-cancel-button
      :confirm-button-loading="adding"
      @confirm="handleAddConfirm"
      @cancel="addDialogText = ''"
    >
      <div class="add-dialog-body">
        <van-field
          v-model="addDialogText"
          type="textarea"
          placeholder="请输入预设问题"
          :maxlength="500"
          rows="3"
          autosize
          show-word-limit
        />
      </div>
    </van-dialog>
  </div>
</template>

<style scoped>
.preset-questions {
  width: 100%;
  max-width: 340px;
  margin-top: 14px;
}

.preset-questions__header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 8px;
  margin-bottom: 10px;
  border-bottom: 1px solid var(--m-border);
}

.preset-questions__header-icon {
  font-size: 16px;
  color: var(--m-brand-primary);
}

.preset-questions__header-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--m-text-primary);
}

.preset-questions__loading {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  font-size: 13px;
  color: var(--m-text-soft);
}

.preset-questions__add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 100%;
  padding: 8px 0;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--m-brand-primary);
  cursor: pointer;
  background: var(--m-brand-primary-soft);
  border: 1px dashed var(--m-brand-primary);
  border-radius: 10px;
  transition: opacity 0.15s ease;
}

.preset-questions__add-btn:active {
  opacity: 0.7;
}

.preset-questions__empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  font-size: 13px;
  color: var(--m-text-soft);
}

.preset-questions__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 240px;
  overflow-y: auto;
}

.preset-questions__item {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 12px 14px;
  cursor: pointer;
  background: var(--m-bg-elevated);
  border: 1px solid var(--m-border);
  border-radius: 14px;
  transition:
    background 0.15s ease,
    border-color 0.15s ease;
}

.preset-questions__item:active {
  background: var(--m-brand-primary-soft);
  border-color: rgb(47 107 255 / 40%);
}

.preset-questions__item-text {
  flex: 1;
  overflow: hidden;
  font-size: 14px;
  line-height: 1.4;
  color: var(--m-text-regular);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preset-questions__item-delete {
  flex-shrink: 0;
  display: none;
  font-size: 14px;
  padding: 2px;
  color: var(--m-text-soft);
  border-radius: 50%;
}

.preset-questions__item:hover .preset-questions__item-delete,
.preset-questions__item:active .preset-questions__item-delete {
  display: inline-flex;
}

.preset-questions__item-arrow {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--m-text-soft);
  transition: transform 0.15s ease;
}

.preset-questions__item:active .preset-questions__item-arrow {
  color: var(--m-brand-primary);
  transform: translateX(2px);
}

.add-dialog-body {
  padding: 16px 20px;
}
</style>
