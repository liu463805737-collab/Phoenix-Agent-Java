<script setup lang="ts">
import { RouterView } from 'vue-router';
</script>

<template>
  <RouterView />
</template>

<style>
:root {
  --pc-bg: #f5f7fb;
  --pc-panel: #fff;
  --pc-border: #e5e7eb;
  --pc-border-soft: #eef0f4;
  --pc-text: #1f2937;
  --pc-text-soft: #6b7280;
  --pc-text-muted: #9ca3af;
  --pc-primary: #2563eb;
  --pc-primary-soft: #eaf1ff;
  --pc-bubble-user: #2f6bff;
  --pc-bubble-bot: #fff;
  --pc-shadow: 0 1px 2px rgb(15 23 42 / 4%);
  --pc-chat-content-width: 880px;
}

html,
body,
#app {
  height: 100%;
  padding: 0;
  margin: 0;
}

body {
  font-family:
    -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei',
    'Segoe UI', Roboto, sans-serif;
  color: var(--pc-text);
  background: var(--pc-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  box-sizing: border-box;
}

button {
  font-family: inherit;
}
</style>
