package pl.edu.agh.xdcs.restapi.mapper;

import com.google.common.collect.EnumBiMap;
import pl.edu.agh.xdcs.db.entity.ResourceType;
import pl.edu.agh.xdcs.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.ResourceDto;

/**
 * @author Kamil Jarosz
 */
public class ResourceTypeMapper extends EnumMapper<ResourceType, ResourceDto.TypeEnum> {
    protected ResourceTypeMapper() {
        super(createMapping());
    }

    private static EnumBiMap<ResourceType, ResourceDto.TypeEnum> createMapping() {
        EnumBiMap<ResourceType, ResourceDto.TypeEnum> mapping =
                EnumBiMap.create(ResourceType.class, ResourceDto.TypeEnum.class);
        mapping.put(ResourceType.CPU, ResourceDto.TypeEnum.CPU);
        mapping.put(ResourceType.CUDA, ResourceDto.TypeEnum.CUDA);
        mapping.put(ResourceType.OPENCL, ResourceDto.TypeEnum.OPENCL);
        return mapping;
    }
}
