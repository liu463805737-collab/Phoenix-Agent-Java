package com.phoenix.agent.service;

public interface MemoryPipelineService {
    void processAndExtractMemory(String userId, String agentSn, String userQuery);
}
