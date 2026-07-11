import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from '@vben/vite-config';

import ElementPlus from 'unplugin-element-plus/vite';

const targetUrl = 'http://localhost:8066';
export default defineConfig(async () => {
  return {
    application: {},
    vite: {
      resolve: {
        alias: {
          '@': fileURLToPath(new URL('./src', import.meta.url)),
          '@phoenix/chat-shared': fileURLToPath(
            new URL('../../packages/chat-shared/src/index.ts', import.meta.url),
          ),
        },
      },
      plugins: [
        ElementPlus({
          format: 'esm',
        }),
      ],
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // mock代理目标地址
            target: targetUrl,
            ws: true,
          },
          '/nl2sql': {
            changeOrigin: true,
            // rewrite: (path) => path.replace(/^\/api/, ''),
            // mock代理目标地址
            target: targetUrl,
            ws: true,
          },
          '/uploads': {
            changeOrigin: true,
            // rewrite: (path) => path.replace(/^\/api/, ''),
            // mock代理目标地址
            target: targetUrl,
            ws: true,
          },
        },
      },
    },
  };
});
