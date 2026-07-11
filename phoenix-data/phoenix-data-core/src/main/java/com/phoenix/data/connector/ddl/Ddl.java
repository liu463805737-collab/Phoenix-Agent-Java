package com.phoenix.data.connector.ddl;

import com.phoenix.data.enums.BizDataSourceTypeEnum;

/**
 * DDL 执行器接口，定义数据源类型判断及 DDL 操作的基础方法。
 */
public interface Ddl {

	/**
	 * 获取数据源类型枚举。
	 */
	BizDataSourceTypeEnum getDataSourceType();

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	default boolean supportedDataSourceType(String type) {
		return getDataSourceType().getTypeName().equals(type);
	}

	default boolean supportedDataSourceType(BizDataSourceTypeEnum type) {
		return getDataSourceType().equals(type);
	}

	default String getDdlType() {
		return getDataSourceType().getProtocol() + "@" + getDataSourceType().getDialect();
	}

}
