import { requestClient } from '#/api/request';

export interface LogicalRelation {
  id?: number;
  datasourceId?: number;
  sourceTableName: string;
  sourceColumnName: string;
  targetTableName: string;
  targetColumnName: string;
  relationType?: string;
  description?: string;
  isDeleted?: number;
  createdTime?: string;
  updatedTime?: string;
}

const API_BASE_URL = '/datasource';

export async function getLogicalRelationsApi(datasourceId: number) {
  return requestClient.get<{ success: boolean; data: LogicalRelation[] }>(
    `${API_BASE_URL}/${datasourceId}/logical-relations`,
    { responseReturn: 'body' },
  );
}

export async function addLogicalRelationApi(
  datasourceId: number,
  logicalRelation: Omit<LogicalRelation, 'id' | 'datasourceId' | 'isDeleted' | 'createdTime' | 'updatedTime'>,
) {
  return requestClient.post<{ success: boolean; data: LogicalRelation }>(
    `${API_BASE_URL}/${datasourceId}/logical-relations`,
    logicalRelation,
    { responseReturn: 'body' },
  );
}

export async function updateLogicalRelationApi(
  datasourceId: number,
  relationId: number,
  logicalRelation: Omit<LogicalRelation, 'id' | 'datasourceId' | 'isDeleted' | 'createdTime' | 'updatedTime'>,
) {
  return requestClient.put<{ success: boolean; data: LogicalRelation }>(
    `${API_BASE_URL}/${datasourceId}/logical-relations/${relationId}`,
    logicalRelation,
    { responseReturn: 'body' },
  );
}

export async function deleteLogicalRelationApi(datasourceId: number, relationId: number) {
  return requestClient.delete(
    `${API_BASE_URL}/${datasourceId}/logical-relations/${relationId}`,
    { responseReturn: 'body' },
  );
}

export async function saveLogicalRelationsApi(datasourceId: number, logicalRelations: LogicalRelation[]) {
  return requestClient.put<{ success: boolean; data: LogicalRelation[] }>(
    `${API_BASE_URL}/${datasourceId}/logical-relations`,
    logicalRelations,
    { responseReturn: 'body' },
  );
}

export async function getTableColumnsApi(datasourceId: number, tableName: string) {
  return requestClient.get<{ success: boolean; data: string[] }>(
    `${API_BASE_URL}/${datasourceId}/tables/${tableName}/columns`,
    { responseReturn: 'body' },
  );
}
