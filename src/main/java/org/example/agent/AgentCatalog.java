package org.example.agent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AgentCatalog {

    private final List<AgentDefinition> agents;

    @JsonCreator
    public AgentCatalog(@JsonProperty("agents") List<AgentDefinition> agents) {
        this.agents = agents;
    }

    public List<AgentDefinition> getAgents() {
        return agents;
    }
}