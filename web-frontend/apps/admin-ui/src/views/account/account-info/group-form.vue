<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElButton, ElMessage, ElTable, ElTableColumn, ElTag } from 'element-plus';

import type { GroupInfo, PlatformAccountGroupInfo } from '#/api';
import {
  createAccountGroupInfoApi,
  deleteAccountGroupInfoByGroupAndAccountApi,
  getAccountGroupInfoByAccountApi,
  getGroupInfoPageApi,
} from '#/api';

defineOptions({ name: 'GroupForm' });

const emit = defineEmits(['success']);

const assignGroupLoading = ref(false);
const allGroups = ref<GroupInfo[]>([]);
const assignedGroupIds = ref<Set<string>>(new Set());
const selectedGroupIds = ref<string[]>([]);

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const accountData = modalApi.getData<any>();
    if (!accountData) return;
    assignGroupLoading.value = true;
    try {
      const accountId = accountData.id ?? '';
      const accountName = accountData.username ?? '';
      const currentIds = [...assignedGroupIds.value];
      const newIds = selectedGroupIds.value;
      const toRemove = currentIds.filter((id) => !newIds.includes(id));
      const toAdd = newIds.filter((id) => !currentIds.includes(id));

      for (const groupId of toRemove) {
        await deleteAccountGroupInfoByGroupAndAccountApi(groupId, accountId);
      }
      for (const groupId of toAdd) {
        const group = allGroups.value.find((g) => g.id === groupId);
        await createAccountGroupInfoApi({
          groupId,
          accountId,
          groupName: group?.name ?? '',
          accountName,
        });
      }
      ElMessage.success('组分配已保存');
      modalApi.close();
      emit('success');
    } catch {
      ElMessage.error('保存失败');
    } finally {
      assignGroupLoading.value = false;
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const accountData = modalApi.getData<any>();
      if (!accountData) return;
      const accountId = accountData.id ?? '';
      assignGroupLoading.value = true;
      allGroups.value = [];
      assignedGroupIds.value = new Set();
      selectedGroupIds.value = [];
      Promise.all([
        getGroupInfoPageApi(1, 9999),
        getAccountGroupInfoByAccountApi(accountId),
      ])
        .then(([groupRes, assignRes]) => {
          const groupPageResult = groupRes?.data || groupRes;
          allGroups.value = groupPageResult?.records || [];
          const assignments: PlatformAccountGroupInfo[] =
            assignRes?.data || assignRes || [];
          assignedGroupIds.value = new Set(
            assignments
              .map((a) => a.groupId)
              .filter((id): id is string => id != null),
          );
          selectedGroupIds.value = [...assignedGroupIds.value];
        })
        .catch(() => {
          allGroups.value = [];
          assignedGroupIds.value = new Set();
          selectedGroupIds.value = [];
        })
        .finally(() => {
          assignGroupLoading.value = false;
        });
    }
  },
});
</script>

<template>
  <Modal title="分配组">
    <div class="mb-4 text-sm text-foreground">
      为用户 <ElTag type="primary">{{ modalApi.getData<any>()?.username }}</ElTag> 分配组
    </div>
    <div v-loading="assignGroupLoading" class="min-h-[120px]">
      <ElTable
        :data="allGroups"
        style="width: 100%"
        stripe
        max-height="360"
        @selection-change="
          (rows) => {
            selectedGroupIds = rows.map((r: any) => r.id).filter(Boolean);
          }
        "
        :row-key="(row: any) => row.id"
      >
        <ElTableColumn type="selection" width="50" :reserve-selection="true" />
        <ElTableColumn prop="name" label="组名称" />
        <ElTableColumn prop="description" label="描述" />
      </ElTable>
    </div>
  </Modal>
</template>
