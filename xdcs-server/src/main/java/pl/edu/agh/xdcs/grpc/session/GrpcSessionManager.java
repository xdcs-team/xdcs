package pl.edu.agh.xdcs.grpc.session;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.grpc.context.SessionContext;
import pl.edu.agh.xdcs.grpc.ee.StubProducer;
import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentDisconnectedEvent;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionClosedEvent;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionCreatedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class GrpcSessionManager {
    private final Map<String, GrpcSession> sessionsByNames = new ConcurrentHashMap<>();

    @Inject
    private Event<GrpcSessionCreatedEvent> sessionCreatedEvent;

    @Inject
    private Event<GrpcSessionClosedEvent> sessionClosedEvent;

    public Optional<GrpcSession> getSession(String agentName) {
        return Optional.ofNullable(sessionsByNames.get(agentName));
    }

    public StubProducer getStubProducer(Agent agent) {
        return new StubProducer(getSession(agent.getName())
                .orElseThrow(() -> new RuntimeException("Agent " + agent.getName() + " doesn't have an active session")));
    }

    private void createSession(@Observes AgentConnectedEvent event, GrpcSessionFactory sessionFactory) {
        GrpcSession session = sessionFactory.newManagedSession(event);
        sessionsByNames.put(session.getAgentName(), session);

        GrpcSessionCreatedEvent sessionEvent = GrpcSessionCreatedEvent.builder()
                .session(session)
                .build();
        sessionCreatedEvent.fire(sessionEvent);
        sessionCreatedEvent.fireAsync(sessionEvent);
    }

    private void sessionClosed(@Observes AgentDisconnectedEvent event, SessionContext sessionContext) {
        getSession(event.getAgentName()).ifPresent(session -> {
            sessionContext.evict(session);
            sessionsByNames.remove(session.getAgentName());
            session.close();

            GrpcSessionClosedEvent sessionEvent = GrpcSessionClosedEvent.builder()
                    .session(session)
                    .build();
            sessionClosedEvent.fire(sessionEvent);
            sessionClosedEvent.fireAsync(sessionEvent);
        });
    }
}
