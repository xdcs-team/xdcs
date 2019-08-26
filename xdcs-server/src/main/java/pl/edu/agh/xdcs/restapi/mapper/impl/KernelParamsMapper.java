package pl.edu.agh.xdcs.restapi.mapper.impl;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.entity.KernelParameter;
import pl.edu.agh.xdcs.db.entity.KernelParameter.Direction;
import pl.edu.agh.xdcs.db.entity.KernelParameter.Type;
import pl.edu.agh.xdcs.db.entity.KernelParameters;
import pl.edu.agh.xdcs.restapi.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.KernelParamDto;
import pl.edu.agh.xdcs.restapi.model.KernelParamDto.DirectionEnum;
import pl.edu.agh.xdcs.restapi.model.KernelParamDto.TypeEnum;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class KernelParamsMapper {
    private EnumMapper<Type, TypeEnum> kernelTypeMapper = EnumMapper.forMapping(
            ImmutableMap.<Type, TypeEnum>builder()
                    .put(Type.SIMPLE, TypeEnum.SIMPLE)
                    .put(Type.POINTER, TypeEnum.POINTER)
                    .build());

    private EnumMapper<Direction, DirectionEnum> kernelDirectionMapper = EnumMapper.forMapping(
            ImmutableMap.<Direction, DirectionEnum>builder()
                    .put(Direction.IN, DirectionEnum.IN)
                    .put(Direction.OUT, DirectionEnum.OUT)
                    .put(Direction.IN_OUT, DirectionEnum.INOUT)
                    .build());

    public List<KernelParamDto> toRestEntity(KernelParameters model) {
        if (model == null) return null;
        List<KernelParameter> parameters = model.getParameters();
        if (parameters == null) return null;

        return parameters.stream()
                .map(this::toRestEntity)
                .collect(Collectors.toList());
    }

    public KernelParamDto toRestEntity(KernelParameter parameter) {
        KernelParamDto dto = new KernelParamDto();
        dto.setName(parameter.getName());
        dto.setType(kernelTypeMapper.toRestEntity(parameter.getType()));
        dto.setDirection(kernelDirectionMapper.toRestEntity(parameter.getDirection()));
        return dto;
    }

    public KernelParameters toModelEntity(List<KernelParamDto> kernelParams) {
        return new KernelParameters(kernelParams.stream()
                .map(this::toModelEntity)
                .collect(Collectors.toList()));
    }

    public KernelParameter toModelEntity(KernelParamDto kernelParam) {
        return KernelParameter.builder()
                .direction(kernelDirectionMapper.toModelEntity(kernelParam.getDirection()))
                .type(kernelTypeMapper.toModelEntity(kernelParam.getType()))
                .name(kernelParam.getName())
                .build();
    }
}
