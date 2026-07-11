import { reactive } from 'vue';

export interface ActionItem {
  name: string;
  color?: string;
  loading?: boolean;
}

interface ActionMenuState {
  show: boolean;
  actions: ActionItem[];
  cancelText: string;
  resolve?: (value: ActionItem | null) => void;
}

/**
 * Vant 4 的 ActionSheet 没有官方命令式 API，这里用一个 reactive 状态 + Promise
 * 模式包一层，让调用方写起来接近原 showActionSheet 的体验。
 */
export function useActionMenu() {
  const state = reactive<ActionMenuState>({
    show: false,
    actions: [],
    cancelText: '取消',
  });

  function open(actions: ActionItem[], cancelText = '取消') {
    return new Promise<ActionItem | null>((resolve) => {
      state.actions = actions;
      state.cancelText = cancelText;
      state.resolve = resolve;
      state.show = true;
    });
  }

  function onSelect(item: ActionItem) {
    state.show = false;
    state.resolve?.(item);
    state.resolve = undefined;
  }

  function onCancel() {
    state.show = false;
    state.resolve?.(null);
    state.resolve = undefined;
  }

  return { state, open, onSelect, onCancel };
}
