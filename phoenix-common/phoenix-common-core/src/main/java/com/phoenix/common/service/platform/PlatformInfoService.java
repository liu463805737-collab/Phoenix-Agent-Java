package com.phoenix.common.service.platform;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.common.model.platform.PlatformInfo;

import java.util.List;

public interface PlatformInfoService extends IService<PlatformInfo> {

    Page<PlatformInfo> page(Page<PlatformInfo> page, PlatformInfo query);

    PlatformInfo getByType(String type);

    PlatformInfo getEnabledByType(String type);

    PlatformInfo getEnabled();

    List<PlatformInfo> queryList(PlatformInfo query);

}
