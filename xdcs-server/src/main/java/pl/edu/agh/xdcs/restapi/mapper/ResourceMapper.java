package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.db.entity.ResourceEntity;
import pl.edu.agh.xdcs.restapi.model.ResourceDto;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class ResourceMapper {
    @Inject
    private ResourceTypeMapper resourceTypeMapper;

    public ResourceDto toResource(ResourceEntity resource) {
        return new ResourceDto()
                .agent(resource.getOwner().getName())
                .key(resource.getResourceKey())
                .type(resourceTypeMapper.toApiEntity(resource.getType()));
    }

    public List<ResourceDto> toResources(List<ResourceEntity> resources) {
        return resources.stream()
                .map(this::toResource)
                .collect(Collectors.toList());
    }
}
