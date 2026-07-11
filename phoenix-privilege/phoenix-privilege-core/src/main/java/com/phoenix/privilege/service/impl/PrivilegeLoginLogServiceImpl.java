package com.phoenix.privilege.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.privilege.entity.PrivilegeLoginLog;
import com.phoenix.privilege.mapper.PrivilegeLoginLogMapper;
import com.phoenix.privilege.service.IPrivilegeLoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class PrivilegeLoginLogServiceImpl extends ServiceImpl<PrivilegeLoginLogMapper, PrivilegeLoginLog>
		implements IPrivilegeLoginLogService {

}
