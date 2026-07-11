package com.phoenix.privilege.service;

import com.phoenix.privilege.enums.CaptchaVerifyResult;
import com.phoenix.privilege.vo.CaptchaVO;

public interface CaptchaService {

	CaptchaVO generate();

	CaptchaVerifyResult verify(String captchaKey, String captchaCode);

}
