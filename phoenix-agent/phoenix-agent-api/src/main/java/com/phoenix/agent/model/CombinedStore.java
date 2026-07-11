package com.phoenix.agent.model;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@Table("tbl_vector_store_combined")
public class CombinedStore implements Serializable {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private String id;
    private String namespace;
    private String keyName;
    private String valueJson;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
