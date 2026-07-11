package com.phoenix.data.service.file.impls;

import com.phoenix.data.properties.FileStorageProperties;
import com.phoenix.data.service.file.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 */
@Slf4j
@AllArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

	private final FileStorageProperties fileStorageProperties;

	/**
	 * 存储文件（响应式版本）
	 * @param filePart 上传的文件
	 * @param subPath 子路径
	 * @return 存储后的文件路径
	 */
	@Override
	public Mono<String> storeFile(FilePart filePart, String subPath) {
		String originalFilename = filePart.filename();
		String extension = "";
		if (originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		String filename = UUID.randomUUID() + extension;

		String storagePath = buildStoragePath(subPath, filename);

		Path filePath = fileStorageProperties.getLocalBasePath().resolve(storagePath);

		checkPathSecurity(filePath);

		return Mono.fromCallable(() -> {
			Path uploadDir = filePath.getParent();
			if (!Files.exists(uploadDir)) {
				Files.createDirectories(uploadDir);
			}
			return filePath;
		}).subscribeOn(Schedulers.boundedElastic()).flatMap(filePart::transferTo).then(Mono.fromCallable(() -> {
			log.info("文件存储成功: {}", storagePath);
			return storagePath;
		}));
	}

	/**
	 * 存储文件（同步版本）
	 * @param file 上传的文件
	 * @param subPath 子路径
	 * @return 存储后的文件路径
	 */
	@Override
	public String storeFile(MultipartFile file, String subPath) {
		try {
			String originalFilename = file.getOriginalFilename();
			String extension = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}
			String filename = UUID.randomUUID() + extension;

			String storagePath = buildStoragePath(subPath, filename);

			Path filePath = fileStorageProperties.getLocalBasePath().resolve(storagePath);

			checkPathSecurity(filePath);

			Path uploadDir = filePath.getParent();
			if (!Files.exists(uploadDir)) {
				Files.createDirectories(uploadDir);
			}
			Files.copy(file.getInputStream(), filePath);

			log.info("文件存储成功: {}", storagePath);
			return storagePath;

		}
		catch (IOException e) {
			log.error("文件存储失败", e);
			throw new RuntimeException("文件存储失败: " + e.getMessage(), e);
		}
	}

	/**
	 * 删除文件（幂等操作）
	 * @param filePath 文件路径
	 * @return 是否删除成功
	 */
	@Override
	public boolean deleteFile(String filePath) {
		try {
			Path fullPath = fileStorageProperties.getLocalBasePath().resolve(filePath);
			checkPathSecurity(fullPath);
			if (Files.exists(fullPath)) {
				Files.deleteIfExists(fullPath);
				log.info("成功删除文件: {}", filePath);
			}
			else {
				// 删除是个等幂的操作，不存在也是当做被删除了
				log.info("文件不存在，跳过删除，视为成功: {}", filePath);
			}
			return true;
		}
		catch (IOException e) {
			log.error("删除文件失败: {}", filePath, e);
			return false;
		}
	}

	/**
	 * 获取文件访问URL
	 * @param filePath 文件路径
	 * @return 访问URL
	 */
	@Override
	public String getFileUrl(String filePath) {
		checkPathSecurity(fileStorageProperties.getLocalBasePath().resolve(filePath));
		// 返回相对路径，前端会自动基于当前域名访问
		return fileStorageProperties.getUrlPrefix() + "/" + filePath;
	}

	/**
	 * 获取文件资源对象
	 * @param filePath 文件路径
	 * @return 文件资源
	 */
	@Override
	public Resource getFileResource(String filePath) {
		Path fullPath = fileStorageProperties.getLocalBasePath().resolve(filePath);
		checkPathSecurity(fullPath);
		if (Files.exists(fullPath)) {
			return new FileSystemResource(fullPath);
		}
		else {
			throw new RuntimeException("File is not exist: " + filePath);
		}
	}

	/**
	 * 检查文件访问路径是否安全
	 * @param filePath 文件访问路径
	 */
	private void checkPathSecurity(Path filePath) {
		if (!filePath.normalize().startsWith(fileStorageProperties.getLocalBasePath())) {
			throw new SecurityException("Invalid file path");
		}
	}

	/**
	 * 构建本地存储路径
	 */
	private String buildStoragePath(String subPath, String filename) {
		StringBuilder pathBuilder = new StringBuilder();

		if (StringUtils.hasText(fileStorageProperties.getPathPrefix())) {
			pathBuilder.append(fileStorageProperties.getPathPrefix()).append("/");
		}

		if (StringUtils.hasText(subPath)) {
			pathBuilder.append(subPath).append("/");
		}

		pathBuilder.append(filename);

		return pathBuilder.toString();
	}

}
