package com.phoenix.platform.controller.thirdparty;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.platform.model.thirdparty.DingtalkEntity;
import com.phoenix.platform.service.thirdparty.DingtalkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 钉钉用户信息控制器
 * <p>
 * 提供钉钉用户信息的 CRUD 及分页查询接口。
 */
@RestController
@RequestMapping("/dingtalk")
@RequiredArgsConstructor
public class DingtalkController {

    private final DingtalkService dingtalkService;

    /**
     * 分页查询钉钉用户信息
     *
     * @param page  当前页码，默认 1
     * @param size  每页记录数，默认 10
     * @param query 查询条件（userCode/userName 模糊匹配）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Page<DingtalkEntity> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      DingtalkEntity query) {
        return dingtalkService.page(new Page<>(page, size), query);
    }

    /**
     * 根据 userCode 查询钉钉用户信息
     *
     * @param userCode 用户编码
     * @return 钉钉用户信息
     */
    @GetMapping("/userCode/{userCode}")
    public DingtalkEntity getByUserCode(@PathVariable String userCode) {
        return dingtalkService.getByUserCode(userCode);
    }

    /**
     * 根据用户 ID 列表批量查询钉钉用户信息
     *
     * @param userIds 用户 ID 列表
     * @return 钉钉用户信息列表
     */
    @GetMapping("/userIds")
    public List<DingtalkEntity> getByUserIds(@RequestParam List<String> userIds) {
        return dingtalkService.getByUserIds(userIds);
    }

    /**
     * 根据 ID 查询钉钉用户信息
     *
     * @param id 主键 ID
     * @return 钉钉用户信息
     */
    @GetMapping("/{id}")
    public DingtalkEntity getById(@PathVariable Long id) {
        return dingtalkService.getById(id);
    }

    /**
     * 新增钉钉用户信息
     *
     * @param dingtalkEntity 钉钉用户信息
     * @return 是否成功
     */
    @PostMapping
    public boolean save(@RequestBody DingtalkEntity dingtalkEntity) {
        return dingtalkService.save(dingtalkEntity);
    }

    /**
     * 更新钉钉用户信息
     *
     * @param dingtalkEntity 钉钉用户信息（需包含主键 ID）
     * @return 是否成功
     */
    @PutMapping
    public boolean update(@RequestBody DingtalkEntity dingtalkEntity) {
        return dingtalkService.updateById(dingtalkEntity);
    }

    /**
     * 删除钉钉用户信息（逻辑删除）
     *
     * @param id 主键 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return dingtalkService.removeById(id);
    }
}
