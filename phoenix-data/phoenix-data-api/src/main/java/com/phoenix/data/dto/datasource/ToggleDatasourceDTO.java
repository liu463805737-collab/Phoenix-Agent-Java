package com.phoenix.data.dto.datasource;

import lombok.Data;

/**
 * 切换数据源启禁状态 DTO
 */
@Data
public class ToggleDatasourceDTO {

	private Integer datasourceId;

	private Boolean isActive;

}
