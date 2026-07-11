import type { LogicalRelation } from '#/api/core/datasource';
import {
  getLogicalRelationsApi,
  getDatasourceTableColumnsApi,
  saveLogicalRelationsApi,
} from '#/api/core/datasource';

export type { LogicalRelation };

const logicalRelationService = {
  async getLogicalRelations(datasourceId: number) {
    return getLogicalRelationsApi(datasourceId);
  },

  async getTableColumns(datasourceId: number, tableName: string) {
    return getDatasourceTableColumnsApi(datasourceId, tableName);
  },

  async saveLogicalRelations(
    datasourceId: number,
    data: Partial<LogicalRelation>[],
  ) {
    return saveLogicalRelationsApi(datasourceId, data);
  },
};

export default logicalRelationService;
