package com.phoenix.privilege.entity;

import com.mybatisflex.annotation.Table;
import com.phoenix.privilege.base.BaseEntity;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table("tbl_privilege_acl")
public class PrivilegeAcl extends BaseEntity {

    /**
     * 授权允许
     */
    public static final int ACL_YES = 1;

    /**
     * 授权不允许
     */
    public static final int ACL_NO = 0;

    /**
     * 授权不确定
     */
    public static final int ACL_NEUTRAL = -1;

    /**
     * 来源id
     */
    private String releaseId;

    /**
     * 来源标识 role表示角色user 表示用户
     */
    private String releaseSn;

    /**
     * 系统标识
     */
    private String systemSn;

    /**
     * 模块id
     */
    private String moduleId;

    /**
     * 模块标识
     */
    private String moduleSn;

    private Integer aclState = 0;

    /**
     * 设置权限
     *
     * @param permission
     * @param yes
     */
    public void setPermission(int permission, boolean yes) {
        if (aclState == null) {
            aclState = 0;
        }
        Integer temp = 1;
        temp = temp << permission;
        if (yes) {
            aclState = aclState | temp;
        } else {
            aclState = aclState ^ temp;
        }
    }

    /**
     * 得到权限
     *
     * @param permission
     * @return
     */
    public int getPermission(int permission) {
        if (aclState == null) {
            aclState = 0;
        }
        Integer temp = 1;
        temp = temp << permission;
        temp = temp & aclState;
        if (temp > 0) {
            return ACL_YES;
        }
        return ACL_NO;
    }

}
