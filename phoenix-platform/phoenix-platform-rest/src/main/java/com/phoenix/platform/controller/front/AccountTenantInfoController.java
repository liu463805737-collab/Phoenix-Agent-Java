package com.phoenix.platform.controller.front;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.platform.model.front.AccountTenantInfo;
import com.phoenix.platform.model.front.TenantInfo;
import com.phoenix.platform.service.front.AccountTenantInfoService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platform/account-tenant-info")
@RequiredArgsConstructor
public class AccountTenantInfoController {

    private final AccountTenantInfoService accountTenantInfoService;

    @GetMapping("/page")
    public ReturnVo<Page<AccountTenantInfo>> page(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  AccountTenantInfo query) {
        return ReturnVo.ok(accountTenantInfoService.page(new Page<>(page, size), query));
    }

    @GetMapping("/{id}")
    public ReturnVo<AccountTenantInfo> getById(@PathVariable String id) {
        return ReturnVo.ok(accountTenantInfoService.getById(id));
    }

    @GetMapping("/account/{accountId}")
    public ReturnVo<List<TenantInfo>> getByAccountId(@PathVariable String accountId) {
        return ReturnVo.ok(accountTenantInfoService.getByAccountId(accountId));
    }

    @PostMapping
    public ReturnVo<Boolean> save(@RequestBody AccountTenantInfo accountTenantInfo) {
        return ReturnVo.ok(accountTenantInfoService.save(accountTenantInfo));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody AccountTenantInfo accountTenantInfo) {
        return ReturnVo.ok(accountTenantInfoService.updateById(accountTenantInfo));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(accountTenantInfoService.removeById(id));
    }
}
