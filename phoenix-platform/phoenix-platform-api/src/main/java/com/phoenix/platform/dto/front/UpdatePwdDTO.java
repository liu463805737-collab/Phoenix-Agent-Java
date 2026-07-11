package com.phoenix.platform.dto.front;

import lombok.Data;

/**
 * 修改密码请求参数
 */
@Data
public class UpdatePwdDTO {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 原密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
