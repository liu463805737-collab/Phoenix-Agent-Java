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
 * 组织信息表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tbl_platform_group_info")
public class GroupInfo extends BaseModel {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    /** 组织名称 */
    private String name;
    /** 组织编码 */
    private String sn;
    /** 组描述 */
    private String description;
    /** 0启用，1禁用 */
    private Integer status;
}
