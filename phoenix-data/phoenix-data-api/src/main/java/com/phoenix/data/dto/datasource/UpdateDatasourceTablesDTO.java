package com.phoenix.data.dto.datasource;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新数据源关联表 DTO
 */
@Data
public class UpdateDatasourceTablesDTO {

	@NotNull(message = "datasourceId cannot be null")
	private Integer datasourceId;

	private List<String> tables;

}
