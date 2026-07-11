package com.phoenix.data.service.hybrid.factory;

import com.phoenix.data.constant.Constant;
import com.phoenix.data.service.hybrid.fusion.FusionStrategy;
import com.phoenix.data.service.hybrid.fusion.impl.RrfFusionStrategy;
import com.phoenix.data.service.hybrid.fusion.impl.WeightedAverageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * FusionStrategy工厂类，根据配置创建相应的FusionStrategy实现类
 */
@Slf4j
@Component
public class FusionStrategyFactory implements FactoryBean<FusionStrategy> {

	@Value("${" + Constant.PROJECT_PROPERTIES_PREFIX + ".fusion-strategy:rrf}")
	/** 融合策略类型 */
	private String fusionStrategyType;

	/**
	 * 创建FusionStrategy实例
	 * @return FusionStrategy实例
	 */
	@Override
	public FusionStrategy getObject() throws Exception {
		log.info("Creating FusionStrategy with type: {}", fusionStrategyType);

		return switch (fusionStrategyType.toLowerCase()) {
			case "rrf" -> {
				log.info("Creating RrfFusionStrategy instance");
				yield new RrfFusionStrategy();
			}
			case "weighted" -> {
				log.info("Creating WeightedAverageStrategy instance");
				yield new WeightedAverageStrategy();
			}
			default -> {
				log.warn("Unknown fusion strategy type: {}, falling back to RrfFusionStrategy", fusionStrategyType);
				yield new RrfFusionStrategy();
			}
		};
	}

	/**
	 * 返回FusionStrategy的类型
	 * @return FusionStrategy类对象
	 */
	@Override
	public Class<?> getObjectType() {
		return FusionStrategy.class;
	}

	/**
	 * 设置为单例模式
	 * @return true
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

}
