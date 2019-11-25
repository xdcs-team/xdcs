package pl.edu.agh.xdcs.services.sweeper;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.grpc.events.AgentRegisteredEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class SweeperTrigger {
    @Inject
    private Logger logger;

    @Inject
    private TaskSweeper taskSweeper;

    public void sweepOnAgentRegister(@Observes AgentRegisteredEvent event) {
        logger.debug("Sweep triggered by agent registration");
        taskSweeper.startSweeping();
    }
}
