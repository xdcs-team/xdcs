package pl.edu.agh.xdcs.services.sweeper;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.api.KernelConfig;
import pl.edu.agh.xdcs.api.TaskRunnerGrpc;
import pl.edu.agh.xdcs.api.TaskSubmit;
import pl.edu.agh.xdcs.db.dao.QueuedTaskDao;
import pl.edu.agh.xdcs.db.dao.ResourceDao;
import pl.edu.agh.xdcs.db.dao.ResourceDao.BoundResource;
import pl.edu.agh.xdcs.db.dao.RuntimeTaskDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.ObjectRefEntity;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;
import pl.edu.agh.xdcs.db.entity.RuntimeTaskEntity;
import pl.edu.agh.xdcs.grpc.session.GrpcSessionManager;

import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
    private GrpcSessionManager sessionManager;

    @Inject
    private ResourceDao resourceDao;

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
        RuntimeTaskEntity runtimeTask = new RuntimeTaskEntity(task.getId());
        logger.trace("Checking task " + task.getId());

        List<BoundResource> resources;
        try {
            resources = resourceDao.lockResources(task, runtimeTask);
        } catch (ResourceDao.ResourceLockFailedException e) {
            logger.debug("Task " + task.getId() + " lacks free resources: " + e.getFailedResources());
            return;
        }

        startTask(task, runtimeTask, resources);
    }

    private void startTask(QueuedTaskEntity task, RuntimeTaskEntity runtimeTask, List<BoundResource> resources) {
        logger.info("Starting task " + task.getId() + " on resources " + resources);
        Set<AgentEntity> agents = resources.stream()
                .map(res -> res.getResource().getOwner())
                .collect(Collectors.toSet());

        runtimeTask.setHistoricalTask(task.getHistoricalTask());
        runtimeTaskDao.persist(runtimeTask);
        queuedTaskDao.remove(task);

        String deploymentId = task.getDeploymentDescriptor().getDeploymentRef().getReferencedObjectId();

        KernelConfig kernelConfig = null;
        if (task.isKernelExecutionTask()) {
            kernelConfig = prepareKernelConfig(task.getHistoricalTask());
        }

        Collection<String> agentIps = getAgentIps(agents);
        int agentId = 0;
        for (AgentEntity agent : agents) {
            TaskRunnerGrpc.TaskRunnerBlockingStub taskRunner = sessionManager.getStubProducer(agent)
                    .getTaskRunnerBlockingStub();
            TaskSubmit.Builder taskSubmit = TaskSubmit.newBuilder()
                    .setDeploymentId(deploymentId)
                    .setTaskId(task.getId())
                    .setAgentVariables(
                            TaskSubmit.AgentVariables.newBuilder()
                                    .addAllAgentIps(agentIps)
                                    .setAgentIpMine(agent.getAddress().getHostAddress())
                                    .setAgentCount(agents.size())
                                    .setAgentId(agentId)
                                    .build()
                    );
            if (task.isKernelExecutionTask()) {
                taskSubmit.setKernelConfig(kernelConfig);
            }
            taskRunner.submit(taskSubmit.build());
            agentId++;
        }
    }

    private KernelConfig prepareKernelConfig(HistoricalTaskEntity task) {
        KernelConfig.Builder kernelConfig = KernelConfig.newBuilder()
                .addAllGlobalWorkShape(task.getGlobalWorkShape().asList())
                .addAllKernelArguments(
                        new TreeMap<>(task.getKernelArguments())
                                .values().stream()
                                .map(ObjectRefEntity::getReferencedObjectId)
                                .collect(Collectors.toList()));
        if (task.getLocalWorkShape() != null) {
            kernelConfig.addAllLocalWorkShape(task.getLocalWorkShape().asList());
        }
        return kernelConfig.build();
    }

    private Collection<String> getAgentIps(Set<AgentEntity> agents) {
        return agents.stream()
                .map(AgentEntity::getAddress)
                .map(InetAddress::getHostAddress)
                .collect(Collectors.toList());
    }
}
