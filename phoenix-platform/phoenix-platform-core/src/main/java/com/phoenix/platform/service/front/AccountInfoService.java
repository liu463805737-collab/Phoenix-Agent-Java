package com.phoenix.platform.service.front;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.phoenix.data.entity.Agent;
import com.phoenix.platform.dto.front.AccountLoginDTO;
import com.phoenix.platform.dto.front.ThirdPartyLoginDTO;
import com.phoenix.platform.dto.front.UpdatePwdDTO;
import com.phoenix.platform.model.front.AccountInfo;
import com.phoenix.tools.vo.ReturnVo;
import com.phoenix.common.vo.front.LoginVO;

import java.util.List;

/**
 * 前台账号信息服务接口
 */
public interface AccountInfoService extends IService<AccountInfo> {

	Page<AccountInfo> page(Page<AccountInfo> page, AccountInfo query);

	AccountInfo getByUsername(String username);

	AccountInfo getByCode(String code);

	AccountInfo getByThirdPartyId(String thirdPartyId);

	List<AccountInfo> getByStatus(String status);

	ReturnVo<LoginVO> login(AccountLoginDTO loginDTO);

	ReturnVo<LoginVO> thirdPartyLogin(ThirdPartyLoginDTO loginDTO);

	/**
	 * 登录后 - 修改密码
	 * @param dto
	 * @return
	 */
	boolean updatePassword(UpdatePwdDTO dto);

	ReturnVo<Void> logout() ;
	/**
	 * 获取我的智能体列表
	 * @return
	 */
	List<Agent> getMyAgents();

	void batchUpdateStatus(List<String> ids, String status);

	Page<AccountInfo> getUnGroupPage(Page<AccountInfo> page, AccountInfo query, String groupId);

	boolean deletePhysicallyById(String id);

}
