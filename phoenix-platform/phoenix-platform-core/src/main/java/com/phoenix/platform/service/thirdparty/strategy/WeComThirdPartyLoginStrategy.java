package com.phoenix.platform.service.thirdparty.strategy;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.phoenix.common.enm.PlatformTypeEnm;
import com.phoenix.common.model.platform.PlatformInfo;
import com.phoenix.common.service.sync.weixin.WeixinSyncConstants;
import com.phoenix.platform.service.thirdparty.ThirdPartyLoginStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeComThirdPartyLoginStrategy implements ThirdPartyLoginStrategy {

    @Override
    public String getPlatformType() {
        return PlatformTypeEnm.WEIXIN.getCode();
    }

    @Override
    public String resolveUserId(String code, PlatformInfo platform) {
        String tokenUrl = WeixinSyncConstants.TOKEN_URL + "?corpid="
                + platform.getCorpid() + "&corpsecret=" + platform.getCorpsecret();
        String tokenResp = HttpRequest.get(tokenUrl).execute().body();
        JSONObject tokenJson = JSONUtil.parseObj(tokenResp);
        Integer errcode = tokenJson.getInt("errcode");
        if (errcode == null || errcode != 0) {
            log.error("获取企业微信 access_token 失败, errcode: {}, errmsg: {}", errcode, tokenJson.getStr("errmsg"));
            return null;
        }
        String accessToken = tokenJson.getStr("access_token");

        String url = WeixinSyncConstants.USER_INFO_URL + "?access_token=" + accessToken + "&code=" + code;
        String respBody = HttpRequest.get(url).execute().body();
        JSONObject resp = JSONUtil.parseObj(respBody);
        Integer errcode2 = resp.getInt("errcode");
        if (errcode2 == null || errcode2 != 0) {
            log.error("企业微信授权码验证失败, errcode: {}, errmsg: {}", errcode2, resp.getStr("errmsg"));
            return null;
        }
        return resp.getStr("UserId");
    }
}
