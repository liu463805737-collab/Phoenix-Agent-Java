package com.phoenix.platform.controller.front;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.platform.model.front.AccountInfo;
import com.phoenix.platform.model.front.GroupAgentInfo;
import com.phoenix.platform.service.front.GroupAgentInfoService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platform/group-agent-info")
@RequiredArgsConstructor
public class GroupAgentInfoController {

    private final GroupAgentInfoService groupAgentInfoService;

    @GetMapping("/page")
    public ReturnVo<Page<GroupAgentInfo>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               GroupAgentInfo query) {
        return ReturnVo.ok(groupAgentInfoService.page(new Page<>(page, size), query));
    }

    @GetMapping("/{id}")
    public ReturnVo<GroupAgentInfo> getById(@PathVariable String id) {
        return ReturnVo.ok(groupAgentInfoService.getById(id));
    }

    @GetMapping("/group/{groupId}")
    public ReturnVo<List<GroupAgentInfo>> getByGroupId(@PathVariable String groupId) {
        return ReturnVo.ok(groupAgentInfoService.getByGroupId(groupId));
    }

    @GetMapping("/agent/{agentId}")
    public ReturnVo<List<GroupAgentInfo>> getByAgentId(@PathVariable String agentId) {
        return ReturnVo.ok(groupAgentInfoService.getByAgentId(agentId));
    }

    @PostMapping
    public ReturnVo<Boolean> save(@RequestBody GroupAgentInfo groupAgentInfo) {
        return ReturnVo.ok(groupAgentInfoService.save(groupAgentInfo));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody GroupAgentInfo groupAgentInfo) {
        return ReturnVo.ok(groupAgentInfoService.updateById(groupAgentInfo));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(groupAgentInfoService.removeById(id));
    }

    @DeleteMapping("/group/{groupId}/agent/{agentId}")
    public ReturnVo<Boolean> deleteByGroupIdAndAgentId(@PathVariable String groupId, @PathVariable String agentId) {
        return ReturnVo.ok(groupAgentInfoService.deleteByGroupIdAndAgentId(groupId, agentId));
    }

    /**
     * 查询智能体列表
     * @return
     */
    @GetMapping("/list")
    public ReturnVo<List<GroupAgentInfo>> list(){
        return ReturnVo.ok(groupAgentInfoService.list());
    }
}
