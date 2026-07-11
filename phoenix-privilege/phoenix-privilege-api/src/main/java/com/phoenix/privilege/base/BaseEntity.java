package com.phoenix.privilege.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseEntity implements Serializable {

	@Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
	private String id;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(value = "create_time")
	private Date createTime = new Date();

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(value = "update_time")
	private Date updateTime;

	@Column("create_by")
	private String createBy;

	@Column("update_by")
	private String updateBy;

	@Column(isLogicDelete = true)
	protected Integer delFlag = 0;

}
