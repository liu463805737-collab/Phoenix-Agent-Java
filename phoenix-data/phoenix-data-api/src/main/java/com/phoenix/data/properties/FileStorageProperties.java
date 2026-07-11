package com.phoenix.data.properties;

import com.phoenix.data.constant.Constant;
import com.phoenix.data.enums.file.FileStorageServiceEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.nio.file.Path;

/**
 * 文件存储相关配置属性。
 */
@Getter
@Setter
@EnableConfigurationProperties({ OssStorageProperties.class })
@ConfigurationProperties(prefix = Constant.PROJECT_PROPERTIES_PREFIX + ".file")
public class FileStorageProperties {

	/**
	 * 存储类型：local（本地存储）、oss（阿里云OSS）
	 */
	private FileStorageServiceEnum type = FileStorageServiceEnum.LOCAL;

	/**
	 * 对象存储路径前缀（通用配置，对OSS和本地存储都适用）
	 */
	private String pathPrefix = "";

	/**
	 * 本地上传目录路径。
	 */
	private String path = "./uploads";

	/**
	 * 对外暴露的访问前缀。
	 */
	private String urlPrefix = "/uploads";

	/**
	 * 头像图片大小上限（字节）。默认 2MB。
	 */
	private long imageSize = 2L * 1024 * 1024;

	/**
	 * 获取本地保存路径,并规范化
	 * @return 本地保存根路径
	 */
	/**
	 * 获取本地保存路径，并规范化
	 * @return 本地保存根路径
	 */
	public Path getLocalBasePath() {
		return Path.of(path).normalize();
	}

}
