package com.phoenix.data.connector.impls.h2;

import com.phoenix.data.connector.accessor.AbstractAccessor;
import com.phoenix.data.connector.ddl.DdlFactory;
import com.phoenix.data.connector.pool.DBConnectionPoolFactory;
import com.phoenix.data.enums.BizDataSourceTypeEnum;
import org.springframework.stereotype.Service;

/**
 * H2 数据库访问器实现。
 *
 * @author HunterPorter
 * @author <a href="mailto:zongpeng_hzp@163.com">HunterPorter</a>
 */

@Service("h2Accessor")
public class H2DBAccessor extends AbstractAccessor {

	private final static String ACCESSOR_TYPE = "H2_Accessor";

	/**
	 * 构造函数。
	 */
	protected H2DBAccessor(DdlFactory ddlFactory, DBConnectionPoolFactory poolFactory) {

		super(ddlFactory, poolFactory.getPoolByDbType(BizDataSourceTypeEnum.H2.getTypeName()));
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
		return BizDataSourceTypeEnum.H2.getTypeName().equalsIgnoreCase(type);
	}

}
