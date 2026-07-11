package com.phoenix.platform.model.thirdparty;

import com.mybatisflex.annotation.Table;
import com.phoenix.common.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Table("tbl_platform_dingtalk")
public class DingtalkEntity extends BaseModel {
    private String userCode;
    private String userName;
    private String userid;
    private String unionid;
    private String avatar;
}
