package com.phoenix.platform.controller.front;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.platform.model.front.AccountGroupInfo;
import com.phoenix.platform.service.front.AccountGroupInfoService;
import com.phoenix.common.vo.front.UserGroupVO;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账号-组织关联控制器
 */
@RestController
@RequestMapping("/platform/account-group-info")
@RequiredArgsConstructor
public class AccountGroupInfoController {

    private final AccountGroupInfoService accountGroupInfoService;

    @GetMapping("/page")
    public ReturnVo<Page<AccountGroupInfo>> page(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 AccountGroupInfo query) {
        return ReturnVo.ok(accountGroupInfoService.page(new Page<>(page, size), query));
    }

    @GetMapping("/{id}")
    public ReturnVo<AccountGroupInfo> getById(@PathVariable String id) {
        return ReturnVo.ok(accountGroupInfoService.getById(id));
    }

    @GetMapping("/group/{groupId}")
    public ReturnVo<List<AccountGroupInfo>> getByGroupId(@PathVariable String groupId) {
        return ReturnVo.ok(accountGroupInfoService.getByGroupId(groupId));
    }

    /**
     * 查询用户组（含组名称和描述）
     * @param accountId
     * @return
     */
    @GetMapping("/account/{accountId}")
    public ReturnVo<List<UserGroupVO>> getByAccountId(@PathVariable String accountId) {
        return ReturnVo.ok(accountGroupInfoService.getUserGroupsByAccountId(accountId));
    }

    @PostMapping
    public ReturnVo<Boolean> save(@RequestBody AccountGroupInfo accountGroupInfo) {
        return ReturnVo.ok(accountGroupInfoService.save(accountGroupInfo));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody AccountGroupInfo accountGroupInfo) {
        return ReturnVo.ok(accountGroupInfoService.updateById(accountGroupInfo));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(accountGroupInfoService.removeById(id));
    }

    /**
     * 删除关联的用户组
     * @param groupId
     * @param accountId
     * @return
     */
    @DeleteMapping("/group/{groupId}/account/{accountId}")
    public ReturnVo<Boolean> deleteByGroupIdAndAccountId(@PathVariable String groupId, @PathVariable String accountId) {
        return ReturnVo.ok(accountGroupInfoService.deleteByGroupIdAndAccountId(groupId, accountId));
    }
}
