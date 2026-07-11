package com.phoenix.data.service.file;

import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface FileStorageService {

	/**
	 * 存储文件（响应式版本，用于 WebFlux Controller）
	 * @param filePart 上传的文件
	 * @param subPath 子路径（如 "avatars"）
	 * @return 存储后的文件路径
	 */
	Mono<String> storeFile(FilePart filePart, String subPath);

	/**
	 * 存储文件（同步版本，用于传统同步代码）
	 * @param file 上传的文件
	 * @param subPath 子路径（如 "avatars"）
	 * @return 存储后的文件路径
	 */
	String storeFile(MultipartFile file, String subPath);

	/**
	 * 删除文件
	 * @param filePath 文件路径
	 * @return 是否删除成功
	 */
	boolean deleteFile(String filePath);

	/**
	 * 获取文件访问URL
	 * @param filePath 文件路径
	 * @return 访问URL
	 */
	String getFileUrl(String filePath);

	/**
	 * 获取文件资源对象
	 * @param filePath 文件路径
	 * @return 文件资源对象
	 */
	Resource getFileResource(String filePath);

}
