import { requestClient } from '#/api/request';

export interface ModelConfig {
  id?: number;
  provider: string;
  apiKey: string;
  baseUrl: string;
  modelName: string;
  modelType: string;
  temperature?: number;
  maxTokens?: number;
  isActive?: boolean;
  completionsPath?: string;
  embeddingsPath?: string;
  proxyEnabled?: boolean;
  proxyHost?: string;
  proxyPort?: number;
  proxyUsername?: string;
  proxyPassword?: string;
}

export interface ModelCheckReady {
  chatModelReady: boolean;
  embeddingModelReady: boolean;
  ready: boolean;
}

const API_BASE_URL = '/api/model-config';

export async function getModelConfigListApi() {
  return requestClient.get<ModelConfig[]>(`${API_BASE_URL}/list`, {
    responseReturn: 'body',
  });
}

export async function addModelConfigApi(config: Omit<ModelConfig, 'id'>) {
  return requestClient.post<string>(`${API_BASE_URL}/add`, config, {
    responseReturn: 'body',
  });
}

export async function updateModelConfigApi(config: ModelConfig) {
  return requestClient.put<string>(`${API_BASE_URL}/update`, config, {
    responseReturn: 'body',
  });
}

export async function deleteModelConfigApi(id: number) {
  return requestClient.delete<string>(`${API_BASE_URL}/${id}`, {
    responseReturn: 'body',
  });
}

export async function activateModelConfigApi(id: number) {
  return requestClient.post<string>(
    `${API_BASE_URL}/activate/${id}`,
    {},
    {
      responseReturn: 'body',
    },
  );
}

export async function testModelConfigConnectionApi(
  config: Omit<ModelConfig, 'id'>,
) {
  return requestClient.post<string>(`${API_BASE_URL}/test`, config, {
    responseReturn: 'body',
  });
}

export async function checkModelConfigReadyApi() {
  return requestClient.get<ModelCheckReady>(`${API_BASE_URL}/check-ready`, {
    responseReturn: 'body',
  });
}
