package com.phoenix.privilege.service.impl;

import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.entity.PrivilegePvalue;
import com.phoenix.privilege.mapper.PrivilegePvalueMapper;
import com.phoenix.privilege.service.IPrivilegePvalueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PrivilegePvalueServiceImpl extends ServiceImpl<PrivilegePvalueMapper, PrivilegePvalue>
		implements IPrivilegePvalueService {

}
