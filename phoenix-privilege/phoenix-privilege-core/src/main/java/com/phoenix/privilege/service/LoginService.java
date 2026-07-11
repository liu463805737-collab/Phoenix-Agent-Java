package com.phoenix.privilege.service;

import com.phoenix.privilege.dto.LoginInfoDTO;
import com.phoenix.privilege.vo.LoginUserInfoVO;
import com.phoenix.privilege.vo.UserMenuVO;
import com.phoenix.tools.vo.ReturnVo;

public interface LoginService {

	ReturnVo<LoginUserInfoVO> login(LoginInfoDTO loginInfoDTO, String ip);

	ReturnVo<Void> logout();

	ReturnVo<UserMenuVO> getUserMenus();

}
