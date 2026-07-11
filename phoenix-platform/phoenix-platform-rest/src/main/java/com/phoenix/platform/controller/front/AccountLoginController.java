package com.phoenix.platform.controller.front;

import cn.dev33.satoken.reactor.context.SaReactorHolder;
import cn.dev33.satoken.stp.StpUtil;
import com.phoenix.platform.dto.front.AccountLoginDTO;
import com.phoenix.platform.dto.front.UpdatePwdDTO;
import com.phoenix.platform.service.front.AccountInfoService;
import com.phoenix.common.vo.front.LoginVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AccountLoginController {

    private final AccountInfoService accountInfoService;

    @PostMapping("/login")
    public Mono<ReturnVo<LoginVO>> login(@RequestBody AccountLoginDTO loginDTO) {
        return SaReactorHolder.sync(() -> accountInfoService.login(loginDTO));
    }

    @PostMapping("/logout")
    public Mono<ReturnVo<Void>> logout() {
        return SaReactorHolder.sync(accountInfoService::logout);
    }

    @PutMapping("/updatePassword")
    public Mono<ReturnVo<Void>> updatePassword(@RequestBody UpdatePwdDTO dto) {
        return SaReactorHolder.sync(() -> {
            dto.setUserId(StpUtil.getLoginIdAsString());
            if (!accountInfoService.updatePassword(dto)) {
                return ReturnVo.fail("原密码错误");
            }
            return ReturnVo.ok("密码修改成功");
        });
    }

}
