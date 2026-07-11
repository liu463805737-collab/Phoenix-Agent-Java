package com.phoenix.agent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.agent.mapper.UserMemoryInfoMapper;
import com.phoenix.agent.model.UserMemoryInfo;
import com.phoenix.agent.service.UserMemoryInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserMemoryInfoServiceImpl extends ServiceImpl<UserMemoryInfoMapper, UserMemoryInfo> implements UserMemoryInfoService {

    @Override
    @Transactional(readOnly = true)
    public UserMemoryInfo findById(String id) {
        return mapper.selectOneById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMemoryInfo> findByUserId(String userId) {
        return mapper.selectByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMemoryInfo> findByUserIdAndAgentSn(String userId, String agentSn) {
        return mapper.selectByUserIdAndAgentSn(userId, agentSn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMemoryInfo> findByUserIdAndAgentSnAndType(String userId, String agentSn, String memoryType) {
        return mapper.selectByUserIdAndAgentSnAndType(userId, agentSn, memoryType);
    }

    @Override
    public Boolean saveOrUpdateUserMemoryInfo(UserMemoryInfo memory) {
        if (memory.getCreatedAt() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        return this.saveOrUpdate(memory);
    }

    @Override
    public void deleteById(String id) {
        this.removeById(id);
        log.info("Deleted user memory: {}", id);
    }

}
