package pl.edu.agh.xdcs.db.entity;

import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public interface Task {
    Type getType();

    String getId();

    String getName();

    DeploymentDescriptorEntity getDeploymentDescriptor();

    HistoricalTaskEntity asHistorical();

    Optional<RuntimeTaskEntity> asRuntime();

    Optional<QueuedTaskEntity> asQueued();

    enum Type {
        HISTORICAL,
        RUNTIME,
        QUEUED,
    }
}
