package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.restapi.mapper.impl.ResourceTypeMapper;
import pl.edu.agh.xdcs.restapi.model.ResourceDto;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class ResourcePatternMapper {
    @Inject
    private ResourceTypeMapper resourceTypeMapper;

    public ResourceDto toRestEntity(ResourcePatternEntity pattern) {
        return new ResourceDto()
                .agent(pattern.getAgentNamePattern().toString())
                .key(pattern.getResourceKeyPattern().toString())
                .type(resourceTypeMapper.toRestEntity(pattern.getType()))
                .quantity(pattern.getQuantity());
    }
}
