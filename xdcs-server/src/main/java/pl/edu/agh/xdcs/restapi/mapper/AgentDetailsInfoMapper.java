package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.grpc.AgentDetailsProvider;
import pl.edu.agh.xdcs.restapi.model.AdditionalPropertyDto;

import javax.inject.Inject;

/**
 * @author Krystian Życiński
 */
public abstract class AgentDetailsInfoMapper<T> {
    @Inject
    protected AgentDetailsProvider agentDetailsProvider;

    public abstract T toRestEntity(Agent agent);

    protected AdditionalPropertyDto createAdditionalProperty(String name, String value) {
        return new AdditionalPropertyDto().name(name).value(value);
    }
}
