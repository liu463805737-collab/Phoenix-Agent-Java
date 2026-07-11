package com.phoenix.privilege.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.reactor.context.SaReactorHolder;
import cn.dev33.satoken.stp.StpUtil;
import com.phoenix.privilege.dto.LoginInfoDTO;
import com.phoenix.privilege.entity.PrivilegeUser;
import com.phoenix.privilege.enums.AuthErrorCode;
import com.phoenix.privilege.service.CaptchaService;
import com.phoenix.privilege.service.IPrivilegeUserService;
import com.phoenix.privilege.service.LoginService;
import com.phoenix.privilege.vo.CaptchaVO;
import com.phoenix.privilege.vo.LoginUserInfoVO;
import com.phoenix.privilege.vo.PrivilegeUserVO;
import com.phoenix.privilege.vo.UserMenuVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/privilege/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService authService;

    private final CaptchaService captchaService;
    private final IPrivilegeUserService privilegeUserService;


    @RequestMapping("doLogin")
    public Mono<String> doLogin(String username, String password) {
        return SaReactorHolder.sync(() -> {
            // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
            if ("zhang".equals(username) && "123456".equals(password)) {
                StpUtil.login(10001);
                return "登录成功";
            }
            return "登录失败";
        });
    }

    /**
     * 获取登录验证码
     */
    @GetMapping("/captcha")
    public ReturnVo<CaptchaVO> captcha() {
        return ReturnVo.ok(captchaService.generate());
    }

    /**
     * 用户登录
     *
     * @param loginInfoDTO 登录参数（用户名、密码、验证码）
     * @param request      用于获取客户端IP
     */
    @PostMapping("/login")
    @SaIgnore
    public Mono<ReturnVo<LoginUserInfoVO>> login(@RequestBody LoginInfoDTO loginInfoDTO, ServerHttpRequest request) {
        String ip = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return SaReactorHolder.sync(() -> authService.login(loginInfoDTO, ip));
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public Mono<ReturnVo<Void>> logout() {
        return SaReactorHolder.sync(() -> authService.logout());
    }

    /**
     * 获取当前登录用户的菜单及权限
     */
    @GetMapping("/menus")
    public Mono<ReturnVo<UserMenuVO>> menus() {
        return SaReactorHolder.sync(authService::getUserMenus);
    }

    @GetMapping("/getLoginUserInfo")
    public Mono<ReturnVo<PrivilegeUserVO>> getLoginUserInfo() {
        return SaReactorHolder.sync(() -> {
            PrivilegeUserVO vo = null;
            try {
                String loginId = (String) StpUtil.getLoginId();
                PrivilegeUser user = privilegeUserService.getById(loginId);
                vo = new PrivilegeUserVO();
                if (user != null) {
                    BeanUtils.copyProperties(user, vo);
                }
            } catch (Exception e) {
                return ReturnVo.fail(AuthErrorCode.TOKEN_INVALID.getMessage(), AuthErrorCode.TOKEN_INVALID.getCode());
            }
            return ReturnVo.ok(vo);
        });
    }

}
