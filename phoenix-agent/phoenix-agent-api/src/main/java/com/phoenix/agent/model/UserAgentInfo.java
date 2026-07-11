package com.phoenix.agent.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Table(value = "tbl_agent_user_agent_info")
public class UserAgentInfo implements Serializable {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    private String userId;
    private String agentSn;
    private Long actionCount;
    private Date lastDate = new Date();
}
