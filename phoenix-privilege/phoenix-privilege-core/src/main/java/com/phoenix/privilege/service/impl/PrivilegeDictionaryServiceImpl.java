package com.phoenix.privilege.service.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.entity.PrivilegeDictionary;
import com.phoenix.privilege.mapper.PrivilegeDictionaryMapper;
import com.phoenix.privilege.service.IPrivilegeDictionaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PrivilegeDictionaryServiceImpl extends ServiceImpl<PrivilegeDictionaryMapper, PrivilegeDictionary>
		implements IPrivilegeDictionaryService {

	@Override
	public List<PrivilegeDictionary> getBySystemSn(String systemSn) {
		return QueryChain.of(getMapper())
			.eq(PrivilegeDictionary::getSystemSn, systemSn)
			.orderBy(PrivilegeDictionary::getOrderNo, true)
			.list();
	}

	@Override
	public List<PrivilegeDictionary> getByPcode(String pcode) {
		return QueryChain.of(getMapper())
			.eq(PrivilegeDictionary::getPcode, pcode)
			.orderBy(PrivilegeDictionary::getOrderNo, true)
			.list();
	}

}
