package com.phoenix.agent.react;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AgentStaticLoader {
    private Map<String, Agent> agents = new ConcurrentHashMap<>();

    public void addAgent(String sn, Agent agent) {
        agents.put(sn, agent);
    }

    /**
     * 生成UML图片
     *
     * @param sn 智能体的标识
     * @return
     */
    public String generatePlantUmlFromGraph(String sn) {
        ReactAgent reactAgent = this.loadAgent(sn);
        GraphRepresentation representation = reactAgent.getAndCompileGraph().stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, sn);
        return representation.content();
    }

    protected Map<String, Agent> loadAgentMap() {
        return agents;
    }

    public ReactAgent loadAgent(String sn) {
        if (sn == null || sn.trim().isEmpty()) {
            throw new IllegalArgumentException("Agent name cannot be null or empty");
        }
        Agent agent = agents.get(sn);
        if (agent == null) {
            throw new NoSuchElementException("Agent not found: " + sn);
        } else {
            return (ReactAgent) agent;
        }
    }

}
