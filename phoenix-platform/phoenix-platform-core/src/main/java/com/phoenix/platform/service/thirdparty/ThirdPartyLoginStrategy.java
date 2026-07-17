package com.phoenix.platform.service.thirdparty;

import com.phoenix.common.model.platform.PlatformInfo;

public interface ThirdPartyLoginStrategy {

    String resolveUserId(String code, PlatformInfo platform);

    String getPlatformType();
}
