package pl.edu.agh.xdcs.restapi.mapper.impl;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.restapi.mapper.UnsatisfiedMappingException;
import pl.edu.agh.xdcs.restapi.model.NodeDto;

/**
 * @author Kamil Jarosz
 */
public class NodeStatusMapper {
    public NodeDto.StatusEnum toRest(Agent.Status status) {
        switch (status) {
            case ONLINE:
                return NodeDto.StatusEnum.ONLINE;
            case OFFLINE:
                return NodeDto.StatusEnum.OFFLINE;
            default:
                throw new UnsatisfiedMappingException();
        }
    }
}
