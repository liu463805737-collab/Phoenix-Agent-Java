package com.phoenix.data.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库配置业务对象，包含数据库连接所需的基本信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbConfigBO {

	private String schema;

	private String url;

	private String username;

	private String password;

	private String connectionType;

	private String dialectType;

}
