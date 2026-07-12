package com.phoenix.common.model.platform;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.phoenix.common.model.BaseModel;
import lombok.Data;

/**
 * 三方平台信息
 */
@Data
@Table("tbl_platform_platform_info")
public class PlatformInfo extends BaseModel {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    /**
     * @see com.phoenix.common.enm.PlatformTypeEnm
     */
    private String type;
    private String name;
    /**
     * 启用状态 0 禁用 1为启用
     */
    private String status= "0";
    private String corpid;
    private String corpsecret;
    private String agentid;
}
