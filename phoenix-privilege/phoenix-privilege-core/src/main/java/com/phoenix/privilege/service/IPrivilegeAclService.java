package com.phoenix.privilege.service;

import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.dto.PrivilegeAclDTO;
import com.phoenix.privilege.entity.PrivilegeAcl;

import java.util.List;

public interface IPrivilegeAclService extends IService<PrivilegeAcl> {

	List<PrivilegeAcl> getByReleaseId(String releaseId);

	PrivilegeAcl getByReleaseIdAndModuleId(String releaseId, String moduleId);

	void saveAllAcl(String releaseId, Boolean checkStatus);

	void saveModuleAcl(PrivilegeAclDTO dto);

	List<PrivilegeAcl> getByUserId(String userId);

	/**
	 * 删除
	 * @param id
	 */
	Boolean deleteAcl(String id);

	void deleteAclByReleaseId(String releaseId);

}
