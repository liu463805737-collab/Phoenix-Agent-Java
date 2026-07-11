package com.phoenix.common.vo.login;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户逇画像
 */
@Data
@Builder
public class UserProfile implements Serializable {
    private String sessionId;
    private String userCode;
    private String userId;
    private String account;
    private String name;
    private String occupation;
    private String telephone;
    private List<String> extendInfos;
    private String email;
    private List<String> tenantSns;
    private List<String> tags;
    private List<UserGroup> groups;

    @Data
    public static class UserGroup{
        private String sn;
        private String name;
    }
}
