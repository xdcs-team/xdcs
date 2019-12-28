package pl.edu.agh.xdcs.db.entity;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public interface Task {
    Type getType();

    Optional<Result> getResult();

    String getId();

    String getName();

    DeploymentDescriptorEntity getDeploymentDescriptor();

    Optional<Task> getOriginTask();

    HistoricalTaskEntity asHistorical();

    Optional<RuntimeTaskEntity> asRuntime();

    Optional<QueuedTaskEntity> asQueued();

    Instant getTimeCreated();

    default boolean isKernelExecutionTask() {
        return EnumSet.of(TaskType.CUDA, TaskType.OPENCL).contains(
                this.getDeploymentDescriptor().getDefinition().getType()
        );
    }

    default boolean isMergingTask() {
        return this.asHistorical().getOriginTask().isPresent();
    }

    enum Type {
        HISTORICAL,
        RUNTIME,
        QUEUED,
    }

    enum Result {
        FINISHED,
        ERRORED,
        CANCELED,
    }
}
