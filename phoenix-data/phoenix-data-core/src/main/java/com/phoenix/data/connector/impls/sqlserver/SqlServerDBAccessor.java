package com.phoenix.data.connector.impls.sqlserver;

import com.phoenix.data.connector.accessor.AbstractAccessor;
import com.phoenix.data.connector.ddl.DdlFactory;
import com.phoenix.data.connector.pool.DBConnectionPoolFactory;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.springframework.stereotype.Service;

/**
 * SQL Server 数据库访问器实现。
 *
 * @author zihen
 * @date 2025/12/14 17:34
 */
@Service("sqlserverAccessor")
public class SqlServerDBAccessor extends AbstractAccessor {

	private final static String ACCESSOR_TYPE = "SqlServer_Accessor";

	/**
	 * 构造函数。
	 */
	public SqlServerDBAccessor(DdlFactory ddlFactory, DBConnectionPoolFactory poolFactory) {
		super(ddlFactory, poolFactory.getPoolByDbType(BizDataSourceTypeEnum.SQL_SERVER.getTypeName()));
	}

	/**
	 * 获取访问器类型标识。
	 */
	@Override
	public String getAccessorType() {
		return ACCESSOR_TYPE;
	}

	/**
	 * 判断是否支持指定的数据源类型。
	 */
	@Override
	public boolean supportedDataSourceType(String type) {
		return BizDataSourceTypeEnum.SQL_SERVER.getTypeName().equalsIgnoreCase(type);
	}

}
