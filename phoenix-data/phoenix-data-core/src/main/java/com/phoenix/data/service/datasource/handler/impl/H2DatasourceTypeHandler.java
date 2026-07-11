package com.phoenix.data.service.datasource.handler.impl;

import com.phoenix.data.entity.Datasource;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import com.phoenix.data.service.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * H2 内存数据库数据源类型处理器
 */
@Component
public class H2DatasourceTypeHandler implements DatasourceTypeHandler {

	/**
	 * 返回H2数据库类型名称
	 * @return 类型名称
	 */
	@Override
	public String typeName() {
		return BizDataSourceTypeEnum.H2.getTypeName();
	}

	/**
	 * 构建H2内存数据库JDBC连接URL
	 * @param datasource 数据源
	 * @return 连接URL
	 */
	@Override
	public String buildConnectionUrl(Datasource datasource) {
		if (!hasRequiredConnectionFields(datasource)) {
			return datasource.getConnectionUrl();
		}
		return String.format(
				"jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE",
				datasource.getDatabaseName());
	}

}
