package com.phoenix.data.connector.impls.hive;

import com.phoenix.data.connector.accessor.AbstractAccessor;
import com.phoenix.data.connector.ddl.DdlFactory;
import com.phoenix.data.connector.pool.DBConnectionPoolFactory;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.springframework.stereotype.Service;

/**
 * Hive 数据源访问器实现
 */
@Service("hiveAccessor")
public class HiveDBAccessor extends AbstractAccessor {

	private final static String ACCESSOR_TYPE = "Hive_Accessor";

	/**
	 * 构造函数。
	 */
	protected HiveDBAccessor(DdlFactory ddlFactory, DBConnectionPoolFactory poolFactory) {
		super(ddlFactory, poolFactory.getPoolByDbType(BizDataSourceTypeEnum.HIVE.getTypeName()));
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
		return BizDataSourceTypeEnum.HIVE.getTypeName().equalsIgnoreCase(type);
	}

}
