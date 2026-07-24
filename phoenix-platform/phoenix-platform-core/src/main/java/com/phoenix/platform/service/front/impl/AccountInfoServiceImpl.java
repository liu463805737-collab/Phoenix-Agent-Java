package com.phoenix.platform.service.front.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.jwt.JWTUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.data.entity.Agent;
import com.phoenix.data.enums.AgentStatusEnm;
import com.phoenix.data.service.agent.AgentService;
import com.phoenix.platform.constant.PlatformConstant;
import com.phoenix.platform.dto.front.AccountLoginDTO;
import com.phoenix.platform.dto.front.ThirdPartyLoginDTO;
import com.phoenix.platform.dto.front.UpdatePwdDTO;
import com.phoenix.platform.mapper.front.AccountInfoMapper;
import com.phoenix.platform.model.front.AccountGroupInfo;
import com.phoenix.platform.model.front.AccountInfo;
import com.phoenix.platform.model.front.GroupAgentInfo;
import com.phoenix.platform.model.front.GroupInfo;
import com.phoenix.common.vo.front.UserGroupVO;
import com.phoenix.platform.service.front.AccountGroupInfoService;
import com.phoenix.platform.service.front.AccountInfoService;
import com.phoenix.platform.service.front.GroupAgentInfoService;
import com.phoenix.platform.service.front.GroupInfoService;
import com.phoenix.platform.service.thirdparty.ThirdPartyLoginFactory;
import com.phoenix.platform.service.thirdparty.ThirdPartyLoginStrategy;
import com.phoenix.common.vo.front.LoginVO;
import com.phoenix.privilege.constant.LoginConstant;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.phoenix.platform.constant.PlatformConstant.ACCOUNT_LOGIN;

@Slf4j

