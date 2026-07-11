<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage, ElButtonGroup, ElRadioGroup, ElRadioButton, ElIcon, ElTooltip, ElButton } from 'element-plus';
import { Document, Download, FullScreen, Close,  } from '@element-plus/icons-vue';

import MarkdownAgentContainer from './markdown/index';
import ReportHtmlView from './ReportHtmlView.vue';

interface Props {
  content: string;
  sessionId?: string;
}

const props = defineProps<Props>();

const reportFormat = ref<'markdown' | 'html'>('markdown');
const showFullscreen = ref(false);

const options = {
  markdownIt: { linkify: true },
  linkAttributes: { attrs: { target: '_blank', rel: 'noopener' } },
};

function downloadMarkdown() {
  const text = props.content;
  if (!text) {
    ElMessage.warning('没有可下载的Markdown报告');
    return;
  }
  const blob = new Blob([text], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `report_${Date.now()}.md`;
  document.body.append(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
  ElMessage.success('Markdown报告下载成功');
}

async function downloadHtml() {
  const text = props.content;
  if (!text) {
    ElMessage.warning('没有可下载的HTML报告');
    return;
  }
  try {
    if (props.sessionId) {
      const { downloadHtmlReportApi } = await import('#/api/core/chat');
      await downloadHtmlReportApi(props.sessionId, text);
      ElMessage.success('HTML报告下载成功');
    } else {
      const { buildReportHtml } = await import('./report-html-template');
      const html = buildReportHtml(text);
      const blob = new Blob([html], { type: 'text/html' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report_${Date.now()}.html`;
      document.body.append(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      ElMessage.success('HTML报告下载成功');
    }
  } catch {
    ElMessage.error('下载HTML报告失败');
  }
}

function openFullscreen() {
  showFullscreen.value = true;
}

function closeFullscreen() {
  showFullscreen.value = false;
}
</script>

<template>
  <div class="report-message">
    <div class="report-message__header">
      <div class="report-message__info">
        <el-icon><Document /></el-icon>
        <span>报告已生成</span>
        <el-radio-group
          v-model="reportFormat"
          size="small"
          class="report-message__format"
        >
          <el-radio-button value="markdown">Markdown</el-radio-button>
          <el-radio-button value="html">HTML</el-radio-button>
        </el-radio-group>
      </div>
      <el-button-group class="report-message__actions">
        <el-button type="primary" @click="downloadMarkdown">
          <el-icon><Download /></el-icon>
          下载Markdown报告
        </el-button>
        <el-button type="success" @click="downloadHtml">
          <el-icon><Download /></el-icon>
          下载HTML报告
        </el-button>
        <el-button type="info" title="全屏查看报告" @click="openFullscreen">
          <el-icon><FullScreen /></el-icon>
          全屏
        </el-button>
      </el-button-group>
    </div>
    <div class="report-message__content">
      <MarkdownAgentContainer
        v-if="reportFormat === 'markdown'"
        class="md-body"
        :content="content"
        :options="options"
      />
      <ReportHtmlView v-else :content="content" />
    </div>
  </div>

  <Teleport to="body">
    <div
      v-if="showFullscreen"
      class="report-fullscreen-overlay"
      @click.self="closeFullscreen"
    >
      <div class="report-fullscreen-container">
        <div class="report-fullscreen-header">
          <span class="report-fullscreen-title">
            {{ reportFormat === 'markdown' ? 'Markdown 报告' : 'HTML 报告' }}
          </span>
          <el-button
            title="关闭"
            type="danger"
            circle
            class="report-fullscreen-close"
            @click="closeFullscreen"
          >
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
        <div class="report-fullscreen-content">
          <MarkdownAgentContainer
            v-if="reportFormat === 'markdown'"
            class="md-body report-fullscreen-body"
            :content="content"
            :options="options"
          />
          <ReportHtmlView
            v-else
            :content="content"
            class="report-fullscreen-body"
          />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.report-message {
  padding: 16px;
  margin-bottom: 16px;
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
}

.report-message__header {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.report-message__info {
  display: flex;
  flex-shrink: 0;
  gap: 12px;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #409eff;
}

.report-message__format {
  margin-left: 8px;
}

.report-message__actions {
  flex-shrink: 0;
}

.report-message__content {
  min-height: 100px;
  margin-top: 16px;
}

.report-fullscreen-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgb(0 0 0 / 70%);
}

.report-fullscreen-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 1200px;
  height: 90vh;
  overflow: hidden;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgb(0 0 0 / 30%);
}

.report-fullscreen-header {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.report-fullscreen-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.report-fullscreen-close {
  flex-shrink: 0;
}

.report-fullscreen-content {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

.report-fullscreen-body {
  min-height: 100%;
}
</style>
