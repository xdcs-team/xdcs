package pl.edu.agh.xdcs.db.entity;

import java.time.Instant;
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

    HistoricalTaskEntity asHistorical();

    Optional<RuntimeTaskEntity> asRuntime();

    Optional<QueuedTaskEntity> asQueued();

    Instant getTimeCreated();

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
