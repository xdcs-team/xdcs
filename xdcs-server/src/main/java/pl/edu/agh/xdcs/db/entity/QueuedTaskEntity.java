package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "TaskQueue")
@Table(name = "XDCS_TASK_QUEUE_", indexes = {
        @Index(columnList = "LAST_CHECK_"),
        @Index(columnList = "CREATED_")
})
public class QueuedTaskEntity extends BaseEntity implements Task {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_", nullable = false)
    private HistoricalTaskEntity historicalTask;

    @Column(name = "LAST_CHECK_")
    private Instant lastCheck = Instant.ofEpochSecond(0);

    @Column(name = "CREATED_")
    private Instant created = Instant.now();

    public QueuedTaskEntity(String id) {
        super(id);
    }

    @Override
    public Type getType() {
        return Type.QUEUED;
    }

    @Override
    public String getName() {
        return historicalTask.getName();
    }

    @Override
    public DeploymentDescriptorEntity getDeploymentDescriptor() {
        return historicalTask.getDeploymentDescriptor();
    }

    @Override
    public HistoricalTaskEntity asHistorical() {
        return historicalTask;
    }

    @Override
    public Optional<RuntimeTaskEntity> asRuntime() {
        return Optional.empty();
    }

    @Override
    public Optional<QueuedTaskEntity> asQueued() {
        return Optional.of(this);
    }
}
