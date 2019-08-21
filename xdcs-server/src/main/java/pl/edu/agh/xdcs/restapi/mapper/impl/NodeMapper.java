package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.agents.Agent;
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

    public NodeDto toNode(Agent agent) {
        NodeDto dto = new NodeDto();
        dto.setId(agent.getName());
        dto.setName(agent.getDisplayName());
        dto.address(agent.getAddress().getHostAddress());
        dto.status(statusMapper.toRest(agent.getStatus()));
        return dto;
    }

    public NodesDto toNodes(Collection<Agent> agents) {
        NodesDto dto = new NodesDto();
        dto.setItems(agents.stream()
                .map(this::toNode)
                .collect(Collectors.toList()));
        return dto;
    }
}
