package pl.edu.agh.xdcs.agentapi.mapper;

import pl.edu.agh.xdcs.api.ResourceType;
import pl.edu.agh.xdcs.mapper.EnumMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kamil Jarosz
 */
public class AgentResourceTypeMapper extends
        EnumMapper<pl.edu.agh.xdcs.db.entity.ResourceType, ResourceType> {
    private static final Map<pl.edu.agh.xdcs.db.entity.ResourceType, ResourceType> mapping = new HashMap<>();

    static {
        mapping.put(pl.edu.agh.xdcs.db.entity.ResourceType.CUDA, ResourceType.CUDA);
        mapping.put(pl.edu.agh.xdcs.db.entity.ResourceType.OPENCL, ResourceType.OPENCL);
        mapping.put(pl.edu.agh.xdcs.db.entity.ResourceType.CPU, ResourceType.CPU);
    }

    protected AgentResourceTypeMapper() {
        super(mapping);
    }
}
