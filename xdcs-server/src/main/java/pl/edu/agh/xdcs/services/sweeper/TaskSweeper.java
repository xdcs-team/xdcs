package pl.edu.agh.xdcs.services.sweeper;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.agents.AgentManager;
import pl.edu.agh.xdcs.api.TaskRunnerGrpc;
import pl.edu.agh.xdcs.api.TaskSubmit;
import pl.edu.agh.xdcs.db.dao.QueuedTaskDao;
import pl.edu.agh.xdcs.db.dao.ResourcePatternDao;
import pl.edu.agh.xdcs.db.dao.RuntimeTaskDao;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.ResourcePatternEntity;
import pl.edu.agh.xdcs.db.entity.RuntimeTaskEntity;
import pl.edu.agh.xdcs.grpc.session.GrpcSessionManager;
import pl.edu.agh.xdcs.util.WildcardPattern;

import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
@Singleton
public class TaskSweeper {
    private static final int CHUNK_SIZE = 10;
    private static final Duration MIN_WAIT_TIME = Duration.ofSeconds(10);

    @Inject
    private Logger logger;

    @Inject
    private QueuedTaskDao queuedTaskDao;

    @Inject
    private RuntimeTaskDao runtimeTaskDao;

    @Inject
    private AgentManager agentManager;

    @Inject
    private GrpcSessionManager sessionManager;

    @Inject
    private ResourcePatternDao resourcePatternDao;

    @Asynchronous
    public void startSweeping() {
        sweep();
    }

    @Schedule(second = "*/15", minute = "*", hour = "*", persistent = false)
    public void sweep() {
        logger.info("Sweep started");
        while (true) {
            if (!checkNextChunk()) {
                return;
            }
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private boolean checkNextChunk() {
        List<QueuedTaskEntity> toCheck = queuedTaskDao.checkCandidates(CHUNK_SIZE, MIN_WAIT_TIME);
        if (toCheck.isEmpty()) return false;

        logger.debug("Checking a chunk of " + toCheck.size() + " tasks");

        toCheck.forEach(this::checkTask);
        return true;
    }

    private void checkTask(QueuedTaskEntity task) {
        logger.trace("Checking task " + task.getId());

        List<ResourcePatternEntity> resourcePatterns = resourcePatternDao.findForTask(task.getId());
        for (ResourcePatternEntity resourcePattern : resourcePatterns) {
            WildcardPattern agentNamePattern = resourcePattern.getAgentNamePattern();
            Optional<Agent> matchedAgent = agentManager.getAllAgents()
                    .stream()
                    .filter(Agent::isReady)
                    .filter(agent -> agentNamePattern.matches(agent.getName()))
                    .findAny();
            if (!matchedAgent.isPresent()) {
                logger.debug("Task " + task.getId() + " lacks an agent with name '" + agentNamePattern + "'");
                return;
            }
        }

        Set<Agent> agents = resourcePatterns.stream()
                .map(ResourcePatternEntity::getAgentNamePattern)
                .flatMap(pattern -> agentManager.getAllAgents()
                        .stream()
                        .filter(agent -> pattern.matches(agent.getName()))
                        .findAny()
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(Collectors.toSet());

        startTask(task, agents);
    }

    private void startTask(QueuedTaskEntity task, Set<Agent> agents) {
        logger.info("Starting task " + task.getId() + " on agents " + agents);

        RuntimeTaskEntity runtimeTask = new RuntimeTaskEntity(task.getId());
        runtimeTask.setHistoricalTask(task.getHistoricalTask());
        runtimeTaskDao.persist(runtimeTask);
        queuedTaskDao.remove(task);

        String deploymentId = task.getDeploymentDescriptor().getDeploymentRef().getReferencedObjectId();
        for (Agent agent : agents) {
            TaskRunnerGrpc.TaskRunnerBlockingStub taskRunner = sessionManager.getStubProducer(agent)
                    .getTaskRunnerBlockingStub();
            taskRunner.submit(TaskSubmit.newBuilder()
                    .setDeploymentId(deploymentId)
                    .setTaskId(task.getId())
                    .build());
        }
    }
}
