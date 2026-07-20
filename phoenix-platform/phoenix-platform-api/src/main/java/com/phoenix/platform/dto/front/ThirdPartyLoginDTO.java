package com.phoenix.platform.dto.front;

import lombok.Data;

@Data
public class ThirdPartyLoginDTO {
    private String platform;
    private String code;
}
