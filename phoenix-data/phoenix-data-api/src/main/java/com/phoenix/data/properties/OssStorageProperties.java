package com.phoenix.data.properties;

import com.phoenix.data.constant.Constant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云OSS存储相关配置属性。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = Constant.PROJECT_PROPERTIES_PREFIX + ".file.oss")
public class OssStorageProperties {

	/**
	 * OSS访问密钥ID
	 */
	private String accessKeyId;

	/**
	 * OSS访问密钥Secret
	 */
	private String accessKeySecret;

	/**
	 * OSS端点地址
	 */
	private String endpoint;

	/**
	 * OSS存储桶名称
	 */
	private String bucketName;

	/**
	 * 自定义域名（可选）
	 */
	private String customDomain;

}
