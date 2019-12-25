package pl.edu.agh.xdcs.services.merging;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.db.dao.HistoricalTaskDao;
import pl.edu.agh.xdcs.db.dao.QueuedTaskDao;
import pl.edu.agh.xdcs.db.dao.ResourcePatternDao;
import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.events.TaskFinishedEvent;
import pl.edu.agh.xdcs.services.sweeper.SweepAfter;
import pl.edu.agh.xdcs.util.WildcardPattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class MergingTaskFactory {
    @Inject
    private Logger logger;

    @Inject
    private QueuedTaskDao queuedTaskDao;

    @Inject
    private HistoricalTaskDao historicalTaskDao;

    @Inject
    private ResourcePatternDao resourcePatternDao;

    @SweepAfter(message = "after creation of merge task")
    public void enqueueMergingTask(@Observes TaskFinishedEvent event) {
        if (!shouldExecuteMergingTaskFor(event.getTask())) {
            return;
        }

        HistoricalTaskEntity finishedTask = event.getTask();
        logger.info("Creating merging task for task with id: " + finishedTask.getId());

        HistoricalTaskEntity mergingTask = new HistoricalTaskEntity();
        mergingTask.setName("Merge " + finishedTask.getName());
        mergingTask.setDeploymentDescriptor(finishedTask.getDeploymentDescriptor());
        mergingTask.setOriginTask(finishedTask);
        QueuedTaskEntity queuedTask = new QueuedTaskEntity(mergingTask.getId());
        queuedTask.setHistoricalTask(mergingTask);
        historicalTaskDao.persist(mergingTask);
        queuedTaskDao.persist(queuedTask);

        WildcardPattern agentPattern = WildcardPattern.parse(finishedTask.getMergingAgent());
        WildcardPattern keyPattern = WildcardPattern.parse("/cpu");

        ResourcePatternEntity pattern = new ResourcePatternEntity();
        pattern.setAgentNameLike(agentPattern.toSqlLike());
        pattern.setResourceKeyLike(keyPattern.toSqlLike());
        pattern.setRequester(queuedTask);
        resourcePatternDao.persist(pattern);
    }

    private boolean shouldExecuteMergingTaskFor(HistoricalTaskEntity task) {
        return Optional.of(Task.Result.FINISHED).equals(task.getResult()) && !task.isMergingTask() &&
                task.getDeploymentDescriptor().getDefinition().getMergingScript() != null;
    }
}
