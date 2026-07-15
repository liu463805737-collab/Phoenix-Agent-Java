<script lang="ts" setup>
import { nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  ElButton,
  ElInput,
  ElMessage,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import type { AccountInfo } from '#/api';
import {
  createAccountGroupInfoApi,
  getUnGroupPageByGroupId,
} from '#/api';

defineOptions({ name: 'AssignPeopleForm' });

const emit = defineEmits(['success']);

const assignLoading = ref(false);
const allAccounts = ref<AccountInfo[]>([]);
const selectedAccountIds = ref<string[]>([]);
const assignPeoplePage = ref(1);
const assignPeoplePageSize = ref(10);
const assignPeopleTotal = ref(0);
const assignPeopleName = ref('');
const assignPeopleCode = ref('');
const assignPeopleAccountMap = ref<Map<string, AccountInfo>>(new Map());
const peopleTableRef = ref();

async function loadAssignPeople() {
  assignLoading.value = true;
  const groupData = modalApi.getData<any>();
  try {
    const params: Record<string, any> = { groupId: groupData?.id };
    if (assignPeopleName.value) params.realName = assignPeopleName.value;
    if (assignPeopleCode.value) params.code = assignPeopleCode.value;
    const res = (await getUnGroupPageByGroupId(
      assignPeoplePage.value,
      assignPeoplePageSize.value,
      params,
    )) as any;
    const pageResult = res?.data || res;
    allAccounts.value = pageResult?.records || [];
    assignPeopleTotal.value = pageResult?.totalRow || 0;
    for (const row of allAccounts.value) {
      if (row.id) assignPeopleAccountMap.value.set(row.id, row);
    }
    await nextTick();
    peopleTableRef.value?.clearSelection();
  } catch {
    allAccounts.value = [];
  } finally {
    assignLoading.value = false;
  }
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const groupData = modalApi.getData<any>();
    if (!groupData) return;
    assignLoading.value = true;
    try {
      const groupId = groupData.id;
      const groupName = groupData.name;
      for (const accountId of selectedAccountIds.value) {
        const account = assignPeopleAccountMap.value.get(accountId);
        await createAccountGroupInfoApi({
          groupId,
          accountId,
          groupName,
          accountName: account?.realName ?? '',
        });
      }
      ElMessage.success('人员分配成功');
      modalApi.close();
      emit('success');
    } catch {
      ElMessage.error('人员分配失败');
    } finally {
      assignLoading.value = false;
    }
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      assignPeopleName.value = '';
      assignPeopleCode.value = '';
      assignPeopleAccountMap.value = new Map();
      assignPeoplePage.value = 1;
      assignPeoplePageSize.value = 10;
      assignPeopleTotal.value = 0;
      selectedAccountIds.value = [];
      loadAssignPeople();
    }
  },
});
</script>

<template>
  <Modal title="分配人员" class="w-200">
    <template #header>
      <div class="flex items-center gap-3">
        <span class="text-base font-semibold text-foreground">分配人员</span>
        <span class="rounded bg-muted px-2.5 py-0.5 text-xs text-muted-foreground">
          {{ modalApi.getData<any>()?.name }}
        </span>
      </div>
    </template>
    <div class="mb-3 flex gap-2">
      <ElInput
        v-model="assignPeopleName"
        placeholder="姓名"
        clearable
        style="width: 160px"
        @keyup.enter="assignPeoplePage = 1; loadAssignPeople()"
      />
      <ElInput
        v-model="assignPeopleCode"
        placeholder="工号"
        clearable
        style="width: 160px"
        @keyup.enter="assignPeoplePage = 1; loadAssignPeople()"
      />
      <ElButton type="primary" @click="assignPeoplePage = 1; loadAssignPeople()">
        搜索
      </ElButton>
    </div>
    <div v-loading="assignLoading" class="min-h-[200px]">
      <ElTable
        ref="peopleTableRef"
        :data="allAccounts"
        style="width: 100%"
        stripe
        max-height="360"
        @selection-change="
          (rows) => {
            selectedAccountIds = rows.map((r: any) => r.id).filter(Boolean);
          }
        "
        :row-key="(row: any) => row.id"
      >
        <ElTableColumn type="selection" width="50" :reserve-selection="true" />
        <ElTableColumn prop="code" label="工号" width="100" />
        <ElTableColumn prop="username" label="用户名" />
        <ElTableColumn prop="realName" label="真实姓名" />
        <ElTableColumn prop="phone" label="手机号" />
        <ElTableColumn label="状态" width="70">
          <template #default="scope">
            <ElTag
              :type="(scope.row as AccountInfo).status === '1' ? 'success' : 'danger'"
              size="small"
            >
              {{ (scope.row as AccountInfo).status === '1' ? '启用' : '禁用' }}
            </ElTag>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="flex justify-end pt-3">
        <ElPagination
          v-model:current-page="assignPeoplePage"
          v-model:page-size="assignPeoplePageSize"
          :total="assignPeopleTotal"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          small
          @current-change="loadAssignPeople"
          @size-change="assignPeoplePage = 1; loadAssignPeople()"
        />
      </div>
    </div>
  </Modal>
</template>
