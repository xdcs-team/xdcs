package pl.edu.agh.xdcs.grpc.events;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.ResourceType;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class AgentRegisteredEvent {
    private Agent agent;
    private String displayName;
    private List<AnnouncedResource> resources;

    @Getter
    @Builder
    public static class AnnouncedResource {
        private String key;
        private ResourceType type;
    }
}
