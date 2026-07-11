package com.phoenix.data.bo.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外键信息业务对象，描述表之间的外键关联关系。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKeyInfoBO {

	private String table;

	private String column;

	private String referencedTable;

	private String referencedColumn;

}
