<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import {
  createRoleApi,
  deleteRoleApi,
  getAclsByReleaseIdApi,
  getRoleAclsApi,
  getRolePageApi,
  saveAllAclApi,
  saveModuleAclApi,
  updateRoleApi,
} from '#/api';

import { useVbenForm } from '#/adapter/form';
import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElCard,
  ElCheckbox,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElTree,
} from 'element-plus';
import { useSchema } from './data';
const aclLoading = ref(false);
const aclTreeData = ref<any[]>([]);
const existingAclMap = ref(new Map<string, any>());

const emit = defineEmits(['success']);
const formData = ref<any>();
const getTitle = computed(() => {
  return formData.value?.id
      ? '编辑角色'
      : '创建角色';
});

const [Form, formApi] = useVbenForm({
  layout: 'vertical',
  schema: useSchema(),
  showDefaultActions: false,
});


function resetForm() {
  formApi.resetForm();
  formApi.setValues(formData.value || {});
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (valid) {
      modalApi.lock();
      const data = await formApi.getValues();
      try {
        await (formData.value?.id
            ? updateRoleApi(data)
            : createRoleApi(data));
        modalApi.close();
        emit('success');
      } finally {
        modalApi.lock(false);
      }
    }
  },
  async onOpenChange(isOpen) {
    if (isOpen) {
      debugger
      const data = modalApi.getData<any>();
      debugger;
      if (data) {
        try {
          const [treeRes, aclRes] = await Promise.all([
            getRoleAclsApi(data.id!),
            getAclsByReleaseIdApi(data.id!),
          ]);
          const tree = ((treeRes as any) || []) as any[];
          aclTreeData.value = tree;
          assignLevels(tree);
          const aclList = ((aclRes as any) || []) as any[];
          const map = new Map<string, any>();
          for (const item of aclList) {
            if (item.moduleId) map.set(item.moduleId, item);
          }
          debugger;
          existingAclMap.value = map;
        } catch {
          aclTreeData.value = [];
          ElMessage.error('获取菜单权限失败');
        } finally {
          aclLoading.value = false;
        }
      }
    }
  },
});
function assignLevels(nodes: any[], level = 0) {
  for (const node of nodes) {
    (node as any)._level = level;
    if (node.children?.length) {
      assignLevels(node.children, level + 1);
    }
  }
}
function traversePvalues(nodes: any[]): any[] {
  const result: any[] = [];
  for (const node of nodes) {
    if (node.pvalues?.length) result.push(...node.pvalues);
    if (node.children?.length) result.push(...traversePvalues(node.children));
  }
  return result;
}
const allPvalues = computed(() => traversePvalues(aclTreeData.value));

const headerAllSelected = computed(() => {
  const list = allPvalues.value;
  return list.length > 0 && list.every((pv) => pv.enabled);
});

const headerIndeterminate = computed(() => {
  const list = allPvalues.value;
  return list.some((pv) => pv.enabled) && !headerAllSelected.value;
});
const currentRole = ref<null | any>(null);


function handleHeaderSelectAll(val: string | number | boolean) {
  const checked = !!val;
  function traverse(nodes: any[]) {
    for (const node of nodes) {
      if (node.pvalues?.length) {
        node.pvalues.forEach((pv) => {
          pv.enabled = checked;
        });
        updateNodeState(node);
      }
      if (node.children?.length) {
        traverse(node.children);
      }
    }
  }
  traverse(aclTreeData.value);
  const roleId = currentRole.value?.id;
  if (roleId) {
    saveAllAclApi(roleId, checked);
  }
}
function calcAclState(pvalues: any[]): string {
  let state = 0;
  for (const pv of pvalues) {
    if (pv.enabled) {
      state |= 1 << pv.position;
    }
  }
  return String(state);
}


function updateNodeState(data: any) {
  data.state = calcAclState(data.pvalues);
}

const treeProps = {
  children: 'children',
  label: 'name',
};

