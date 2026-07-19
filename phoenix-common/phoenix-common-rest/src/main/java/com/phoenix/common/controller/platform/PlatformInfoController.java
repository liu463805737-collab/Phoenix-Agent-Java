package com.phoenix.common.controller.platform;

import com.mybatisflex.core.paginate.Page;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.platform.DingTalkSdkService;
import com.phoenix.common.service.platform.PlatformInfoService;
import com.phoenix.tools.vo.ReturnVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/platform/platform-info")
@RequiredArgsConstructor
public class PlatformInfoController {

    private final PlatformInfoService platformInfoService;

    private final DingTalkSdkService dingTalkSdkService;

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

    /**
     * 获取钉钉 JSAPI 鉴权配置（用于前端 dd.config）
     * @param url 当前页面的完整 URL（需 encodeURIComponent）
     * @return agentId, corpId, timeStamp, nonceStr, signature
     */
    @GetMapping("/getDingTalkJsApiConfig")
    public ReturnVo<Map<String, String>> getDingTalkJsApiConfig(@RequestParam String url) {
        try {
            Map<String, String> config = dingTalkSdkService.getJsApiConfig(url);
            return ReturnVo.ok(config);
        } catch (Exception e) {
            String cause = e.getMessage();
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
                if (root instanceof java.net.UnknownHostException) {
                    cause = "无法解析钉钉API域名，请检查服务器网络/DNS/代理配置: " + root.getMessage();
                    break;
                }
            }
            log.error("获取钉钉 JSAPI 配置失败", e);
            return ReturnVo.error(cause);
        }
    }

}
