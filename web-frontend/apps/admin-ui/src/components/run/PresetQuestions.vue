<!--
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
-->

<template>
  <div class="preset-questions-wrapper">
    <div class="preset-questions-container">
      <div class="questions-header">
        <el-icon class="header-icon"><ChatLineRound /></el-icon>
        <span class="header-title">预设问题</span>
      </div>

      <div v-if="loading" class="questions-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="activeQuestions.length === 0" class="questions-empty">
        <span>暂无预设问题</span>
      </div>

      <div v-else class="questions-list">
        <div
          v-for="question in activeQuestions"
          :key="question.id"
          class="question-item"
          @click="handleQuestionClick(question)"
        >
          <span class="question-text">{{ question.question }}</span>
          <el-icon class="question-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import type { PropType } from 'vue';
import { ElIcon, ElMessage } from 'element-plus';
import { ChatLineRound, ArrowRight, Loading } from '@element-plus/icons-vue';
import { getPresetQuestionsApi } from '#/api/core/agent';
import type { PresetQuestion } from '#/api/core/agent';

export default defineComponent({
  name: 'PresetQuestions',
  components: {
    ElIcon,
    ChatLineRound,
    ArrowRight,
    Loading,
  },
  props: {
    agentId: {
      type: Number,
      required: true,
    },
    onQuestionClick: {
      type: Function as PropType<(question: string) => void>,
      required: true,
    },
  },
  emits: ['loaded'],
  setup(props, { emit }) {
    const questions = ref<PresetQuestion[]>([]);
    const loading = ref(false);
    const activeQuestions = computed(() => {
      return questions.value.filter((q: any) => q.isActive !== false);
    });

    const loadPresetQuestions = async () => {
      loading.value = true;
      try {
        questions.value = (await getPresetQuestionsApi(
          props.agentId,
        )) as PresetQuestion[];
      } catch {
        ElMessage.error('加载预设问题失败');
      } finally {
        loading.value = false;
        emit('loaded', { hasQuestions: activeQuestions.value.length > 0 });
      }
    };

    const handleQuestionClick = (question: PresetQuestion) => {
      if (props.onQuestionClick) {
        props.onQuestionClick(question.question || '');
      }
    };

    onMounted(() => {
      loadPresetQuestions();
    });

    return {
      questions,
      loading,
      activeQuestions,
      handleQuestionClick,
    };
  },
});
</script>

<style scoped>
.preset-questions-wrapper {
  margin-bottom: 16px;
}

.preset-questions-container {
  padding: 12px 16px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.questions-loading {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  font-size: 13px;
  color: #909399;
}

.questions-loading .el-icon {
  font-size: 16px;
  color: #409eff;
}

.questions-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  font-size: 13px;
  color: #909399;
}

.questions-header {
  display: flex;
  gap: 8px;
  align-items: center;
  padding-bottom: 8px;
  margin-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.header-icon {
  font-size: 16px;
  color: #409eff;
}

.header-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.questions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: calc(3 * (28px + 8px));
  overflow-y: auto;
  padding: 1px;
}

.question-item {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  max-width: calc(50% - 4px);
  padding: 6px 12px;
  cursor: pointer;
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.question-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
  transform: translateY(-1px);
}

.question-item:active {
  transform: translateY(0);
}

.question-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  line-height: 1.4;
  color: #303133;
  white-space: nowrap;
}

.question-item:hover .question-text {
  color: #409eff;
}

.question-arrow {
  flex-shrink: 0;
  font-size: 14px;
  color: #c0c4cc;
  transition: all 0.2s ease;
}

.question-item:hover .question-arrow {
  color: #409eff;
  transform: translateX(2px);
}

@media (max-width: 768px) {
  .question-item {
    max-width: 100%;
  }
}
</style>
