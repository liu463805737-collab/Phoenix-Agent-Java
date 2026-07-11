export interface PageResult<T> {
  pageNumber: number;
  pageSize: number;
  totalRow: number;
  records: T[];
}
