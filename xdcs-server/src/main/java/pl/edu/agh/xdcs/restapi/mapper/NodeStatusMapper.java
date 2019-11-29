package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.mapper.UnsatisfiedMappingException;
import pl.edu.agh.xdcs.restapi.model.NodeDto;

/**
 * @author Kamil Jarosz
 */
public class NodeStatusMapper {
    public NodeDto.StatusEnum toRest(Agent.Status status) {
        switch (status) {
            case READY:
                return NodeDto.StatusEnum.READY;
            case OFFLINE:
                return NodeDto.StatusEnum.OFFLINE;
            case UNAVAILABLE:
                return NodeDto.StatusEnum.UNAVAILABLE;
            case BUSY:
                return NodeDto.StatusEnum.BUSY;
            default:
                throw new UnsatisfiedMappingException();
        }
    }
}
