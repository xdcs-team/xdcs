package pl.edu.agh.xdcs.events;

import lombok.Builder;
import lombok.Getter;
import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.Task;

/**
 * @author Jan Rodzoń
 */
@Getter
@Builder
public class TaskFinishedEvent {
    private HistoricalTaskEntity task;
}
