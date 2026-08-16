<script setup lang="ts">
import type { GraphRequest } from '#/api/core/graph';

import { ref } from 'vue';

import { ChatDotRound, Check, Close } from '@element-plus/icons-vue';
import { ElButton, ElIcon, ElInput } from 'element-plus';

defineOptions({ name: 'HumanFeedback' });

const props = defineProps<{
  request: GraphRequest;
}>();

const emit = defineEmits<{
  (e: 'feedback', request: GraphRequest, rejectedPlan: boolean, content: string): void;
}>();

const feedbackInput = ref('');

const submitFeedback = (rejectedPlan: boolean) => {
  const feedbackContent = feedbackInput.value.trim() || 'Accept';
  emit('feedback', props.request, rejectedPlan, feedbackContent);
  feedbackInput.value = '';
};
</script>

<template>
  <div class="human-feedback-area">
    <div class="feedback-header">
      <el-icon><ChatDotRound /></el-icon>
      <span>请对智能体的计划进行评价</span>
    </div>
    <div class="feedback-input">
      <el-input
        v-model="feedbackInput"
        type="textarea"
        :rows="3"
        placeholder="请输入您的反馈意见（可选）..."
        maxlength="500"
        show-word-limit
      />
    </div>
    <div class="feedback-actions">
      <el-button type="success" @click="submitFeedback(false)">
        <el-icon><Check /></el-icon>
        通过计划
      </el-button>
      <el-button type="danger" @click="submitFeedback(true)">
        <el-icon><Close /></el-icon>
        不通过计划
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.human-feedback-area {
  padding: 20px;
  margin: 16px 0;
  background: #f8fbff;
  border: 1px solid #e1f0ff;
  border-radius: 12px;
}

.feedback-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #409eff;
}

.feedback-header .el-icon {
  font-size: 18px;
  color: #409eff;
}

.feedback-input {
  margin-bottom: 16px;
}

.feedback-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .feedback-actions {
    flex-direction: column;
  }

  .feedback-actions .el-button {
    width: 100%;
  }
}
</style>
