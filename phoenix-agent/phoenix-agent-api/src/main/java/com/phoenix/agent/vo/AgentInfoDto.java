package com.phoenix.agent.vo;

import com.phoenix.common.vo.login.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 传入参数
 */
@Data
@NoArgsConstructor  // 添加无参构造器
@AllArgsConstructor
@Builder
public class AgentInfoDto implements Serializable {
    private String sn;
    private String message;
    private UserProfile userProfile;
}
