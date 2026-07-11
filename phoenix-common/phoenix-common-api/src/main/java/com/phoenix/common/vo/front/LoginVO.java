package com.phoenix.common.vo.front;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private String userId;
    private String userCode;
    private String email;
    private String username;
    private String realName;
    private List<LoginGroupVO> groups;

    @Data
    public static class LoginGroupVO {
        private String sn;
        private String name;
    }
}