/**
 * 前台账号信息服务实现
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class AccountInfoServiceImpl extends ServiceImpl<AccountInfoMapper, AccountInfo> implements AccountInfoService {
    private final AccountGroupInfoService accountGroupInfoService;
    private final GroupInfoService groupInfoService;
    private final GroupAgentInfoService groupAgentInfoService;
    private final AgentService agentService;
    private final PlatformInfoService platformInfoService;
    private final ThirdPartyLoginFactory thirdPartyLoginFactory;

    @Override
    public List<Agent> getMyAgents() {
        /**
         * 1、获取用户的角色列表
         * 2、获取角色的智能体列表
         * 3、合并返回
         */
        List<Agent> datas = null;
        String userId = StpUtil.getLoginIdAsString();
        List<AccountGroupInfo> ags = accountGroupInfoService.getByAccountId(userId);
        if (CollUtil.isNotEmpty(ags)) {
            List<String> gids = CollStreamUtil.toList(ags, AccountGroupInfo::getGroupId);
            if (CollUtil.isNotEmpty(gids)) {
                List<GroupAgentInfo> gas = groupAgentInfoService.getByGroupIds(gids);
                if (CollUtil.isNotEmpty(gas)) {
                    List<Long> agentIds = CollStreamUtil.toList(gas, GroupAgentInfo::getAgentId);
                    datas = agentService.findByIds(agentIds, AgentStatusEnm.PUBLISHED.getCode());
                }
            }
        }
        return datas;
    }

	@Override
	public Page<AccountInfo> page(Page<AccountInfo> page, AccountInfo query) {
        String keyword = query.getKeyword();
		QueryWrapper qw = QueryWrapper.create()
			.eq(AccountInfo::getDeptId, query.getDeptId(), StrUtil.isNotBlank(query.getDeptId()))
			.eq(AccountInfo::getStatus, query.getStatus(), StrUtil.isNotBlank(query.getStatus()))
			.orderBy(AccountInfo::getCreateTime, false);
        if (StrUtil.isNotBlank(keyword)) {
            qw.and((Consumer<QueryWrapper>) q -> q.like(AccountInfo::getRealName, keyword)
                    .or(AccountInfo::getCode)
                    .like(keyword)
                    .or(AccountInfo::getPhone)
                    .like(keyword));
        }
		Page<AccountInfo> result = this.page(page, qw);
		List<AccountInfo> records = result.getRecords();
		if (CollUtil.isNotEmpty(records)) {
			records.forEach(r -> r.setPassword(null));
		}
		if (CollUtil.isNotEmpty(records)) {
			List<String> accountIds = records.stream().map(AccountInfo::getId).collect(Collectors.toList());
			List<AccountGroupInfo> agList = accountGroupInfoService.list(
				QueryWrapper.create().in(AccountGroupInfo::getAccountId, accountIds));
			if (CollUtil.isNotEmpty(agList)) {
				Map<String, List<AccountGroupInfo>> groupMap = agList.stream()
					.collect(Collectors.groupingBy(AccountGroupInfo::getAccountId));
				List<String> groupIds = agList.stream()
					.map(AccountGroupInfo::getGroupId).distinct().collect(Collectors.toList());
				Map<String, GroupInfo> groupInfoMap = groupInfoService.listByIds(groupIds).stream()
					.collect(Collectors.toMap(GroupInfo::getId, g -> g));
				records.forEach(account -> {
					List<AccountGroupInfo> accountGroups = groupMap.get(account.getId());
					if (CollUtil.isNotEmpty(accountGroups)) {
						account.setGroups(accountGroups.stream()
							.map(ag -> {
								GroupInfo gi = groupInfoMap.get(ag.getGroupId());
								return UserGroupVO.builder()
									.groupId(ag.getGroupId())
									.groupName(ag.getGroupName())
									.description(gi != null ? gi.getDescription() : null)
									.build();
							})
							.collect(Collectors.toList()));
					}
				});
			}
		}
		return result;
	}

    @Override
    public AccountInfo getByUsername(String username) {
        return QueryChain.of(this.getMapper()).eq(AccountInfo::getUsername, username).one();
    }

    @Override
    public AccountInfo getByCode(String code) {
        return QueryChain.of(this.getMapper()).eq(AccountInfo::getCode, code).one();
    }

    @Override
    public AccountInfo getByThirdPartyId(String thirdPartyId) {
        return QueryChain.of(this.getMapper()).eq(AccountInfo::getThirdPartyId, thirdPartyId).one();
    }

    @Override
    public List<AccountInfo> getByStatus(String status) {
        return QueryChain.of(this.getMapper())
                .eq(AccountInfo::getStatus, status)
                .orderBy(AccountInfo::getCreateTime, false)
                .list();
    }

    @Override
    public void batchUpdateStatus(List<String> ids, String status) {
        AccountInfo entity = AccountInfo.builder().status(status).build();
        QueryWrapper qw = QueryWrapper.create().in(AccountInfo::getId, ids);
        this.getMapper().updateByQuery(entity, qw);
    }

    @Override
    public Page<AccountInfo> getUnGroupPage(Page<AccountInfo> page, AccountInfo query, String groupId) {
        return QueryChain.of(this.getMapper())
                .like(AccountInfo::getUsername, query.getUsername(), StrUtil.isNotBlank(query.getUsername()))
                .like(AccountInfo::getRealName, query.getRealName(), StrUtil.isNotBlank(query.getRealName()))
                .like(AccountInfo::getPhone, query.getPhone(), StrUtil.isNotBlank(query.getPhone()))
                .eq(AccountInfo::getCode, query.getCode(), StrUtil.isNotBlank(query.getCode()))
                .eq(AccountInfo::getStatus, query.getStatus(), StrUtil.isNotBlank(query.getStatus()))
                .eq(AccountInfo::getEmail, query.getEmail(), StrUtil.isNotBlank(query.getEmail()))
                .and("id NOT IN (SELECT account_id FROM tbl_platform_account_group_info WHERE group_id = ?)", groupId)
                .orderBy(AccountInfo::getCreateTime, false)
                .page(page);
    }

    @Override
    public ReturnVo<Void> logout() {
        StpUtil.logout();
        return ReturnVo.ok(LoginConstant.LOGOUT_SUCCESS);
    }

    @Override
    public boolean updatePassword(UpdatePwdDTO dto) {
        AccountInfo user = getById(dto.getUserId());
        if (user == null) {
            return false;
        }
        String hashedOld = SecureUtil.md5(PlatformConstant.PASSWORD_SALT + dto.getOldPassword());
        if (!hashedOld.equals(user.getPassword())) {
            return false;
        }
        user.setPassword(dto.getNewPassword());
        return updateById(user);
    }

    @Override
    public ReturnVo<LoginVO> login(AccountLoginDTO loginDTO) {
        AccountInfo account = getByUsername(loginDTO.getUsername());
        if (account == null) {
            return ReturnVo.fail("用户名或密码错误");
        }
        if ("0".equals(account.getStatus())) {
            return ReturnVo.fail("账户已被禁用");
        }
        String hashedPassword = SecureUtil.md5(PlatformConstant.PASSWORD_SALT + loginDTO.getPassword());
        if (!hashedPassword.equals(account.getPassword())) {
            return ReturnVo.fail("用户名或密码错误");
        }
        StpUtil.login(account.getId());
        return buildLoginResult(account);
    }

    @Override
    public ReturnVo<LoginVO> thirdPartyLogin(ThirdPartyLoginDTO loginDTO) {
        if (loginDTO == null || StrUtil.isBlank(loginDTO.getPlatform()) || StrUtil.isBlank(loginDTO.getCode())) {
            return ReturnVo.fail("参数不完整");
        }

        PlatformInfo platform = platformInfoService.getEnabledByType(loginDTO.getPlatform());
        if (platform == null) {
            return ReturnVo.fail("未找到启用的平台配置");
        }

        ThirdPartyLoginStrategy strategy = thirdPartyLoginFactory.getStrategy(loginDTO.getPlatform());
        String thirdPartyId;
        try {
            thirdPartyId = strategy.resolveUserId(loginDTO.getCode(), platform);
        } catch (Exception e) {
            log.error("解析三方用户ID失败, platform: {}, error: ", loginDTO.getPlatform(), e);
            return ReturnVo.fail("授权验证失败");
        }
        if (StrUtil.isBlank(thirdPartyId)) {
            return ReturnVo.fail("授权验证失败");
        }

        AccountInfo account = getByThirdPartyId(thirdPartyId);
        if (account == null) {
            return ReturnVo.fail("未绑定平台账号，请联系管理员");
        }
        if ("0".equals(account.getStatus())) {
            return ReturnVo.fail("账户已被禁用");
        }

        StpUtil.login(account.getId());
        return buildLoginResult(account);
    }

    private ReturnVo<LoginVO> buildLoginResult(AccountInfo account) {
        List<GroupInfo> groupInfos = groupInfoService.getByLoginId(account.getId());
        List<LoginVO.LoginGroupVO> groupVOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(groupInfos)) {
            groupInfos.forEach(groupInfo -> {
                LoginVO.LoginGroupVO groupVO = new LoginVO.LoginGroupVO();
                groupVO.setSn(groupInfo.getSn());
                groupVO.setName(groupInfo.getName());
                groupVOS.add(groupVO);
            });
        }
        String token = StpUtil.getTokenValue();
        LoginVO loginVO = LoginVO.builder()
                .token(token)
                .userCode(account.getCode())
                .email(account.getEmail())
                .userId(account.getId())
                .username(account.getUsername())
                .realName(account.getRealName())
                .groups(groupVOS)
                .build();
        StpUtil.getSession().set(ACCOUNT_LOGIN, loginVO);
        return ReturnVo.ok(loginVO);
    }

    private String generateToken(AccountInfo account) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", account.getId());
        payload.put("username", account.getUsername());
        long exp = DateUtil.offsetDay(new Date(), 7).getTime();
        payload.put("exp", exp);
        String secret = PlatformConstant.PASSWORD_SALT + "_jwt_secret";
        return JWTUtil.createToken(payload, secret.getBytes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(AccountInfo entity) {
        if (StrUtil.isNotBlank(entity.getPassword())) {
            entity.setPassword(SecureUtil.md5(PlatformConstant.PASSWORD_SALT + entity.getPassword()));
        }
        boolean result = super.save(entity);
        if (result) {
            GroupInfo group = groupInfoService.getBySn(PlatformConstant.GROUP_SN_COMMON);
            if (group != null) {
                AccountGroupInfo agi = AccountGroupInfo.builder()
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .accountId(entity.getId())
                        .accountName(entity.getUsername())
                        .build();
                accountGroupInfoService.save(agi);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(AccountInfo entity) {
        if (StrUtil.isNotBlank(entity.getPassword())) {
            entity.setPassword(SecureUtil.md5(PlatformConstant.PASSWORD_SALT + entity.getPassword()));
        }
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePhysicallyById(String id) {
        accountGroupInfoService.deleteByAccountId(id);
        return this.getMapper().deletePhysicallyById(id);
    }

}
