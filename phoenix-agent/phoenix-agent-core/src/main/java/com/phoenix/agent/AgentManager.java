package com.phoenix.agent;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.phoenix.common.enm.AgentTypeEnm;
import com.phoenix.agent.react.ReactAgentComponent;
import com.phoenix.agent.vo.AgentInfoDto;
import com.phoenix.agent.workflow.WorkflowComponent;
import com.phoenix.common.exception.BusinessException;
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
                } else {
                    outputFlux = workflowComponent.streamCall(agentInfoDto.getSn(), agentInfoDto.getMessage(), agentInfoDto.getUserProfile());
                }
            }
        }
        return outputFlux;
    }
}
