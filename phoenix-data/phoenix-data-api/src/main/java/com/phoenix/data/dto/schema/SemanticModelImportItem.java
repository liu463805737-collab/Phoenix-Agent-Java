package com.phoenix.data.dto.schema;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 语义模型导入项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticModelImportItem {

	@NotBlank(message = "表名不能为空")
	@ExcelProperty(value = "表名*", index = 0)
	private String tableName;

	@NotBlank(message = "字段名不能为空")
	@ExcelProperty(value = "字段名*", index = 1)
	private String columnName;

	@NotBlank(message = "业务名称不能为空")
	@ExcelProperty(value = "业务名称*", index = 2)
	private String businessName;

	@ExcelProperty(value = "同义词", index = 4)
	private String synonyms;

	@JsonAlias({ "businessDesc", "description", "desc" })
	@ExcelProperty(value = "业务描述", index = 5)
	private String businessDescription;

	@ExcelProperty(value = "字段注释", index = 6)
	private String columnComment;

	@NotBlank(message = "数据类型不能为空")
	@ExcelProperty(value = "数据类型*", index = 3)
	private String dataType;

	/**
	 * 创建时间（可选，用于导入时指定创建时间）
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@ExcelProperty(value = "创建时间", index = 7)
	private LocalDateTime createTime;

}
