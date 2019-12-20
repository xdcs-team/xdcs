package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.agentapi.mapper.AgentResourceTypeMapper;
import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.dao.AgentDao;
import pl.edu.agh.xdcs.db.dao.ResourceDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.ResourceEntity;
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

    @Inject
    private ResourceDao resourceDao;

    @Inject
    private AgentResourceTypeMapper resourceTypeMapper;

    private void createAgent(@Observes AgentConnectedEvent event) {
        String name = event.getAgentName();
        AgentEntity agent = agentDao.findByName(name)
                .orElseGet(AgentEntity::new);

        agent.setName(name);
        agent.setStatus(AgentEntity.Status.UNAVAILABLE);
        agent.setAddress(event.getAgentAddress());
        agentDao.persist(agent);
    }

    private void registerAgent(@Observes AgentRegisteredEvent event) {
        AgentEntity agentEntity = agentDao.findByName(event.getAgent().getName())
                .orElseThrow(() -> new DatabaseInconsistencyException("Expected agent with name: " +
                        event.getAgent().getName()));

        agentEntity.setDisplayName(event.getDisplayName());
        agentEntity.setStatus(AgentEntity.Status.READY);
        resourceDao.removeResources(agentEntity);

        event.getResources().forEach(resource -> {
            ResourceEntity resourceEntity = new ResourceEntity();
            resourceEntity.setOwner(agentEntity);
            resourceEntity.setLockedBy(null);
            resourceEntity.setResourceKey(resource.getKey());
            resourceEntity.setType(resourceTypeMapper.toModelEntity(resource.getType()));
            resourceDao.persist(resourceEntity);
        });
    }

    private void disconnectAgent(@Observes AgentDisconnectedEvent event) {
        agentDao.findByName(event.getAgentName()).ifPresent(agentEntity -> {
            agentEntity.setStatus(AgentEntity.Status.OFFLINE);
            resourceDao.removeResources(agentEntity);
        });
    }
}
