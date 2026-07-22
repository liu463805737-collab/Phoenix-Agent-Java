package com.phoenix.agent.harness.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class HarnessRequest implements Serializable {
    @NotBlank
    private String userId;
    @NotBlank
    private String sessionId;
    @NotBlank
    private String message;
    @NotBlank
    private String harnessSn;
}
