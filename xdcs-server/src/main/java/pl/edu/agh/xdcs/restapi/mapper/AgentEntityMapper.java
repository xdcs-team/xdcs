package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.dao.AgentDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Krystian Życiński
 */
public class AgentEntityMapper {
    @Inject
    private AgentDao agentDao;

    public List<AgentEntity> toAgentEntities(List<String> agents) {
        return agents.stream().map(this::toAgentEntity).collect(Collectors.toList());
    }

    private AgentEntity toAgentEntity(String agent) {
        return agentDao.findByName(agent).orElseThrow(()
                -> new DatabaseInconsistencyException("Expected agent with name: " + agent));
    }

}
