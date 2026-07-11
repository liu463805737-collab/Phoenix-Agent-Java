package com.phoenix.privilege.base;

import lombok.Data;

import java.io.Serializable;

@Data
public class Query implements Serializable {

	private Integer pageNum = 1;

	private Integer pageSize = 10;

}
