package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.db.dao.ArtifactTreeDao;
import pl.edu.agh.xdcs.db.dao.DeploymentDescriptorDao;
import pl.edu.agh.xdcs.db.dao.HistoricalTaskDao;
import pl.edu.agh.xdcs.db.dao.LogLineDao;
import pl.edu.agh.xdcs.db.dao.QueuedTaskDao;
import pl.edu.agh.xdcs.db.dao.ResourceDao;
import pl.edu.agh.xdcs.db.dao.ResourcePatternDao;
import pl.edu.agh.xdcs.db.dao.RuntimeTaskDao;
import pl.edu.agh.xdcs.db.dao.TaskDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.ArtifactTreeEntity;
import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;
import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.LogLineEntity;
import pl.edu.agh.xdcs.db.entity.ObjectRefEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.db.entity.Task;
import pl.edu.agh.xdcs.db.entity.WorkShape;
import pl.edu.agh.xdcs.events.AgentLoggedEvent;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.types.Blob;
import pl.edu.agh.xdcs.or.types.Tree;
import pl.edu.agh.xdcs.util.WildcardPattern;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private ObjectRepository objectRepository;

    @Inject
    private Event<AgentLoggedEvent> agentLoggedEvent;

    @Inject
    private ResourceDao resourceDao;

    @Inject
    private ArtifactTreeDao artifactTreeDao;

    public Optional<Task> getTaskById(String taskId) {
        return taskDao.findById(taskId);
    }

    public long countTasks() {
        return taskDao.count();
    }

    public List<Task> queryTasks(int from, int maxResults) {
        return taskDao.list(from, maxResults);
    }

    public List<Task> queryActiveTasks(int from, int maxResults) {
        return taskDao.listActive(from, maxResults);
    }

    public List<ResourcePatternEntity> getResourcePatterns(String taskId) {
        return resourcePatternDao.findForTask(taskId);
    }

    public TaskCreationWizard newTask() {
        return new TaskCreationWizard();
    }

    public void finishTask(Task task, AgentEntity agentEntity, Task.Result result) {
        resourceDao.unlockResources(task.getId(), agentEntity);
        if (!resourceDao.hasAnyLocks(task.getId())) {
            runtimeTaskDao.removeById(task.getId());
        }

        Optional<Task.Result> currentResult = task.asHistorical().getResult();
        if (result == Task.Result.ERRORED || !currentResult.isPresent()) {
            task.asHistorical().setResult(result);
        }
    }

    public void saveLog(Task task, Instant time, LogLineEntity.LogType type, byte[] contents, AgentEntity agent) {
        LogLineEntity logLine = new LogLineEntity();
        logLine.setTask(task.asHistorical());
        logLine.setType(type);
        logLine.setTime(time);
        logLine.setContents(contents);
        logLine.setLoggedBy(agent);
        logLineDao.persist(logLine);
        agentLoggedEvent.fire(AgentLoggedEvent.builder()
                .logLine(logLine)
                .build());
    }

    public List<LogLineEntity> getLogs(Task task, Instant from, Instant to, List<AgentEntity> agentEntities) {
        return logLineDao.query(task.getId(), from, to, agentEntities);
    }

    public void addArtifactTree(Task task, AgentEntity agent, String artifactTreeId) {
        objectRepository.validate(artifactTreeId, Tree.class);
        ArtifactTreeEntity artifactTree = new ArtifactTreeEntity();
        artifactTree.setUploadedBy(agent);
        artifactTree.setRoot(ObjectRefEntity.of(artifactTreeId, Tree.class));
        artifactTree.setTask(task.asHistorical());
        artifactTreeDao.persist(artifactTree);
    }

    public class TaskCreationWizard {
        private String deploymentDescriptorId;
        private String name;
        private Set<ResourcePatternEntity> resourcePatterns = new HashSet<>();
        private Map<Integer, ObjectRefEntity> kernelArguments;
        private WorkShape globalWorkShape;
        private WorkShape localWorkShape;

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
                WildcardPattern agentPattern,
                WildcardPattern keyPattern) {
            ResourcePatternEntity pattern = new ResourcePatternEntity();
            pattern.setAgentNameLike(agentPattern.toSqlLike());
            pattern.setResourceKeyLike(keyPattern.toSqlLike());
            resourcePatterns.add(pattern);
            return this;
        }

        public TaskCreationWizard addKernelArguments(List<byte[]> kernelArguments) {
            this.kernelArguments = new HashMap<>();
            for (int i = 0; i < kernelArguments.size(); i++) {
                String objectId = objectRepository.store(Blob.fromBytes(kernelArguments.get(i)));
                this.kernelArguments.put(i, ObjectRefEntity.of(objectId, Blob.class));
            }
            return this;
        }

        public TaskCreationWizard globalWorkShape(List<Integer> globalWorkShape) {
            this.globalWorkShape = WorkShape.fromList(globalWorkShape);
            return this;
        }

        public TaskCreationWizard localWorkShape(List<Integer> localWorkShape) {
            this.localWorkShape = WorkShape.fromList(localWorkShape);
            return this;
        }

        public QueuedTaskEntity enqueue() {
            DeploymentDescriptorEntity descriptor = deploymentDescriptorDao.find(deploymentDescriptorId)
                    .orElseThrow(() -> new TaskCreationException("Unknown descriptor: " + deploymentDescriptorId));

            HistoricalTaskEntity historicalTask = new HistoricalTaskEntity();
            historicalTask.setName(name);
            historicalTask.setDeploymentDescriptor(descriptor);
            historicalTask.setKernelArguments(kernelArguments);
            historicalTask.setGlobalWorkShape(globalWorkShape);
            historicalTask.setLocalWorkShape(localWorkShape);
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
