package com.phoenix.privilege.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.privilege.dto.PrivilegeUserDTO;
import com.phoenix.privilege.entity.PrivilegeUser;
import com.phoenix.privilege.vo.PrivilegeUserVO;

public interface IPrivilegeUserService extends IService<PrivilegeUser> {

	PrivilegeUser getByUsername(String username);

	PrivilegeUser getByCode(String code);

	boolean checkUsernameExist(String username);

	boolean checkMobileExist(String mobile);

	boolean updatePassword(String userId, String oldPassword, String newPassword);
	/** 管理员设置密码*/
	boolean setPassword(String userId, String newPassword);

	String resetPassword(String userId);

	boolean saveUser(PrivilegeUserDTO dto);

	boolean deleteUser(String id);

	Page<PrivilegeUserVO> pageByQuery(Page<PrivilegeUserVO> page, PrivilegeUserDTO dto);

}
