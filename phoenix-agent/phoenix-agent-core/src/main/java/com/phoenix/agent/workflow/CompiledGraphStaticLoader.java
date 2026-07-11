package com.phoenix.agent.workflow;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CompiledGraphStaticLoader {
    public Map<String, CompiledGraph> compiledGraphs = new ConcurrentHashMap<>();
    /**
     * 添加状态图
     * @param name 状态图名称
     * @param compiledGraph 状态图
     */
    public void addCompiledGraph(String name, CompiledGraph compiledGraph) {
        compiledGraphs.put(name, compiledGraph);
    }

    /**
     * 获取所有状态图
     *
     * @return
     */
    public List<CompiledGraph> getCompiledGraphs() {
        return new ArrayList<>(compiledGraphs.values());
    }

    /**
     * 获取状态图
     *
     * @param name 状态图名称
     * @return
     */
    public CompiledGraph getCompiledGraph(String name) {
        return compiledGraphs.get(name);
    }

    /**
     * 移除状态图
     *
     * @param name 状态图名称
     */
    public void removeCompiledGraph(String name) {
        compiledGraphs.remove(name);
    }

}
