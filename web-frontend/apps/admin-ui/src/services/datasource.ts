import type {
  Datasource,
  AgentDatasource,
  DatasourceType,
} from '#/api/core/datasource';
import {
  getAllDatasourcesApi,
  getDatasourceTypesApi,
  testDatasourceConnectionApi,
  createDatasourceApi,
  updateDatasourceApi,
  deleteDatasourceApi,
  getDatasourceTablesApi,
} from '#/api/core/datasource';

export type { Datasource, AgentDatasource, DatasourceType };

const datasourceService = {
  async getAllDatasource(status?: string, type?: string) {
    return getAllDatasourcesApi(status, type);
  },

  async getDatasourceTypes() {
    return getDatasourceTypesApi();
  },

  async testConnection(id: number) {
    return testDatasourceConnectionApi(id);
  },

  async createDatasource(data: Partial<Datasource>) {
    return createDatasourceApi(data);
  },

  async updateDatasource(id: number, data: Partial<Datasource>) {
    return updateDatasourceApi(id, data);
  },

  async deleteDatasource(id: number) {
    return deleteDatasourceApi(id);
  },

  async getDatasourceTables(id: number) {
    return getDatasourceTablesApi(id);
  },
};

export default datasourceService;
