<template>
  <div class="report-html-view-wrapper">
    <iframe
      ref="iframeRef"
      class="report-html-iframe"
      sandbox="allow-scripts allow-same-origin"
      title="HTML报告预览"
      @load="onIframeLoad"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, watch, nextTick } from 'vue';
import { buildReportHtml } from './charts/report-html-template';

export default defineComponent({
  name: 'ReportHtmlView',
  props: {
    content: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    const iframeRef = ref<HTMLIFrameElement | null>(null);

    const resizeIframe = () => {
      const iframe = iframeRef.value;
      if (!iframe || !iframe.contentWindow) return;
      try {
        const doc = iframe.contentWindow.document;
        const height = Math.max(
          doc.documentElement.scrollHeight,
          doc.body?.scrollHeight || 0,
        );
        if (height > 0) {
          iframe.style.height = `${height}px`;
        }
      } catch {
        // cross-origin error, ignore
      }
    };

    const onIframeLoad = () => {
      nextTick(() => {
        resizeIframe();
        setTimeout(resizeIframe, 1000);
      });
    };

    const loadHtml = () => {
      const iframe = iframeRef.value;
      if (!iframe) return;

      iframe.style.height = '';

      if (!props.content) {
        iframe.srcdoc =
          '<html><body style="padding:20px;color:#666;">暂无报告内容</body></html>';
        return;
      }

      const html = buildReportHtml(props.content);
      iframe.srcdoc = html;
    };

    watch(
      () => props.content,
      () => {
        nextTick(loadHtml);
      },
      { immediate: true },
    );

    return { iframeRef, onIframeLoad };
  },
});
</script>

<style scoped>
.report-html-view-wrapper {
  width: 100%;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.report-html-iframe {
  display: block;
  width: 100%;
  height: 200px;
  border: none;
}
</style>
