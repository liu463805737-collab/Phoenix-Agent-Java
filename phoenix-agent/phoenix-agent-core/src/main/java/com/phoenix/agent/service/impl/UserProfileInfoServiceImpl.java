package com.phoenix.agent.service.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.agent.mapper.UserProfileInfoMapper;
import com.phoenix.agent.model.UserProfileInfo;
import com.phoenix.agent.service.UserProfileInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserProfileInfoServiceImpl extends ServiceImpl<UserProfileInfoMapper, UserProfileInfo> implements UserProfileInfoService {

    @Override
    @Transactional(readOnly = true)
    public UserProfileInfo findByUserId(String userId) {
        return mapper.selectByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileInfo findByUserIdAndAgentSn(String userId, String agentSn) {
        return mapper.selectByUserIdAndAgentSn(userId, agentSn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileInfo> findByAgentSn(String agentSn) {
        return mapper.selectByAgentSn(agentSn);
    }

    @Override
    public Boolean saveOrUpdateUserProfileInfo(UserProfileInfo profile) {
        profile.setUpdatedAt(LocalDateTime.now());
        return this.saveOrUpdate(profile);
    }

    @Override
    public void deleteByUserIdAndAgentSn(String userId, String agentSn) {
        mapper.deleteByUserIdAndAgentSn(userId, agentSn);
        log.info("Deleted user profile: userId={} agentSn={}", userId, agentSn);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileInfo getByUserIdAndAgentSn(String userId, String agentSn) {
        return QueryChain.of(this.getMapper())
                .eq(UserProfileInfo::getUserId, userId)
                .eq(UserProfileInfo::getAgentSn, agentSn)
                .one();
    }
    @Override
    public void saveProfileUpdates(String userId, String agentSn, Map<String, String> newUpdates) {
        if (newUpdates == null || newUpdates.isEmpty()) {
            return;
        }
        UserProfileInfo profile = this.getByUserIdAndAgentSn(userId, agentSn);
        if (profile == null) {
            profile = UserProfileInfo.builder()
                    .userId(userId)
                    .agentSn(agentSn)
                    .profileData(new HashMap<>())
                    .build();
        }
        Map<String, String> currentData = profile.getProfileData();
        if (currentData == null) {
            currentData = new HashMap<>();
        }
        currentData.putAll(newUpdates);
        profile.setProfileData(currentData);
        profile.setUpdatedAt(LocalDateTime.now());
        this.saveOrUpdate(profile);
    }

}
