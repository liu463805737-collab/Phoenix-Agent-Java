import {
  getAgentDatasourcesApi,
  getActiveAgentDatasourceApi,
  initSchema,
  toggleAgentDatasourceApi,
  removeAgentDatasourceApi,
  addAgentDatasourceApi,
  updateAgentDatasourceTablesApi,
} from '#/api/core/datasource';

const agentDatasourceService = {
  async getAgentDatasource(agentId: number) {
    return getAgentDatasourcesApi(agentId);
  },

  async getActiveAgentDatasource(agentId: number) {
    return getActiveAgentDatasourceApi(agentId);
  },

  async initSchema(agentId: number) {
    return initSchema(agentId);
  },

  async toggleDatasourceForAgent(
    agentId: number,
    params: { datasourceId: number; isActive: boolean },
  ) {
    return toggleAgentDatasourceApi(
      agentId,
      params.datasourceId,
      params.isActive,
    );
  },

  async removeDatasourceFromAgent(agentId: number, datasourceId: number) {
    return removeAgentDatasourceApi(agentId, datasourceId);
  },

  async addDatasourceToAgent(agentId: number, datasourceId: number) {
    return addAgentDatasourceApi(agentId, datasourceId);
  },

  async updateDatasourceTables(
    agentId: number,
    datasourceId: number,
    tables: string[],
  ) {
    return updateAgentDatasourceTablesApi(agentId, datasourceId, tables);
  },
};

export default agentDatasourceService;
