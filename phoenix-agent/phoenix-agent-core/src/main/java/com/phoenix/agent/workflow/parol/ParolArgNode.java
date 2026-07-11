package com.phoenix.agent.workflow.parol;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParolArgNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {


        return Map.of();
    }
}
