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
@Table("tbl_platform_tenant_info")
public class TenantInfo extends BaseModel {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    private String sn;
    private String name;
    private String description;
}
