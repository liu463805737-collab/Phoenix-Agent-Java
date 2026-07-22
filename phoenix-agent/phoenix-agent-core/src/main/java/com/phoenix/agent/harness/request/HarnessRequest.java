package com.phoenix.agent.harness.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
