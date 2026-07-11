package com.phoenix.common.vo.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录用户的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfo implements Serializable {
    private String id;
    // user admin
    private String type;
    private String username;
    private String name;
    private String sex;
    private String deptName;
    private String email;
    private String phone;
}
