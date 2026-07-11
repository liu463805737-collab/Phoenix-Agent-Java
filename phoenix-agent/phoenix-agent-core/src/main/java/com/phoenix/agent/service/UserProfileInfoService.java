package com.phoenix.agent.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.agent.model.UserProfileInfo;

import java.util.List;
import java.util.Map;

public interface UserProfileInfoService extends IService<UserProfileInfo> {

    UserProfileInfo findByUserId(String userId);

    UserProfileInfo findByUserIdAndAgentSn(String userId, String agentSn);

    List<UserProfileInfo> findByAgentSn(String agentSn);

    Boolean saveOrUpdateUserProfileInfo(UserProfileInfo profile);

    void deleteByUserIdAndAgentSn(String userId, String agentSn);

    void saveProfileUpdates(String userId, String agentSn, Map<String, String> newUpdates);
    UserProfileInfo getByUserIdAndAgentSn(String userId, String agentSn);
}
