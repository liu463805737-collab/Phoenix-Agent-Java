package com.phoenix.data.controller;

import com.phoenix.data.properties.FileStorageProperties;
import com.phoenix.data.service.file.FileStorageService;
import com.phoenix.data.vo.UploadResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件上传控制器
 *
 * @author Makoto
 * @since 2025/9/19
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class FileUploadController {

	private final FileStorageProperties fileStorageProperties;

	private final FileStorageService fileStorageService;

	/**
	 * 上传头像图片
	 */
	@PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<ResponseEntity<UploadResponse>> uploadAvatar(@RequestPart("file") FilePart file) {
		// 验证文件类型
		String contentType = file.headers().getContentType() != null ? file.headers().getContentType().toString()
				: null;
		if (contentType == null || !contentType.startsWith("image/")) {
			return Mono.just(ResponseEntity.badRequest().body(UploadResponse.error("只支持图片文件")));
		}

		if (file.headers().getContentLength() > fileStorageProperties.getImageSize()) {
			return Mono.just(ResponseEntity.badRequest().body(UploadResponse.error("图片大小超过最大限制")));
		}

		// 使用文件存储服务存储文件
		return fileStorageService.storeFile(file, "avatars").map(filePath -> {
			String fileUrl = fileStorageService.getFileUrl(filePath);
			// 提取文件名
			String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
			return ResponseEntity.ok(UploadResponse.ok("上传成功", fileUrl, filename));
		}).onErrorResume(e -> {
			log.error("头像上传失败", e);
			return Mono
				.just(ResponseEntity.internalServerError().body(UploadResponse.error("上传失败: " + e.getMessage())));
		});
	}

	/**
	 * 获取文件
	 */
	@GetMapping("/**")
	public ResponseEntity<byte[]> getFile(ServerHttpRequest request) {
		try {
			String requestMapPath = this.getClass().getAnnotation(RequestMapping.class).value()[0];
			String requestPath = request.getPath().value();
			String urlPrefix = fileStorageProperties.getUrlPrefix();
			String requestPrefix = requestMapPath + urlPrefix + "/";
			if (!requestPath.startsWith(requestPrefix)) {
				return ResponseEntity.badRequest().build();
			}
			String filePath = requestPath.substring(requestPrefix.length());
			if (filePath.isBlank()) {
				return ResponseEntity.badRequest().build();
			}

			Path basePath = fileStorageProperties.getLocalBasePath().toAbsolutePath().normalize();
			Path fullPath = basePath.resolve(filePath).normalize();
			if (!fullPath.startsWith(basePath)) {
				return ResponseEntity.status(403).build();
			}

			if (!Files.exists(fullPath) || Files.isDirectory(fullPath)) {
				return ResponseEntity.notFound().build();
			}

			byte[] fileContent = Files.readAllBytes(fullPath);
			String contentType = Files.probeContentType(fullPath);

			return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
				.body(fileContent);

		}
		catch (IOException e) {
			log.error("文件读取失败", e);
			return ResponseEntity.internalServerError().build();
		}
	}

}
