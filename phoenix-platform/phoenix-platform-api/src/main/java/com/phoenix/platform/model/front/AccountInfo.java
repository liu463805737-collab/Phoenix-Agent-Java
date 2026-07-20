package com.phoenix.platform.model.front;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.phoenix.common.model.BaseModel;
import com.phoenix.common.vo.front.UserGroupVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 前台账号信息表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_platform_account_info")
public class AccountInfo extends BaseModel {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    /** 账号编码 */
    private String code;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 真实姓名 */
    private String realName;
    /** 昵称 */
    private String nickName;
    /** 生日 */
    private String birthday;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 头像地址 */
    private String avatarUrl;
    /**
     * 性别
     * @see com.phoenix.common.enm.GenderEnm
     */
    private String gender;
    /** 状态 0-禁用 1-启用 */
    private String status;
    /** 第三方平台ID */
    private String thirdPartyId;
    /** 关联员工ID */
    private String employeeId;
    /** 部门ID */
    private String deptId;
    /** 部门名称 */
    private String deptName;

    /** 关联的用户组列表 */
    @Column(ignore = true)
    private List<UserGroupVO> groups;
}
