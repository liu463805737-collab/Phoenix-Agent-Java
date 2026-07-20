package com.phoenix.common.service.sync;

import cn.hutool.core.util.StrUtil;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.common.service.sync.dingtalk.DingTalkSyncStrategy;
import com.phoenix.common.service.sync.feishu.FeishuSyncStrategy;
import com.phoenix.common.service.sync.weixin.WeixinSyncStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformSyncFactory {

	private final PlatformInfoService platformInfoService;

	private final WeixinSyncStrategy weixinSyncStrategy;

	private final DingTalkSyncStrategy dingTalkSyncStrategy;

	private final FeishuSyncStrategy feishuSyncStrategy;

	private final Map<String, PlatformSyncStrategy> strategyMap = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		strategyMap.put(PlatformTypeEnm.WEIXIN.getCode(), weixinSyncStrategy);
		strategyMap.put(PlatformTypeEnm.DINGTALK.getCode(), dingTalkSyncStrategy);
		strategyMap.put(PlatformTypeEnm.FEISHU.getCode(), feishuSyncStrategy);
		log.info("平台支持同步的应用类型: {}", strategyMap.keySet());
	}

	public PlatformSyncStrategy getStrategy() {
		PlatformInfo platformInfo = platformInfoService.getEnabled();
		if (platformInfo == null || StrUtil.isBlank(platformInfo.getType())) {
			throw new IllegalStateException("未找到启用的平台配置，请在 tbl_platform_platform_info 中设置一条 status=1 的记录");
		}
		PlatformSyncStrategy strategy = strategyMap.get(platformInfo.getType());
		if (strategy == null) {
			throw new IllegalArgumentException("不支持的平台类型: " + platformInfo.getType() + "，支持: " + strategyMap.keySet());
		}
		log.info("使用同步策略: {} -> {}", platformInfo.getType(), strategy.getClass().getSimpleName());
		return strategy;
	}

}
