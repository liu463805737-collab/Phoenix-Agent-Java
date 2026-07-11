package com.phoenix.agent.hook;

import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@HookPositions({HookPosition.BEFORE_MODEL})
public class CombinedDbHook extends AbstractCombinedDbHook {

    @Override
    public List<String> namespace(String agentId ,String userId) {
        return List.of(agentId, userId);
    }
}
