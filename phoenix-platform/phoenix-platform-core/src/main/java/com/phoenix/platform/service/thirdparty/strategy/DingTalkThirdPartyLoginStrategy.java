package com.phoenix.platform.service.thirdparty.strategy;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.sync.dingtalk.DingTalkSyncConstants;
import com.phoenix.platform.service.thirdparty.ThirdPartyLoginStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DingTalkThirdPartyLoginStrategy implements ThirdPartyLoginStrategy {

    @Override
    public String getPlatformType() {
        return PlatformTypeEnm.DINGTALK.getCode();
    }

    @Override
    public String resolveUserId(String code, PlatformInfo platform) {
        String tokenUrl = DingTalkSyncConstants.TOKEN_URL + "?appkey=" + platform.getCorpid()
                + "&appsecret=" + platform.getCorpsecret();
        String tokenResp = HttpRequest.get(tokenUrl).execute().body();
        JSONObject tokenJson = JSONUtil.parseObj(tokenResp);
        Integer errcode = tokenJson.getInt("errcode");
        if (errcode == null || errcode != 0) {
            log.error("获取钉钉 access_token 失败, errcode: {}, errmsg: {}", errcode, tokenJson.getStr("errmsg"));
            return null;
        }
        String accessToken = tokenJson.getStr("access_token");

        String url = DingTalkSyncConstants.USER_INFO_URL + "?access_token=" + accessToken;
        String body = JSONUtil.createObj().set("code", code).toString();
        String respBody = HttpRequest.post(url).body(body).execute().body();
        JSONObject resp = JSONUtil.parseObj(respBody);
        Integer errcode2 = resp.getInt("errcode");
        if (errcode2 == null || errcode2 != 0) {
            log.error("钉钉授权码验证失败, errcode: {}, errmsg: {}", errcode2, resp.getStr("errmsg"));
            return null;
        }
        return resp.getJSONObject("result").getStr("userid");
    }
}
