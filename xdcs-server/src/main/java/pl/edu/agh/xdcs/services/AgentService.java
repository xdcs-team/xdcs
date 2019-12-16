package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.dao.AgentDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentDisconnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentRegisteredEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Krystian Życiński
 */
@Transactional
public class AgentService {
    @Inject
    private AgentDao agentDao;

    private void createAgent(@Observes AgentConnectedEvent event) {
        persistIfNeeded(event.getAgentName());
    }

    private void registerAgent(@Observes AgentRegisteredEvent event) {
        AgentEntity agentEntity = agentDao.findByName(event.getAgent().getName())
                .orElseThrow(() -> new DatabaseInconsistencyException("Expected agent with name: " +
                        event.getAgent().getName()));

        registerAgent(agentEntity, event.getDisplayName());
    }

    private void registerAgent(AgentEntity agentEntity, String displayName) {
        agentEntity.setDisplayName(displayName);
        agentEntity.setStatus(AgentEntity.Status.READY);
    }

    private void disconnectAgent(@Observes AgentDisconnectedEvent event) {
        AgentEntity agentEntity = agentDao.findByName(event.getAgentName())
                .orElseThrow(() -> new DatabaseInconsistencyException("Expected agent with name: " +
                        event.getAgentName()));

        agentEntity.setStatus(AgentEntity.Status.OFFLINE);
    }

    private void persistIfNeeded(String name) {
        if (!agentDao.findByName(name).isPresent()) {
            persistAgentEntity(name);
        }
    }

    private void persistAgentEntity(String name) {
        AgentEntity agentEntity = new AgentEntity();
        agentEntity.setName(name);
        agentEntity.setStatus(AgentEntity.Status.UNAVAILABLE);
        agentDao.persist(agentEntity);
    }
}
