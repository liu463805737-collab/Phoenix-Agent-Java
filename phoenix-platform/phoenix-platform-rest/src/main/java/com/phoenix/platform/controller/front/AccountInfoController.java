package com.phoenix.platform.controller.front;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.phoenix.data.entity.Agent;
import com.phoenix.platform.model.front.AccountInfo;
import com.phoenix.platform.service.front.AccountInfoService;import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台账号信息控制器
 */
@RestController
@RequestMapping("/platform/account-info")
@RequiredArgsConstructor
public class AccountInfoController {

    private final AccountInfoService accountInfoService;

    @GetMapping("/page")
    public ReturnVo<Page<AccountInfo>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            AccountInfo query) {
        return ReturnVo.ok(accountInfoService.page(new Page<>(page, size), query));
    }

    /**
     * 分页查询本组内未分配的人员
     * @param page
     * @param size
     * @param query
     * @return
     */
    @GetMapping("/getUnGroupPageByGroupId")
    public ReturnVo<Page<AccountInfo>> getUnGroupPage(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam String groupId,
                                            AccountInfo query) {
        return ReturnVo.ok(accountInfoService.getUnGroupPage(new Page<>(page, size), query, groupId));
    }

    /**
     * 获取我的智能体列表
     * @return
     */
    @GetMapping("/getMyAgents")
    public ReturnVo<List<Agent>> getMyAgents() {
        return ReturnVo.ok(accountInfoService.getMyAgents());
    }
    @GetMapping("/{id}")
    public ReturnVo<AccountInfo> getById(@PathVariable String id) {
        return ReturnVo.ok(accountInfoService.getById(id));
    }

    @GetMapping("/username/{username}")
    public ReturnVo<AccountInfo> getByUsername(@PathVariable String username) {
        return ReturnVo.ok(accountInfoService.getByUsername(username));
    }

    @GetMapping("/code/{code}")
    public ReturnVo<AccountInfo> getByCode(@PathVariable String code) {
        return ReturnVo.ok(accountInfoService.getByCode(code));
    }

    @GetMapping("/third-party/{thirdPartyId}")
    public ReturnVo<AccountInfo> getByThirdPartyId(@PathVariable String thirdPartyId) {
        return ReturnVo.ok(accountInfoService.getByThirdPartyId(thirdPartyId));
    }

    @GetMapping("/status/{status}")
    public ReturnVo<List<AccountInfo>> getByStatus(@PathVariable String status) {
        return ReturnVo.ok(accountInfoService.getByStatus(status));
    }

    @PostMapping
    public ReturnVo<Boolean> save(@RequestBody AccountInfo accountInfo) {
        accountInfo.setCreator(StpUtil.getLoginId().toString());
        return ReturnVo.ok(accountInfoService.save(accountInfo));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody AccountInfo accountInfo) {
        return ReturnVo.ok(accountInfoService.updateById(accountInfo));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(accountInfoService.deletePhysicallyById(id));
    }

    /**
     * 查询人员列表
     * @return
     */
    @GetMapping("/list")
    public ReturnVo<List<AccountInfo>> list(){
        // 查询启用的人员
        return ReturnVo.ok(accountInfoService.list(new QueryWrapper().eq("status", "1")));
    }

    /**
     * 批量启用/禁用账号
     */
    @PutMapping("/batch-status")
    public ReturnVo<Void> batchUpdateStatus(@RequestBody List<String> ids, @RequestParam String status) {
        accountInfoService.batchUpdateStatus(ids, status);
        return ReturnVo.ok();
    }
}
