package com.phoenix.privilege.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.entity.PrivilegeAcl;
import com.phoenix.privilege.entity.PrivilegeModule;
import com.phoenix.privilege.vo.ModuleTreeVO;

import java.util.List;

public interface IPrivilegeModuleService extends IService<PrivilegeModule> {

	List<PrivilegeModule> getBySystemId(Long systemId);

	List<PrivilegeModule> getByPid(String pid);

	List<ModuleTreeVO> tree(Long systemId);

	List<ModuleTreeVO> tree(Long systemId, String releaseId);

	List<ModuleTreeVO> getModelTreeByUserId(String userId, List<PrivilegeAcl> acls);

}
