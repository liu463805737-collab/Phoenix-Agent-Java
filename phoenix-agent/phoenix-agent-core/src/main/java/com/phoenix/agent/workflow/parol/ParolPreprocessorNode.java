package com.phoenix.agent.workflow.parol;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

public class ParolPreprocessorNode implements NodeAction {
    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String input = state.value("input", "");
        input = input.trim();
        System.out.println("预处理节点：输入 -> " + input);
        return Map.of("cleaned_input", input);
    }
}