function handlePvalueChange(data: any) {
  updateNodeState(data);
  const role = currentRole.value;
  if (!role?.id || !role?.sn) return;
  const aclState = Number(calcAclState(data.pvalues));
  saveModuleAclApi({
    releaseId: role.id,
    releaseSn: role.sn,
    moduleId: data.id,
    moduleSn: data.sn,
    aclState,
    status: aclState > 0 ? 'check' : 'uncheck',
  });
}
</script>

<template>
  <Modal :title="getTitle" class="w-200 ">
    <div v-loading="aclLoading" class="acl-body max-h-[600px] overflow-y-auto">
      <div v-if="aclTreeData.length === 0 && !aclLoading" class="py-8 text-center text-gray-400">
        暂无菜单数据
      </div>
      <div v-else class="[&_.el-tree-node__content]:h-auto">
        <div
            class="grid grid-cols-[220px_100px_1fr] items-center px-2 py-2 pl-6 mb-1 text-xs font-semibold text-gray-500 border-b border-gray-200">
          <div>菜单名称</div>
          <div class="text-left">
            <ElCheckbox
                :model-value="headerAllSelected"
                :indeterminate="headerIndeterminate"
                @change="handleHeaderSelectAll"
            >
              {{ headerAllSelected ? '取消全选' : '全选' }}
            </ElCheckbox>
          </div>
          <div>操作权限</div>
        </div>
        <ElTree
            ref="treeRef"
            :data="aclTreeData"
            :props="treeProps"
            node-key="id"
            :default-expand-all="true"
        >
          <template #default="{ data } = { data: null }">
            <div v-if="data" class="grid grid-cols-[220px_100px_1fr] gap-2 items-center w-full min-w-0">
              <div class="flex gap-2 items-center min-w-0"
                   :style="{ paddingLeft: ((data as any)._level || 0) * 24 + 'px' }">
                <ElIcon class="flex shrink-0 items-center text-base">
                  <IconifyIcon
                      :icon="data.image || (data.type === '0' ? 'lucide:folder' : 'lucide:file-text')"
                  />
                </ElIcon>
                <span class="truncate text-sm font-medium">{{ data.name }}</span>
              </div>
              <div v-if="data.pvalues?.length" class="flex items-center text-xs">
                <ElCheckbox
                    :model-value="data.pvalues.every((pv) => pv.enabled)"
                    :indeterminate="
                      data.pvalues.some((pv) => pv.enabled) &&
                      !data.pvalues.every((pv) => pv.enabled)
                    "
                    @click.stop
                    @change="(val: string | number | boolean) => {
                      data.pvalues.forEach((pv) => { pv.enabled = !!val; });
                      updateNodeState(data);
                      const role = currentRole.value;
                      if (!role?.id || !role?.sn) return;
                      const aclState = Number(calcAclState(data.pvalues));
                      saveModuleAclApi({
                        releaseId: role.id,
                        releaseSn: role.sn,
                        systemSn: '',
                        moduleId: data.id,
                        moduleSn: data.sn,
                        aclState,
                        status: aclState > 0 ? 'check' : 'uncheck',
                      });
                    }"
                >
                  全选
                </ElCheckbox>
              </div>
              <div v-else class="flex items-center text-xs"/>
              <div v-if="data.pvalues?.length" class="flex flex-wrap gap-1 gap-x-3 items-center">
                <ElCheckbox
                    v-for="pv in data.pvalues"
                    :key="pv.pvalueId"
                    :model-value="pv.enabled"
                    @click.stop
                    @change="(val: string | number | boolean) => {
                      pv.enabled = !!val;
                      handlePvalueChange(data);
                    }"
                    class="text-xs"
                >
                  {{ pv.pvalueName || pv.name }}
                </ElCheckbox>
              </div>
            </div>
          </template>
        </ElTree>
      </div>
    </div>
  </Modal>
</template>
