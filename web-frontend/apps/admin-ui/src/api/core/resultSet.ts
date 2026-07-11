export interface ResultData {
  displayStyle?: ResultDisplayStyleBO;
  resultSet: ResultSetData;
}

export interface ResultDisplayStyleBO {
  type: string;
  title: string;
  x: string;
  y: Array<string>;
}

export interface ResultSetData {
  column: string[];
  data: Array<Record<string, string>>;
  errorMsg?: string;
}

export interface PaginationConfig {
  currentPage: number;
  pageSize: number;
  total: number;
}

export interface ResultSetDisplayConfig {
  showSqlResults: boolean;
  pageSize: number;
}
