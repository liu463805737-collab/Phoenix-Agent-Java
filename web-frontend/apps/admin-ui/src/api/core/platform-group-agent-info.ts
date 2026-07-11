import { requestClient } from '#/api/request';

export interface GroupAgentAssignment {
  id?: string;
  groupId?: string;
  agentId?: string;
  agentName?: string;
  createTime?: string;
  creator?: string;
  updateTime?: string;
  update?: string;
}

export async function getGroupAgentInfoByGroupApi(groupId: string) {
  return requestClient.get<GroupAgentAssignment[]>(
    `/platform/group-agent-info/group/${groupId}`,
    { responseReturn: 'body' },
  );
}

export async function createGroupAgentInfoApi(data: Record<string, any>) {
  return requestClient.post('/platform/group-agent-info', data, {
    responseReturn: 'body',
  });
}

export async function deleteGroupAgentInfoByGroupAndAgentApi(
  groupId: string,
  agentId: string,
) {
  return requestClient.delete(
    `/platform/group-agent-info/group/${groupId}/agent/${agentId}`,
    { responseReturn: 'body' },
  );
}
