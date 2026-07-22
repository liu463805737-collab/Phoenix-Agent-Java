package com.phoenix.agent;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.phoenix.common.enm.AgentTypeEnm;
import com.phoenix.agent.harness.request.HarnessRequest;
import com.phoenix.agent.harness.send.HarnessChatService;
import com.phoenix.agent.react.ReactAgentComponent;
import com.phoenix.agent.vo.AgentInfoDto;
import com.phoenix.agent.workflow.WorkflowComponent;
import com.phoenix.data.entity.Agent;
import com.phoenix.data.service.agent.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static com.phoenix.common.exception.ExceptionAgentEnum.AGENT_STATUS_DISABLED;


/**
 * 智能体管理
 */
@Component
@RequiredArgsConstructor
public class AgentManager {
    private final ReactAgentComponent reactAgentComponent;
    private final WorkflowComponent workflowComponent;
    private final HarnessChatService harnessChatService;
    private final AgentService agentService;


    /**
     * 智能体调用
     *
     * @param agentInfoDto agentInfoDto
     * @return
     */
    public Flux<NodeOutput> streamCall(AgentInfoDto agentInfoDto) throws Exception {
        Flux<NodeOutput> outputFlux = Flux.empty();
        Agent agent = agentService.findBySn(agentInfoDto.getSn());
        if (agent != null) {
            String type = agent.getType();
            if (StrUtil.isNotBlank(type)) {
                if (type.equals(AgentTypeEnm.AGENT.getCode())) {
                    outputFlux = reactAgentComponent.stream(agentInfoDto.getSn(), agentInfoDto.getMessage(), agentInfoDto.getUserProfile());
                } else if (type.equals(AgentTypeEnm.WORKFLOW.getCode())) {
                    outputFlux = workflowComponent.streamCall(agentInfoDto.getSn(), agentInfoDto.getMessage(), agentInfoDto.getUserProfile());
                }else if (type.equals(AgentTypeEnm.HARNESS.getCode())) {
                    HarnessRequest harnessRequest = HarnessRequest.builder()
                            .message(agentInfoDto.getMessage())
                            .userId(agentInfoDto.getUserProfile().getUserId())
                            .sessionId(agentInfoDto.getUserProfile().getSessionId())
                            .harnessSn(agentInfoDto.getSn())
                            .build();
                    outputFlux = harnessChatService.stream(agentInfoDto.getSn(), harnessRequest);
                }
            }
        }
        return outputFlux;
    }
}
