package com.phoenix.platform.model.front;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.phoenix.common.model.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号-组织关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "tbl_platform_account_group_info")
public class AccountGroupInfo extends BaseModel {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    /** 组织ID */
    private String groupId;
    /** 账号ID */
    private String accountId;
    /** 组织名称 */
    private String groupName;
    /** 账号名称 */
    private String accountName;
}
