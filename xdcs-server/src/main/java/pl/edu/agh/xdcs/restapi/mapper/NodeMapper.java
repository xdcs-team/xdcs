package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.db.dao.ResourceDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.restapi.model.NodeDto;
import pl.edu.agh.xdcs.restapi.model.NodesDto;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class NodeMapper {
    @Inject
    private NodeStatusMapper statusMapper;

    @Inject
    private ResourceMapper resourceMapper;

    @Inject
    private ResourceDao resourceDao;

    public NodeDto toNode(AgentEntity agent) {
        NodeDto dto = new NodeDto();
        dto.setId(agent.getName());
        dto.setName(agent.getDisplayName());
        dto.address(agent.getAddress().getHostAddress());
        dto.status(statusMapper.toRest(agent.getStatus()));
        dto.setResources(resourceMapper.toResources(resourceDao.getByAgent(agent)));
        return dto;
    }

    public NodesDto toNodes(Collection<AgentEntity> agents) {
        NodesDto dto = new NodesDto();
        dto.setItems(agents.stream()
                .map(this::toNode)
                .collect(Collectors.toList()));
        return dto;
    }
}
