import type {
  ComponentRecordType,
  GenerateMenuAndRoutesOptions,
  RouteRecordStringComponent,
} from '@vben/types';

import { generateAccessible } from '@vben/access';
import { preferences } from '@vben/preferences';
import { useAccessStore } from '@vben/stores';

// import { ElMessage } from 'element-plus';

import { getPrivilegeMenusApi } from '#/api';
import type { ModuleTreeVO } from '#/api/core/menu';
import { BasicLayout, IFrameView } from '#/layouts';
// import { $t } from '#/locales';

const forbiddenComponent = () => import('#/views/_core/fallback/forbidden.vue');

/** 将嵌套/平铺菜单统一扁平化，再按 pid 重建树结构 */
function normalizeMenuTree(modules: ModuleTreeVO[]): ModuleTreeVO[] {
  const flatMap = new Map<string, ModuleTreeVO>();

  function collect(list: ModuleTreeVO[]) {
    for (const mod of list) {
      flatMap.set(mod.id, { ...mod, children: [] });
      if (mod.children?.length) {
        collect(mod.children);
      }
    }
  }
  collect(modules);

  const roots: ModuleTreeVO[] = [];
  for (const node of flatMap.values()) {
    if (node.pid && flatMap.has(node.pid)) {
      flatMap.get(node.pid)!.children!.push(node);
    } else {
      roots.push(node);
    }
  }
  return roots;
}

function convertModuleTreeToRoute(
  modules: ModuleTreeVO[],
): RouteRecordStringComponent[] {
  const tree = normalizeMenuTree(modules);

  return tree.map((module) => {
    const route: RouteRecordStringComponent = {
      name: module.sn,
      path: module.url || '',
      component:
        module.type === '1' && module.component ? module.component : '',
      meta: {
        icon: module.image || undefined,
        order: module.orderNo,
        title: module.name,
        hideInMenu: module.isShow === 0,
      },
      children: module.children?.length
        ? convertModuleTreeToRoute(module.children)
        : [],
    };
    return route;
  });
}

/**
 * 添加文件扫描路径，
 * 根据用户角色从后端获取菜单树并生成可访问路由
 * - 后端模式：完全由后端 API 返回的菜单决定路由
 * - 通过 pageMap 将组件路径字符串解析为实际 Vue 组件
 * - 将权限值（pvalues）存入 accessStore 用于按钮级权限控制
 */
async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  const pageMap: ComponentRecordType = {
    ...import.meta.glob('../views/**/*.vue'),
    ...import.meta.glob('../components/**/*.vue'),
  };

  const layoutMap: ComponentRecordType = {
    BasicLayout,
    IFrameView,
  };

  return await generateAccessible(preferences.app.accessMode, {
    ...options,
    fetchMenuListAsync: async () => {
      // ElMessage({
      //   duration: 500,
      //   message: `${$t('common.loadingMenu')}...`,
      // });
      const res = await getPrivilegeMenusApi();

      // 存储权限值（pvalues）到 accessStore，用于前端按钮级权限控制
      if (res.pvalues?.length) {
        const accessStore = useAccessStore();
        accessStore.setAccessCodes(res.pvalues.map((p) => p.name));
      }

      return convertModuleTreeToRoute(res.menus);
    },
    // 可以指定没有权限跳转403页面
    forbiddenComponent,
    // 如果 route.meta.menuVisibleWithForbidden = true
    layoutMap,
    pageMap,
  });
}

export { generateAccess };
