<script lang="ts" setup>
import { nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage, ElTable, ElTableColumn, ElTag } from 'element-plus';

import type { GroupAgentAssignment, GroupAgentInfo } from '#/api';
import {
  createGroupAgentInfoApi,
  deleteGroupAgentInfoByGroupAndAgentApi,
  getGroupAgentInfoByGroupApi,
  getGroupAgentInfoListApi,
} from '#/api';

defineOptions({ name: 'AssignAgentForm' });

const emit = defineEmits(['success']);

const assignLoading = ref(false);
const allAgents = ref<GroupAgentInfo[]>([]);
const selectedAgentIds = ref<string[]>([]);
const assignedAgentIds = ref<Set<string>>(new Set());
const agentTableRef = ref();

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const groupData = modalApi.getData<any>();
    if (!groupData) return;
    assignLoading.value = true;
    try {
      const groupId = groupData.id;
      const currentIds = [...assignedAgentIds.value];
      const newIds = selectedAgentIds.value;
      const toRemove = currentIds.filter((id) => !newIds.includes(id));
      const toAdd = newIds.filter((id) => !currentIds.includes(id));
      for (const agentId of toRemove) {
        await deleteGroupAgentInfoByGroupAndAgentApi(groupId, agentId);
      }
      for (const agentId of toAdd) {
        await createGroupAgentInfoApi({ groupId, agentId });
      }
      ElMessage.success('智能体分配成功');
      modalApi.close();
      emit('success');
    } catch {
      ElMessage.error('智能体分配失败');
    } finally {
      assignLoading.value = false;
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const groupData = modalApi.getData<any>();
      if (!groupData) return;
      assignLoading.value = true;
      Promise.all([
        getGroupAgentInfoListApi({ status: 'published' }),
        getGroupAgentInfoByGroupApi(groupData.id),
      ])
        .then(([agentRes, assignRes]) => {
          allAgents.value = (agentRes as any)?.data || agentRes || [];
          const assignments: GroupAgentAssignment[] =
            (assignRes as any)?.data || assignRes || [];
          assignedAgentIds.value = new Set(
            assignments
              .map((a) => a.agentId)
              .filter((id): id is string => id != null),
          );
          selectedAgentIds.value = [...assignedAgentIds.value];
          nextTick(() => {
            if (agentTableRef.value) {
              agentTableRef.value.clearSelection();
              allAgents.value.forEach((agent) => {
                if (assignedAgentIds.value.has(agent.id ?? '') && agent.id != null) {
                  agentTableRef.value.toggleRowSelection(agent, true);
                }
              });
            }
          });
        })
        .catch(() => {
          allAgents.value = [];
          assignedAgentIds.value = new Set();
          selectedAgentIds.value = [];
        })
        .finally(() => {
          assignLoading.value = false;
        });
    }
  },
});
</script>

<template>
  <Modal title="分配智能体" class="w-200">
    <template #header>
      <div class="flex items-center gap-3">
        <span class="text-base font-semibold text-foreground">分配智能体</span>
        <span class="rounded bg-muted px-2.5 py-0.5 text-xs text-muted-foreground">
          {{ modalApi.getData<any>()?.name }}
        </span>
      </div>
    </template>
    <div v-loading="assignLoading" class="min-h-[200px]">
      <ElTable
        ref="agentTableRef"
        :data="allAgents"
        style="width: 100%"
        stripe
        max-height="400"
        @selection-change="
          (rows) => {
            selectedAgentIds = rows.map((r: any) => r.id).filter(Boolean);
          }
        "
        :row-key="(row: any) => row.id"
      >
        <ElTableColumn type="selection" width="50" :reserve-selection="true" />
        <ElTableColumn prop="name" label="名称" min-width="80" />
        <ElTableColumn prop="category" label="类型" min-width="40" />
        <ElTableColumn prop="description" label="描述" min-width="180" show-overflow-tooltip />
      </ElTable>
    </div>
  </Modal>
</template>
