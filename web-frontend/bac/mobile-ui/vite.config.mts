import { fileURLToPath, URL } from 'node:url';

import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import { defineConfig, loadEnv } from 'vite';
import Components from 'unplugin-vue-components/vite';
import { VantResolver } from '@vant/auto-import-resolver';
import pxToViewport from 'postcss-px-to-viewport-8-plugin';

// 设计稿基准宽度（375）；如果是 750 设计稿，把 viewportWidth 改为 750
const VIEWPORT_WIDTH = 375;

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, fileURLToPath(new URL('.', import.meta.url)), '');
  const port = Number(env.VITE_PORT) || 5999;

  return {
    plugins: [
      vue(),
      vueJsx(),
      Components({
        dts: 'src/components.d.ts',
        resolvers: [VantResolver()],
      }),
    ],
    resolve: {
      alias: {
        '#': fileURLToPath(new URL('./src', import.meta.url)),
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    css: {
      postcss: {
        plugins: [
          pxToViewport({
            unitToConvert: 'px',
            viewportWidth: VIEWPORT_WIDTH,
            unitPrecision: 5,
            propList: ['*'],
            viewportUnit: 'vw',
            fontViewportUnit: 'vw',
            // 跳过 vant 自带的设计稿（375 与本项目一致时无需排除）
            exclude: [/node_modules\/vant/],
            minPixelValue: 1,
            mediaQuery: false,
          }),
        ],
      },
    },
    server: {
      host: true,
      port,
    },
    preview: {
      host: true,
      port,
    },
  };
});
