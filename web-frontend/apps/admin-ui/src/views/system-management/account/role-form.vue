<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElButton, ElCheckbox, ElMessage, ElPagination } from 'element-plus';

import type { PrivilegeRole, PrivilegeUserRoleVO } from '#/api';
import {
  getRolePageApi,
  getUserRolesByUserIdApi,
  removeUserRoleBatchApi,
  saveUserRoleBatchApi,
} from '#/api';

defineOptions({ name: 'RoleForm' });

const emit = defineEmits(['success']);

const roleList = ref<PrivilegeRole[]>([]);
const roleTotal = ref(0);
const rolePage = ref(1);
const rolePageSize = ref(10);
const roleLoading = ref(false);
const roleSubmitting = ref(false);
const checkedRoleIds = ref<Set<string>>(new Set());

async function loadRoles() {
  roleLoading.value = true;
  try {
    const res = (await getRolePageApi({
      page: rolePage.value,
      size: rolePageSize.value,
      validState: 1,
    })) as any;
    const pageResult = res?.data || res;
    roleList.value = pageResult?.records || [];
    roleTotal.value = pageResult?.totalRow || 0;
  } catch {
    roleList.value = [];
    roleTotal.value = 0;
  } finally {
    roleLoading.value = false;
  }
}

function handleRolePageChange(val: number) {
  rolePage.value = val;
  loadRoles();
}

function handleRoleSizeChange(val: number) {
  rolePageSize.value = val;
  rolePage.value = 1;
  loadRoles();
}

function handleRoleCheckChange(
  roleId: string,
  checked: string | number | boolean,
) {
  const next = new Set(checkedRoleIds.value);
  if (checked) {
    next.add(roleId);
  } else {
    next.delete(roleId);
  }
  checkedRoleIds.value = next;
}

function handleRoleCheckAll(val: string | number | boolean) {
  if (val) {
    checkedRoleIds.value = new Set(roleList.value.map((r) => r.id!));
  } else {
    checkedRoleIds.value = new Set();
  }
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const user = modalApi.getData<any>();
    if (!user?.id) return;
    roleSubmitting.value = true;
    try {
      const userIds = [user.id];
      const checked = checkedRoleIds.value;
      const allRoleIds = new Set(roleList.value.map((r) => r.id!));

      const toSave: string[] = [];
      const toRemove: string[] = [];

      for (const roleId of checked) {
        toSave.push(roleId);
      }
      for (const roleId of allRoleIds) {
        if (!checked.has(roleId)) {
          toRemove.push(roleId);
        }
      }
      for (const roleId of toSave) {
        await saveUserRoleBatchApi({ roleId, userIds });
      }
      for (const roleId of toRemove) {
        await removeUserRoleBatchApi({ roleId, userIds });
      }
      ElMessage.success('权限分配成功');
      modalApi.close();
      emit('success');
    } catch {
      ElMessage.error('权限分配失败');
    } finally {
      roleSubmitting.value = false;
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const user = modalApi.getData<any>();
      if (user) {
        checkedRoleIds.value = new Set();
        rolePage.value = 1;
        loadRoles();
        if (user.id) {
          getUserRolesByUserIdApi(user.id).then((res: any) => {
            const userRoles = res?.data || res || [];
            checkedRoleIds.value = new Set(
              (userRoles as PrivilegeUserRoleVO[])
                .map((r) => r.roleId!)
                .filter(Boolean),
            );
          }).catch(() => {});
        }
      }
    }
  },
});
</script>

<template>
  <Modal title="分配权限">
    <template #header>
      <div class="flex items-center gap-3">
        <span class="text-base font-semibold text-foreground">分配权限</span>
        <span class="rounded bg-muted px-2.5 py-0.5 text-xs text-muted-foreground">
          {{ modalApi.getData<any>()?.realName || modalApi.getData<any>()?.username }}
        </span>
      </div>
    </template>
    <div v-loading="roleLoading" class="min-h-[120px]">
      <div v-if="roleList.length === 0 && !roleLoading" class="py-12 text-center text-sm text-muted-foreground">
        暂无角色数据
      </div>
      <template v-else>
        <div class="flex items-center justify-between border-b border-border pb-3">
          <ElCheckbox
            :model-value="roleList.length > 0 && checkedRoleIds.size === roleList.length"
            :indeterminate="checkedRoleIds.size > 0 && checkedRoleIds.size < roleList.length"
            @change="handleRoleCheckAll"
          >
            全选
          </ElCheckbox>
          <span class="text-xs text-muted-foreground">
            已选 {{ checkedRoleIds.size }} / {{ roleList.length }}
          </span>
        </div>
        <div class="flex max-h-[320px] flex-col gap-0.5 overflow-y-auto py-1">
          <div
            v-for="role in roleList"
            :key="role.id"
            class="flex cursor-pointer items-center rounded-md px-3 py-2 transition-colors hover:bg-accent"
            :class="{ 'bg-accent': checkedRoleIds.has(role.id!) }"
            @click="handleRoleCheckChange(role.id!, !checkedRoleIds.has(role.id!))"
          >
            <ElCheckbox
              :model-value="checkedRoleIds.has(role.id!)"
              @click.stop
            >
              <span class="text-sm text-foreground">{{ role.name }}</span>
            </ElCheckbox>
          </div>
        </div>
      </template>
    </div>
    <div class="flex justify-end pt-3">
      <ElPagination
        v-model:current-page="rolePage"
        v-model:page-size="rolePageSize"
        :total="roleTotal"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        background
        small
        @current-change="handleRolePageChange"
        @size-change="handleRoleSizeChange"
      />
    </div>
  </Modal>
</template>
