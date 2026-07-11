package com.phoenix.agent.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.agent.model.UserAgentInfo;

import java.util.List;

public interface UserAgentInfoService extends IService<UserAgentInfo> {

    UserAgentInfo findById(String id);

    List<UserAgentInfo> findByUserId(String userId);

    UserAgentInfo findByUserIdAndAgentSn(String userId, String agentSn);

    Boolean saveOrUpdateUserAgentInfo(UserAgentInfo userAgentInfo);

    UserAgentInfo recordAction(UserAgentInfo userAgentInfo);

    void deleteById(String id);

}
