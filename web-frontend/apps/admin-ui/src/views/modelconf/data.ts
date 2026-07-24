export interface ColumnDef {
  prop?: string;
  label: string;
  width?: number;
  minWidth?: number;
  slot?: string;
}

export function useColumns(): ColumnDef[] {
  return [
    { prop: 'id', label: 'ID', width: 80 },
    { label: '提供商', width: 120, slot: 'provider' },
    { prop: 'modelName', label: '模型名称', width: 180 },
    { label: '模型类型', width: 120, slot: 'modelType' },
    { prop: 'baseUrl', label: 'API地址', minWidth: 200 },
    { label: '路径配置', minWidth: 180, slot: 'path' },
    { label: '温度', width: 100, slot: 'temperature' },
    { label: '最大Token', width: 120, slot: 'maxTokens' },
    { label: '状态', width: 100, slot: 'status' },
  ];
}
