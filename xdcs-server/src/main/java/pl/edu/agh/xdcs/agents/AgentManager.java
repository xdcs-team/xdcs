package pl.edu.agh.xdcs.agents;

import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentDisconnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentRegisteredEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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

    public Optional<Agent> getAgent(String name) {
        return Optional.ofNullable(agents.getOrDefault(name, null));
    }

    public Collection<Agent> getAllAgents() {
        return agents.values();
    }

    private Agent getOrCreateAgent(String name) {
        return agents.computeIfAbsent(name, Agent::new);
    }

    private void connectAgent(@Observes AgentConnectedEvent event) {
        Agent agent = getOrCreateAgent(event.getAgentName());
        agent.setAddress(event.getAgentAddress());
        agent.setTunnelEndpoint(event.getTunnelEndpoint());
        agent.setStatus(Agent.Status.UNAVAILABLE);
    }

    private void registerAgent(@Observes AgentRegisteredEvent event) {
        Agent agent = event.getAgent();
        agent.setDisplayName(event.getDisplayName());
        agent.setStatus(Agent.Status.READY);
    }

    private void disconnectAgent(@Observes AgentDisconnectedEvent event) {
        Agent agent = getOrCreateAgent(event.getAgentName());
        agent.setAddress(event.getAgentAddress());
        agent.setStatus(Agent.Status.OFFLINE);
    }
}
