package pl.edu.agh.xdcs.agentapi.mapper;

import pl.edu.agh.xdcs.api.Logs;
import pl.edu.agh.xdcs.db.entity.LogLineEntity;
import pl.edu.agh.xdcs.mapper.SimpleMapper;
import pl.edu.agh.xdcs.mapper.UnsatisfiedMappingException;

/**
 * @author Kamil Jarosz
 */
public class LogTypeMapper implements SimpleMapper<LogLineEntity.LogType, Logs.LogType> {
    @Override
    public LogLineEntity.LogType toModelEntity(Logs.LogType rest) {
        switch (rest) {
            case INTERNAL:
                return LogLineEntity.LogType.INTERNAL;
            case STDOUT:
                return LogLineEntity.LogType.STDOUT;
            case STDERR:
                return LogLineEntity.LogType.STDERR;
            default:
                throw new UnsatisfiedMappingException();
        }
    }

    @Override
    public Logs.LogType toApiEntity(LogLineEntity.LogType model) {
        switch (model) {
            case INTERNAL:
                return Logs.LogType.INTERNAL;
            case STDOUT:
                return Logs.LogType.STDOUT;
            case STDERR:
                return Logs.LogType.STDERR;
            default:
                throw new UnsatisfiedMappingException();
        }
    }
}
