package com.phoenix.data.service.file;

import com.phoenix.data.properties.FileStorageProperties;
import com.phoenix.data.properties.OssStorageProperties;
import com.phoenix.data.service.file.impls.LocalFileStorageServiceImpl;
import com.phoenix.data.service.file.impls.OssFileStorageServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * 文件存储服务工厂，根据配置创建对应的存储服务实现
 */
@Component
@AllArgsConstructor
public class FileStorageServiceFactory implements FactoryBean<FileStorageService> {

	private final FileStorageProperties properties;

	private final OssStorageProperties ossProperties;

	/**
	 * 根据配置创建文件存储服务实例
	 * @return 文件存储服务
	 */
	@Override
	public FileStorageService getObject() {
		if (com.phoenix.data.enums.file.FileStorageServiceEnum.OSS.equals(properties.getType())) {
			return new OssFileStorageServiceImpl(properties, ossProperties);
		}
		else {
			return new LocalFileStorageServiceImpl(properties);
		}
	}

	/**
	 * 返回文件存储服务的类型
	 * @return 类型
	 */
	@Override
	public Class<?> getObjectType() {
		return FileStorageService.class;
	}

}
