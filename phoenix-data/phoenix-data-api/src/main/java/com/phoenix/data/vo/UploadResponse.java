package com.phoenix.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用上传响应实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

	private boolean success;

	private String message;

	private String url;

	private String filename;

	/**
	 * 创建成功上传响应
	 * @param message 消息
	 * @param url 文件URL
	 * @param filename 文件名
	 * @return UploadResponse实例
	 */
	public static UploadResponse ok(String message, String url, String filename) {
		UploadResponse r = new UploadResponse();
		r.setSuccess(true);
		r.setMessage(message);
		r.setUrl(url);
		r.setFilename(filename);
		return r;
	}

	/**
	 * 创建错误上传响应
	 * @param message 错误消息
	 * @return UploadResponse实例
	 */
	public static UploadResponse error(String message) {
		UploadResponse r = new UploadResponse();
		r.setSuccess(false);
		r.setMessage(message);
		return r;
	}

}
