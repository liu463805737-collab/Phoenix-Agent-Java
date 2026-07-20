package com.phoenix.agent.harness.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class HarnessResponse implements Serializable {
    private String type;
    private String sessionId;
    private String content;
}
