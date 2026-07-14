package com.phoenix.agent.harness.agent;

import com.alibaba.cloud.ai.graph.agent.Agent;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class HarnessStaticLoader {
    private Map<String, HarnessAgent> agents = new ConcurrentHashMap<>();

    public void addAgent(String sn, HarnessAgent agent) {
        agents.put(sn, agent);
    }


    protected Map<String, HarnessAgent> loadAgentMap() {
        return agents;
    }

    public HarnessAgent loadAgent(String sn) {
        if (sn == null || sn.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent name cannot be null or empty");
        }
        HarnessAgent agent = agents.get(sn);
        if (agent == null) {
            throw new NoSuchElementException("Agent not found: " + sn);
        } else {
            return agent;
        }
    }

}
