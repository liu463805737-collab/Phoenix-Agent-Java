package com.phoenix.agent.harness.state;

import io.agentscope.extensions.postgresql.state.PostgresAgentStateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresAgentStateStoreService {

    private final PostgresAgentStateStore  postgresAgentStateStore;


}
