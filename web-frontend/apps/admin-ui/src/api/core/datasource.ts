import { requestClient } from '#/api/request';

export interface Datasource {
  id?: number;
  name?: string;
  type?: string;
  host?: string;
  port?: number;
  databaseName?: string;
  schema?: string;
  jdbcUrl?: string;
  username?: string;
  password?: string;
  description?: string;
  status?: string;
  createTime?: string;
  updateTime?: string;
}

export interface AgentDatasource {
  id?: number;
  agentId?: number;
  datasourceId?: number;
  datasource?: Datasource;
  isActive?: number;
  selectTables?: string[];
}

export async function getAllDatasourcesApi(status?: string, type?: string) {
  const params: Record<string, string> = {};
  if (status) params.status = status;
  if (type) params.type = type;
  return requestClient.get<Datasource[]>('/api/datasource', {
    params,
    responseReturn: 'body',
  });
}

export async function getDatasourceApi(id: number) {
  return requestClient.get<Datasource>(`/api/datasource/${id}`, {
    responseReturn: 'body',
  });
}

export async function createDatasourceApi(data: Partial<Datasource>) {
  return requestClient.post<Datasource>('/api/datasource', data, {
    responseReturn: 'body',
  });
}

export async function updateDatasourceApi(
  id: number,
  data: Partial<Datasource>,
) {
  return requestClient.put<Datasource>(`/api/datasource/${id}`, data, {
    responseReturn: 'body',
  });
}

export async function deleteDatasourceApi(id: number) {
  return requestClient.delete(`/api/datasource/${id}`, {
    responseReturn: 'body',
  });
}

export async function testDatasourceConnectionApi(id: number) {
  return requestClient.post<{ message?: string; success: boolean }>(
    `/api/datasource/${id}/test`,
    undefined,
    { responseReturn: 'body' },
  );
}

export interface DatasourceType {
  code: number;
  typeName: string;
  dialect: string;
  protocol: string;
  displayName: string;
}

export async function getDatasourceTypesApi() {
  return requestClient.get<DatasourceType[]>('/api/datasource/types', {
    responseReturn: 'body',
  });
}

export async function getDatasourceTablesApi(id: number) {
  return requestClient.get<string[]>(`/api/datasource/${id}/tables`, {
    responseReturn: 'body',
  });
}

export interface ColumnInfo {
  name: string;
  type: string;
  description?: string;
}

export async function getDatasourceTableColumnsApi(
  id: number,
  tableName: string,
) {
  return requestClient.get<string[]>(
    `/api/datasource/${id}/tables/${tableName}/columns`,
    { responseReturn: 'body' },
  );
}

export async function getDatasourceTableColumnNamesApi(
  id: number,
  tableName: string,
): Promise<string[]> {
  const response = await requestClient.get<{
    data: string[];
    success: boolean;
  }>(`/api/datasource/${id}/tables/${tableName}/columns`, {
    responseReturn: 'body',
  });
  if (!response.success) return [];
  return response.data ?? [];
}

export interface LogicalRelation {
  id?: number;
  sourceTableName?: string;
  sourceColumnName?: string;
  targetTableName?: string;
  targetColumnName?: string;
  relationType?: string;
  description?: string;
}

// ===== 逻辑外键 =====

export async function getLogicalRelationsApi(datasourceId: number) {
  return requestClient.get<LogicalRelation[]>(
    `/api/datasource/${datasourceId}/logical-relations`,
    { responseReturn: 'body' },
  );
}

export async function createLogicalRelationApi(
  datasourceId: number,
  data: Partial<LogicalRelation>,
) {
  return requestClient.post<LogicalRelation>(
    `/api/datasource/${datasourceId}/logical-relations`,
    data,
    { responseReturn: 'body' },
  );
}

export async function updateLogicalRelationApi(
  datasourceId: number,
  relationId: number,
  data: Partial<LogicalRelation>,
) {
  return requestClient.put<LogicalRelation>(
    `/api/datasource/${datasourceId}/logical-relations/${relationId}`,
    data,
    { responseReturn: 'body' },
  );
}

export async function deleteLogicalRelationApi(
  datasourceId: number,
  relationId: number,
) {
  return requestClient.delete(
    `/api/datasource/${datasourceId}/logical-relations/${relationId}`,
    { responseReturn: 'body' },
  );
}

export async function saveLogicalRelationsApi(
  datasourceId: number,
  data: Partial<LogicalRelation>[],
) {
  return requestClient.put<LogicalRelation[]>(
    `/api/datasource/${datasourceId}/logical-relations`,
    data,
    { responseReturn: 'body' },
  );
}

// ===== Agent-Datasource 关联 =====

export async function getAgentDatasourcesApi(agentId: number) {
  return requestClient.get<AgentDatasource[]>(
    `/api/agent/${agentId}/datasources`,
    { responseReturn: 'body' },
  );
}
export async function initSchema(agentId: number) {
  return requestClient.post(
    `/api/agent/${agentId}/datasources/init`,
    undefined,
    { responseReturn: 'body' },
  );
}

export async function getActiveAgentDatasourceApi(agentId: number) {
  return requestClient.get<AgentDatasource>(
    `/api/agent/${agentId}/datasources/active`,
    { responseReturn: 'body' },
  );
}

export async function addAgentDatasourceApi(
  agentId: number,
  datasourceId: number,
) {
  return requestClient.post(
    `/api/agent/${agentId}/datasources/${datasourceId}`,
    undefined,
    { responseReturn: 'body' },
  );
}

export async function removeAgentDatasourceApi(
  agentId: number,
  datasourceId: number,
) {
  return requestClient.delete(
    `/api/agent/${agentId}/datasources/${datasourceId}`,
    { responseReturn: 'body' },
  );
}

export async function toggleAgentDatasourceApi(
  agentId: number,
  datasourceId: number,
  isActive: boolean,
) {
  return requestClient.put(
    `/api/agent/${agentId}/datasources/toggle`,
    { datasourceId, isActive },
    { responseReturn: 'body' },
  );
}

export async function updateAgentDatasourceTablesApi(
  agentId: number,
  datasourceId: number,
  tables: string[],
) {
  return requestClient.post(
    `/api/agent/${agentId}/datasources/tables`,
    { datasourceId, tables },
    { responseReturn: 'body' },
  );
}

export async function initAgentDatasourceApi(agentId: number) {
  return requestClient.post(
    `/api/agent/${agentId}/datasources/init`,
    {},
    { responseReturn: 'body' },
  );
}
