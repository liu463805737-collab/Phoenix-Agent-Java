import type {
  SemanticModel,
  SemanticModelAddDto,
  SemanticModelImportItem,
  BatchImportResult,
} from '#/api/core/semanticModel';
import {
  getSemanticModelListApi,
  createSemanticModelApi,
  updateSemanticModelApi,
  deleteSemanticModelApi,
  batchDeleteSemanticModelApi,
  enableSemanticModelApi,
  disableSemanticModelApi,
  batchImportSemanticModelApi,
  downloadSemanticModelTemplateApi,
  importSemanticModelExcelApi,
} from '#/api/core/semanticModel';

export type { SemanticModel, SemanticModelAddDto, SemanticModelImportItem, BatchImportResult };

const semanticModelService = {
  async list(agentId?: number, keyword?: string) {
    return getSemanticModelListApi(agentId, keyword);
  },

  async create(data: SemanticModelAddDto) {
    try {
      await createSemanticModelApi(data);
      return true;
    } catch {
      return false;
    }
  },

  async update(id: number, data: SemanticModel) {
    try {
      await updateSemanticModelApi(id, data);
      return true;
    } catch {
      return false;
    }
  },

  async delete(id: number) {
    try {
      await deleteSemanticModelApi(id);
      return true;
    } catch {
      return false;
    }
  },

  async batchDelete(ids: number[]) {
    try {
      await batchDeleteSemanticModelApi(ids);
      return true;
    } catch {
      return false;
    }
  },

  async enable(ids: number[]) {
    try {
      await enableSemanticModelApi(ids);
      return true;
    } catch {
      return false;
    }
  },

  async disable(ids: number[]) {
    try {
      await disableSemanticModelApi(ids);
      return true;
    } catch {
      return false;
    }
  },

  async batchImport(dto: { agentId: number; items: SemanticModelImportItem[] }) {
    return batchImportSemanticModelApi(dto);
  },

  async downloadTemplate() {
    await downloadSemanticModelTemplateApi();
  },

  async importExcel(file: File, agentId: number) {
    return importSemanticModelExcelApi(file, agentId);
  },
};

export default semanticModelService;
