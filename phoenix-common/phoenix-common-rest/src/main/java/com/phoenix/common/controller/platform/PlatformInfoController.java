package com.phoenix.common.controller.platform;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/platform/platform-info")
@RequiredArgsConstructor
public class PlatformInfoController {

    private final PlatformInfoService platformInfoService;

    @GetMapping("/page")
    public ReturnVo<Page<PlatformInfo>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              PlatformInfo query) {
        return ReturnVo.ok(platformInfoService.page(new Page<>(page, size), query));
    }

    @GetMapping("/{id}")
    public ReturnVo<PlatformInfo> getById(@PathVariable String id) {
        return ReturnVo.ok(platformInfoService.getById(id));
    }

    @GetMapping("/type/{type}")
    public ReturnVo<PlatformInfo> getByType(@PathVariable String type) {
        return ReturnVo.ok(platformInfoService.getByType(type));
    }

    @GetMapping("/type/{type}/enabled")
    public ReturnVo<PlatformInfo> getEnabledByType(@PathVariable String type) {
        return ReturnVo.ok(platformInfoService.getEnabledByType(type));
    }

    @PostMapping
    public ReturnVo<Boolean> save(@RequestBody PlatformInfo platformInfo) {
        return ReturnVo.ok(platformInfoService.save(platformInfo));
    }

    @PutMapping
    public ReturnVo<Boolean> update(@RequestBody PlatformInfo platformInfo) {
        return ReturnVo.ok(platformInfoService.updateById(platformInfo));
    }

    @DeleteMapping("/{id}")
    public ReturnVo<Boolean> delete(@PathVariable String id) {
        return ReturnVo.ok(platformInfoService.removeById(id));
    }

    /**
     * 获取启动的三方应用
     * @return
     */
    @GetMapping("/getEnabledPlatform")
    public ReturnVo<PlatformInfo> getEnabled(){
        return ReturnVo.ok(platformInfoService.getEnabled());
    }

}
