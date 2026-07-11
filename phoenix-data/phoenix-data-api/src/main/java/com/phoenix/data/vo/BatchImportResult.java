package com.phoenix.data.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量导入结果对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchImportResult {

	private int total;

	private int successCount;

	private int failCount;

	@Builder.Default
	private List<String> errors = new ArrayList<>();

	/**
	 * 添加错误信息
	 * @param error 错误消息
	 */
	public void addError(String error) {
		if (errors == null) {
			errors = new ArrayList<>();
		}
		errors.add(error);
	}

}
