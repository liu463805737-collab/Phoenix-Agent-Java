package com.phoenix.platform.controller.front;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.platform.model.front.GroupInfo;
import com.phoenix.platform.service.front.GroupAgentInfoService;
import com.phoenix.platform.service.front.GroupInfoService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 组织信息控制器
 */
@RestController
@RequestMapping("/platform/group-info")
@RequiredArgsConstructor
public class GroupInfoController {

    private final GroupInfoService groupInfoService;
    private final GroupAgentInfoService groupAgentInfoService;

    @GetMapping("/page")
    public ReturnVo<Page<GroupInfo>> page(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           GroupInfo query) {
        return ReturnVo.ok(groupInfoService.page(new Page<>(page, size), query));
    }

    @GetMapping("/{id}")
    public ReturnVo<GroupInfo> getById(@PathVariable String id) {
        return ReturnVo.ok(groupInfoService.getById(id));
    }

    @GetMapping("/sn/{sn}")
    public ReturnVo<GroupInfo> getBySn(@PathVariable String sn) {
        return ReturnVo.ok(groupInfoService.getBySn(sn));
    }

    @PostMapping
    public ReturnVo<Boolean> save(@RequestBody GroupInfo groupInfo) {
        return ReturnVo.ok(groupInfoService.save(groupInfo));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody GroupInfo groupInfo) {
        return ReturnVo.ok(groupInfoService.updateById(groupInfo));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(groupInfoService.deleteById(id));
    }

    @PutMapping("/{id}/toggle-status")
    public ReturnVo<Boolean> toggleStatus(@PathVariable String id) {
        return ReturnVo.ok(groupInfoService.toggleStatus(id));
    }

    @DeleteMapping("/{groupId}/agent/{agentId}")
    public ReturnVo<Boolean> removeAgent(@PathVariable String groupId, @PathVariable String agentId) {
        return ReturnVo.ok(groupAgentInfoService.deleteByGroupIdAndAgentId(groupId, agentId));
    }
}
