/**
 * @description: 配置默认排序号相关属性
 */
export enum OrderNoDefaultEnum {
  VALUE = 100,
  MAX = 999999999,
  MIN = -999999999,
}

/**
 * @description: 配置默认排序号相关属性
 */
export enum LevelNoDefaultEnum {
  VALUE = 1,
  MAX = 999999999,
  MIN = 0,
}

/**
 * @description: 配置默认描述字段
 */
export enum RemarkDefaultEnum {
  MIN_ROWS = 2,
  MAX_ROWS = 8,
}

export enum FormValidPatternEnum {
  // '^[a-zA-Z_]{1,}[0-9a-zA-Z_]{1,}$'
  CODE = '^[0-9a-zA-Z_\-]{1,}$', // 请输入英文或数字！
  SN = '^[a-zA-Z_]{1,}[0-9a-zA-Z_]*$', // 请输入英文或数字（以英文或下划线开头）！
  FIELD_NAME_SN = '(^_([a-zA-Z0-9]_?)*$)|(^[a-zA-Z](_?[a-zA-Z0-9])*_?$)',
  URL = "^(http:\\/\\/|https:\\/\\/|\\/)",
  EMAIL = '^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$',
  MOBILE = '^(1[0-9])\\d{9}$',
}

/**
 * @description:  contentTyp
 */
export enum ContentTypeEnum {
  // json
  JSON = 'application/json;charset=UTF-8',
  // form-data qs
  FORM_URLENCODED = 'application/x-www-form-urlencoded;charset=UTF-8',
  // form-data  upload
  FORM_DATA = 'multipart/form-data;charset=UTF-8',
}
