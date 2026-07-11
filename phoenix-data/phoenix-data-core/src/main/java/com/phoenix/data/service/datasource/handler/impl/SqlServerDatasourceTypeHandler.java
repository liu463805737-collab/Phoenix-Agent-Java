package com.phoenix.data.service.datasource.handler.impl;

import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * SQL Server 数据源类型处理器
 */
@Component
public class SqlServerDatasourceTypeHandler implements DatasourceTypeHandler {

	/**
	 * 返回SQL Server数据库类型名称
	 * @return 类型名称
	 */
	@Override
	public String typeName() {
		return BizDataSourceTypeEnum.SQL_SERVER.getTypeName();
	}

}
