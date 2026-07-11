package com.phoenix.data.connector.impls.postgre;

import com.phoenix.data.connector.accessor.AbstractAccessor;
import com.phoenix.data.connector.ddl.DdlFactory;
import com.phoenix.data.connector.pool.DBConnectionPoolFactory;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.springframework.stereotype.Service;

/**
 * PostgreSQL 数据库访问器实现。
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service("postgreAccessor")
public class PostgreDBAccessor extends AbstractAccessor {

	private final static String ACCESSOR_TYPE = "PostgreSQL_Accessor";

	/**
	 * 构造函数。
	 */
	protected PostgreDBAccessor(DdlFactory ddlFactory, DBConnectionPoolFactory poolFactory) {

		super(ddlFactory, poolFactory.getPoolByDbType(BizDataSourceTypeEnum.POSTGRESQL.getTypeName()));
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
		return BizDataSourceTypeEnum.POSTGRESQL.getTypeName().equalsIgnoreCase(type);
	}

}
