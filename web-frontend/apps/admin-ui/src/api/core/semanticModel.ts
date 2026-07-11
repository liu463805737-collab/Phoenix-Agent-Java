import { requestClient } from '#/api/request';

export interface SemanticModel {
  id?: number;
  agentId: number;
  datasourceId?: number;
  tableName: string;
  columnName: string;
  businessName: string;
  synonyms: string;
  businessDescription: string;
  columnComment: string;
  dataType: string;
  status: number;
  createdTime?: string;
  updateTime?: string;
}

export interface SemanticModelAddDto {
  agentId: number;
  tableName: string;
  columnName: string;
  businessName: string;
  synonyms: string;
  businessDescription: string;
  columnComment: string;
  dataType: string;
}

export interface SemanticModelImportItem {
  tableName: string;
  columnName: string;
  businessName: string;
  synonyms?: string;
  businessDescription?: string;
  columnComment?: string;
  dataType: string;
}

export interface SemanticModelBatchImportDTO {
  agentId: number;
  items: SemanticModelImportItem[];
}

export interface BatchImportResult {
  total: number;
  successCount: number;
  failCount: number;
  errors: string[];
}

const API_BASE_URL = '/api/semantic-model';

export async function getSemanticModelListApi(agentId?: number, keyword?: string): Promise<SemanticModel[]> {
  const params: Record<string, string> = {};
  if (agentId !== undefined) params.agentId = agentId.toString();
  if (keyword) params.keyword = keyword;
  const response = await requestClient.get<{ success: boolean; data: SemanticModel[] }>(
    API_BASE_URL,
    { params, responseReturn: 'body' },
  );
  if (!response.success) {
    return [];
  }
  return response.data ?? [];
}

export async function getSemanticModelApi(id: number): Promise<SemanticModel | null> {
  const response = await requestClient.get<{ success: boolean; data: SemanticModel }>(
    `${API_BASE_URL}/${id}`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    return null;
  }
  return response.data;
}

export async function createSemanticModelApi(model: SemanticModelAddDto): Promise<void> {
  const response = await requestClient.post<{ success: boolean; message?: string }>(
    API_BASE_URL,
    model,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '创建语义模型失败');
  }
}

export async function updateSemanticModelApi(id: number, model: SemanticModel): Promise<void> {
  const response = await requestClient.put<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/${id}`,
    model,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '更新语义模型失败');
  }
}

export async function deleteSemanticModelApi(id: number): Promise<void> {
  const response = await requestClient.delete<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/${id}`,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '删除语义模型失败');
  }
}

export async function batchDeleteSemanticModelApi(ids: number[]): Promise<void> {
  const response = await requestClient.delete<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/batch`,
    { data: ids, responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '批量删除失败');
  }
}

export async function enableSemanticModelApi(ids: number[]): Promise<void> {
  const response = await requestClient.put<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/enable`,
    ids,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '启用语义模型失败');
  }
}

export async function disableSemanticModelApi(ids: number[]): Promise<void> {
  const response = await requestClient.put<{ success: boolean; message?: string }>(
    `${API_BASE_URL}/disable`,
    ids,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '停用语义模型失败');
  }
}

export async function batchImportSemanticModelApi(dto: SemanticModelBatchImportDTO): Promise<BatchImportResult> {
  const response = await requestClient.post<{ success: boolean; data: BatchImportResult; message?: string }>(
    `${API_BASE_URL}/batch-import`,
    dto,
    { responseReturn: 'body' },
  );
  if (!response.success) {
    throw new Error(response.message || '批量导入失败');
  }
  return response.data;
}

export async function importSemanticModelExcelApi(file: File, agentId: number): Promise<BatchImportResult> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('agentId', agentId.toString());

  const response = await requestClient.post<{ success: boolean; data: BatchImportResult; message?: string }>(
    `${API_BASE_URL}/import/excel`,
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' },
      responseReturn: 'body',
    },
  );
  if (!response.success) {
    throw new Error(response.message || 'Excel导入失败');
  }
  return response.data;
}

export async function downloadSemanticModelTemplateApi(): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/template/download`);
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', 'semantic_model_template.xlsx');
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
