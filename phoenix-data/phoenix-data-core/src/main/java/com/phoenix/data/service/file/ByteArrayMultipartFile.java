package com.phoenix.data.service.file;

import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 简单的 MultipartFile 实现，用于将字节数组包装为 MultipartFile
 */
@AllArgsConstructor
public class ByteArrayMultipartFile implements MultipartFile {

	private final byte[] content;

	private final String filename;

	private final String contentType;

	/**
	 * 返回固定文件名 "file"
	 * @return 文件名
	 */
	@Override
	public String getName() {
		return "file";
	}

	/**
	 * 获取原始文件名
	 * @return 原始文件名
	 */
	@Override
	public String getOriginalFilename() {
		return filename;
	}

	/**
	 * 获取文件内容类型
	 * @return 内容类型
	 */
	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * 判断文件内容是否为空
	 * @return 是否为空
	 */
	@Override
	public boolean isEmpty() {
		return content == null || content.length == 0;
	}

	/**
	 * 获取文件大小
	 * @return 文件大小
	 */
	@Override
	public long getSize() {
		return content.length;
	}

	/**
	 * 获取文件字节数组
	 * @return 字节数组
	 */
	@Override
	public byte[] getBytes() {
		return content;
	}

	/**
	 * 获取文件输入流
	 * @return 输入流
	 */
	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(content);
	}

	/**
	 * 不支持此操作，直接抛出异常
	 * @param dest 目标文件
	 * @throws IOException IO异常
	 * @throws IllegalStateException 非法状态异常
	 */
	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		throw new UnsupportedOperationException("transferTo is not supported");
	}

}
