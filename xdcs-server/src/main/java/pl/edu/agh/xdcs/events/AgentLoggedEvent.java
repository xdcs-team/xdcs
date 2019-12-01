package pl.edu.agh.xdcs.events;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.db.entity.LogLineEntity;

/**
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class AgentLoggedEvent {
    private LogLineEntity logLine;
}
