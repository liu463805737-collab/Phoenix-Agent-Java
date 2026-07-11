package com.phoenix.agent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.agent.mapper.UserAgentInfoMapper;
import com.phoenix.agent.model.UserAgentInfo;
import com.phoenix.agent.service.UserAgentInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserAgentInfoServiceImpl extends ServiceImpl<UserAgentInfoMapper, UserAgentInfo> implements UserAgentInfoService {

    @Override
    @Transactional(readOnly = true)
    public UserAgentInfo findById(String id) {
        return mapper.selectOneById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAgentInfo> findByUserId(String userId) {
        return mapper.selectByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAgentInfo findByUserIdAndAgentSn(String userId, String agentSn) {
        return mapper.selectByUserIdAndAgentSn(userId, agentSn);
    }

    @Override
    public Boolean saveOrUpdateUserAgentInfo(UserAgentInfo userAgentInfo) {
        if (userAgentInfo.getLastDate() == null) {
            userAgentInfo.setLastDate(new Date());
        }
        if (userAgentInfo.getActionCount() == null) {
            userAgentInfo.setActionCount(0L);
        }
        return this.saveOrUpdate(userAgentInfo);
    }

    @Override
    public UserAgentInfo recordAction(UserAgentInfo userAgentInfo) {
        UserAgentInfo info = mapper.selectByUserIdAndAgentSn(userAgentInfo.getUserId(), userAgentInfo.getAgentSn());
        if (info == null) {
            info = new UserAgentInfo();
            info.setUserId(userAgentInfo.getUserId());
            info.setAgentSn(userAgentInfo.getAgentSn());
            info.setActionCount(1L);
            info.setLastDate(new Date());
        } else {
            info.setActionCount(info.getActionCount() + 1);
            info.setLastDate(new Date());
        }
        this.saveOrUpdateUserAgentInfo(info);
        return info;
    }

    @Override
    public void deleteById(String id) {
        this.removeById(id);
        log.info("Deleted user-agent info: {}", id);
    }

}
