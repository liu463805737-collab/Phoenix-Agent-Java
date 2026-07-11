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

export async function getModelConfigListApi() {
  return requestClient.get<ModelConfig[]>('/api/model-config/list', {
    responseReturn: 'body',
  });
}

export async function addModelConfigApi(config: Omit<ModelConfig, 'id'>) {
  return requestClient.post('/api/model-config/add', config, {
    responseReturn: 'body',
  });
}

export async function updateModelConfigApi(config: ModelConfig) {
  return requestClient.put('/api/model-config/update', config, {
    responseReturn: 'body',
  });
}

export async function deleteModelConfigApi(id: number) {
  return requestClient.delete(`/api/model-config/${id}`, {
    responseReturn: 'body',
  });
}

export async function activateModelConfigApi(id: number) {
  return requestClient.post(`/api/model-config/activate/${id}`, undefined, {
    responseReturn: 'body',
  });
}

export async function testModelConfigConnectionApi(
  config: Omit<ModelConfig, 'id'>,
) {
  return requestClient.post('/api/model-config/test', config, {
    responseReturn: 'body',
  });
}

export async function checkModelReadyApi() {
  return requestClient.get<ModelCheckReady>('/api/model-config/check-ready', {
    responseReturn: 'body',
  });
}
