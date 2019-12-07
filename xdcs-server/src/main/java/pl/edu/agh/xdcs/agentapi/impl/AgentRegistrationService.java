package pl.edu.agh.xdcs.agentapi.impl;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.api.AgentRegistrationGrpc;
import pl.edu.agh.xdcs.api.AgentRegistrationRequest;
import pl.edu.agh.xdcs.api.AgentRegistrationResponse;
import pl.edu.agh.xdcs.grpc.Service;
import pl.edu.agh.xdcs.grpc.events.AgentRegisteredEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
@Service
public class AgentRegistrationService extends AgentRegistrationGrpc.AgentRegistrationImplBase {
    @Inject
    private Logger logger;

    @Inject
    private Agent currentAgent;

    @Inject
    private Event<AgentRegisteredEvent> agentRegisteredEvent;

    @Override
    public void register(AgentRegistrationRequest request, StreamObserver<AgentRegistrationResponse> responseObserver) {
        logger.info("Agent registering: " + request);

        AgentRegisteredEvent event = AgentRegisteredEvent.builder()
                .agent(currentAgent)
                .displayName(request.getDisplayName())
                .build();
        agentRegisteredEvent.fire(event);
        agentRegisteredEvent.fireAsync(event);

        responseObserver.onNext(AgentRegistrationResponse.newBuilder()
                .setSuccess(true)
                .build());
        responseObserver.onCompleted();
    }
}
