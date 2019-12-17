package pl.edu.agh.xdcs.restapi.mapper;

import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.restapi.model.ResourceDto;
import pl.edu.agh.xdcs.util.WildcardPattern;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class ResourcePatternMapper {
    public ResourceDto toRestEntity(ResourcePatternEntity pattern) {
        return new ResourceDto()
                .agent(WildcardPattern.parseLike(pattern.getAgentNameLike()).toString())
                .key(WildcardPattern.parseLike(pattern.getResourceKeyLike()).toString());
    }
}
