/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

interface ImportMetaEnv {
  readonly VITE_PORT?: string;
  readonly VITE_APP_TITLE?: string;
  readonly VITE_GLOB_API_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
