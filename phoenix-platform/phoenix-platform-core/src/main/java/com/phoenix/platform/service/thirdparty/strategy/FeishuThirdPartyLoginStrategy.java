package com.phoenix.platform.service.thirdparty.strategy;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.sync.feishu.FeishuSyncConstants;
import com.phoenix.platform.service.thirdparty.ThirdPartyLoginStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeishuThirdPartyLoginStrategy implements ThirdPartyLoginStrategy {

    @Override
    public String getPlatformType() {
        return PlatformTypeEnm.FEISHU.getCode();
    }

    @Override
    public String resolveUserId(String code, PlatformInfo platform) {
        String url = FeishuSyncConstants.OAUTH_ACCESS_TOKEN_URL;
        String body = JSONUtil.createObj()
                .set("grant_type", "authorization_code")
                .set("code", code)
                .set("app_id", platform.getCorpid())
                .set("app_secret", platform.getCorpsecret())
                .toString();
        String respBody = HttpRequest.post(url).body(body).execute().body();
        JSONObject resp = JSONUtil.parseObj(respBody);
        Integer code2 = resp.getInt("code");
        if (code2 == null || code2 != 0) {
            log.error("飞书授权码验证失败, code: {}, msg: {}", code2, resp.getStr("msg"));
            return null;
        }
        JSONObject data = resp.getJSONObject("data");
        if (data == null) {
            return null;
        }
        return data.getStr("user_id");
    }
}
