package pl.edu.agh.xdcs.services;

import com.google.common.base.Preconditions;
import pl.edu.agh.xdcs.db.dao.DeploymentDescriptorDao;
import pl.edu.agh.xdcs.db.dao.HistoricalTaskDao;
import pl.edu.agh.xdcs.db.dao.LogLineDao;
import pl.edu.agh.xdcs.db.dao.QueuedTaskDao;
import pl.edu.agh.xdcs.db.dao.ResourcePatternDao;
import pl.edu.agh.xdcs.db.dao.RuntimeTaskDao;
import pl.edu.agh.xdcs.db.dao.TaskDao;
import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;
import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.LogLineEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.db.entity.ResourceType;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.events.AgentLoggedEvent;
import pl.edu.agh.xdcs.util.WildcardPattern;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class TaskService {
    @Inject
    private QueuedTaskDao queuedTaskDao;

    @Inject
    private HistoricalTaskDao historicalTaskDao;

    @Inject
    private RuntimeTaskDao runtimeTaskDao;

    @Inject
    private TaskDao taskDao;

    @Inject
    private DeploymentService deploymentService;

    @Inject
    private DeploymentDescriptorDao deploymentDescriptorDao;

    @Inject
    private ResourcePatternDao resourcePatternDao;

    @Inject
    private LogLineDao logLineDao;

    @Inject
    private Event<AgentLoggedEvent> agentLoggedEvent;

    public Optional<Task> getTaskById(String taskId) {
        return taskDao.findById(taskId);
    }

    public long countTasks() {
        return taskDao.count();
    }

    public List<Task> queryTasks(int from, int maxResults) {
        return taskDao.list(from, maxResults);
    }

    public List<ResourcePatternEntity> getResourcePatterns(String taskId) {
        return resourcePatternDao.findForTask(taskId);
    }

    public TaskCreationWizard newTask() {
        return new TaskCreationWizard();
    }

    public void reportCompletion(String taskId) {
        runtimeTaskDao.removeById(taskId);
        historicalTaskDao.setFinished(taskId);
    }

    public void reportFailure(String taskId) {
        runtimeTaskDao.removeById(taskId);
        historicalTaskDao.setErrored(taskId);
    }

    public void saveLog(Task task, Instant time, LogLineEntity.LogType type, byte[] contents) {
        LogLineEntity logLine = new LogLineEntity();
        logLine.setTask(task.asHistorical());
        logLine.setType(type);
        logLine.setTime(time);
        logLine.setContents(contents);
        logLineDao.persist(logLine);
        agentLoggedEvent.fire(AgentLoggedEvent.builder()
                .logLine(logLine)
                .build());
    }

    public List<LogLineEntity> getLogs(Task task, Instant from, Instant to) {
        return logLineDao.findByPeriod(task.getId(), from, to);
    }

    public class TaskCreationWizard {
        private String deploymentDescriptorId;
        private String name;
        private Set<ResourcePatternEntity> resourcePatterns = new HashSet<>();

        public TaskCreationWizard name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public TaskCreationWizard deploymentId(String deploymentId) {
            this.deploymentDescriptorId = deploymentService.getDeployment(deploymentId)
                    .getDescriptorId();
            return this;
        }

        public TaskCreationWizard addResourcePattern(
                ResourceType type,
                WildcardPattern agentPattern,
                WildcardPattern keyPattern,
                int quantity) {
            Preconditions.checkArgument(quantity > 0);

            ResourcePatternEntity pattern = new ResourcePatternEntity();
            pattern.setType(type);
            pattern.setAgentNamePattern(agentPattern);
            pattern.setQuantity(quantity);
            pattern.setResourceKeyPattern(keyPattern);
            resourcePatterns.add(pattern);
            return this;
        }

        public QueuedTaskEntity enqueue() {
            DeploymentDescriptorEntity descriptor = deploymentDescriptorDao.find(deploymentDescriptorId)
                    .orElseThrow(() -> new TaskCreationException("Unknown descriptor: " + deploymentDescriptorId));

            HistoricalTaskEntity historicalTask = new HistoricalTaskEntity();
            historicalTask.setName(name);
            historicalTask.setDeploymentDescriptor(descriptor);
            QueuedTaskEntity queuedTask = new QueuedTaskEntity(historicalTask.getId());
            queuedTask.setHistoricalTask(historicalTask);
            historicalTaskDao.persist(historicalTask);
            queuedTaskDao.persist(queuedTask);

            // persist resource patterns
            resourcePatterns.forEach(rp -> rp.setRequester(queuedTask));
            resourcePatternDao.bulkPersist(resourcePatterns);

            return queuedTask;
        }
    }
}
