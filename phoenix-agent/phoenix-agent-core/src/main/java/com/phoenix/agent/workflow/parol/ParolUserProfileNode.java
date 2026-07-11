package com.phoenix.agent.workflow.parol;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.phoenix.common.vo.login.UserProfile;

import java.util.Map;
import java.util.Optional;

import static com.phoenix.agent.constant.PhoenixAgentConstant.RUNTIMECONTEXT_USERPROFILE;


public class ParolUserProfileNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        StringBuilder preMessage = new StringBuilder();
        Optional<UserProfile> userProfile = state.value(RUNTIMECONTEXT_USERPROFILE, UserProfile.class);
        if (userProfile.isPresent()) {
            UserProfile profile = userProfile.get();
            if (StrUtil.isNotBlank(profile.getName())){
                preMessage.append("\n").append("我的姓名：").append(profile.getName());
            }
            if (CollUtil.isNotEmpty(profile.getExtendInfos())){
                preMessage.append("\n").append("我负责的区域为 ").append(StrUtil.join(" ",  profile.getExtendInfos()));
            }
        }
        System.out.println("登录人信息：输入 -> " + preMessage.toString());
        return Map.of("userProfile_result", preMessage.toString());
    }
}
