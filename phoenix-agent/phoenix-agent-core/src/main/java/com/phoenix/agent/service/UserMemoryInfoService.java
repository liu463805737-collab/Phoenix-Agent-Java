package com.phoenix.agent.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.agent.model.UserMemoryInfo;

import java.util.List;

public interface UserMemoryInfoService extends IService<UserMemoryInfo> {

    UserMemoryInfo findById(String id);

    List<UserMemoryInfo> findByUserId(String userId);

    List<UserMemoryInfo> findByUserIdAndAgentSn(String userId, String agentSn);

    List<UserMemoryInfo> findByUserIdAndAgentSnAndType(String userId, String agentSn, String memoryType);

    Boolean saveOrUpdateUserMemoryInfo(UserMemoryInfo memory);

    void deleteById(String id);

}
