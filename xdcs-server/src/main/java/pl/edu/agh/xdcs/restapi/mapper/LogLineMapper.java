package pl.edu.agh.xdcs.restapi.mapper;

import com.google.common.collect.ImmutableMap;
import pl.edu.agh.xdcs.db.entity.LogLineEntity;
import pl.edu.agh.xdcs.db.entity.LogLineEntity.LogType;
import pl.edu.agh.xdcs.mapper.EnumMapper;
import pl.edu.agh.xdcs.restapi.model.LogDto;
import pl.edu.agh.xdcs.security.web.UserContext;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class LogLineMapper {
    private EnumMapper<LogType, LogDto.TypeEnum> typeMapper =
            EnumMapper.forMapping(ImmutableMap.<LogType, LogDto.TypeEnum>builder()
                    .put(LogType.INTERNAL, LogDto.TypeEnum.INT)
                    .put(LogType.STDOUT, LogDto.TypeEnum.OUT)
                    .put(LogType.STDERR, LogDto.TypeEnum.ERR)
                    .build());

    @Inject
    private UserContext userContext;

    public LogDto toRestEntity(LogLineEntity model) {
        LogDto dto = new LogDto();
        dto.setTime(model.getTime().atOffset(userContext.getCurrentZoneOffset()));
        dto.setContents(model.getContents());
        dto.setType(typeMapper.toApiEntity(model.getType()));
        return dto;
    }

    public List<LogDto> toRestEntities(List<LogLineEntity> lines) {
        return lines.stream()
                .map(this::toRestEntity)
                .collect(Collectors.toList());
    }
}
