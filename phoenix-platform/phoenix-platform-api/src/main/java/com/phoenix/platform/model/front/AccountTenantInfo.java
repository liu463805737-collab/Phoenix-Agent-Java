package com.phoenix.platform.model.front;


import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.phoenix.common.model.BaseModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Table("tbl_platform_account_tenant_info")
public class AccountTenantInfo extends BaseModel {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    private String accountId;
    private String tenantId;
}
