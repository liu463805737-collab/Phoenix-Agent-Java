<template>
  <div class="report-html-view-wrapper" :style="wrapperStyle">
    <iframe
      ref="iframeRef"
      class="report-html-iframe"
      :style="iframeStyle"
      sandbox="allow-scripts allow-same-origin"
      title="HTML报告预览"
      @load="onIframeLoad"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref, watch, nextTick } from 'vue';
import { buildReportHtml } from './report-html-template';

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
    const iframeHeight = ref<number | null>(null);

    const wrapperStyle = computed(() => {
      if (iframeHeight.value) {
        return { height: `${iframeHeight.value + 50}px` };
      }
      return {};
    });

    const iframeStyle = computed(() => {
      if (iframeHeight.value) {
        return { height: `${iframeHeight.value}px`, minHeight: '0' };
      }
      return {};
    });

    const loadHtml = () => {
      if (!iframeRef.value) return;

      if (!props.content) {
        iframeRef.value.srcdoc =
          '<html><body style="padding:20px;color:#666;">暂无报告内容</body></html>';
        return;
      }

      const html = buildReportHtml(props.content);
      iframeRef.value.srcdoc = html;
    };

    const adjustHeight = () => {
      const iframe = iframeRef.value;
      if (!iframe) return;
      try {
        const doc = iframe.contentDocument || iframe.contentWindow?.document;
        if (doc) {
          const h = doc.documentElement.scrollHeight;
          iframeHeight.value = h;
        }
      } catch {
        // 跨域限制时不做调整
      }
    };

    const onIframeLoad = () => {
      adjustHeight();
    };

    watch(
      () => props.content,
      () => {
        iframeHeight.value = null;
        nextTick(loadHtml);
      },
      { immediate: true },
    );

    return { iframeRef, wrapperStyle, iframeStyle, onIframeLoad };
  },
});
</script>

<style scoped>
.report-html-view-wrapper {
  width: 100%;
  height: 100%;
  overflow: auto;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: height 0.2s ease;
}

.report-html-iframe {
  display: block;
  width: 100%;
  height: 400px;
  min-height: 200px;
  border: none;
}
</style>
