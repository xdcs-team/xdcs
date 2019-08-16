package pl.edu.agh.xdcs.agents;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class AgentManager {
    private final ConcurrentMap<String, Agent> agents = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        agents.put("hello", Agent.builder()
                .name("hello")
                .label("Hello")
                .address(InetAddress.getLoopbackAddress())
                .status(Agent.Status.ONLINE)
                .build());
        agents.put("goodbye", Agent.builder()
                .name("goodbye")
                .label("Goodbye")
                .address(InetAddress.getLoopbackAddress())
                .status(Agent.Status.OFFLINE)
                .build());
    }

    public Optional<Agent> getAgent(String name) {
        return Optional.ofNullable(agents.getOrDefault(name, null));
    }

    public Collection<Agent> getAllAgents() {
        return agents.values();
    }
}
