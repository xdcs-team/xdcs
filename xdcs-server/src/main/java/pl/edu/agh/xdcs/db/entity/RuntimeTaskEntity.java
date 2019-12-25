package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Entity(name = "RuntimeTask")
@Table(name = "XDCS_TASK_")
public class RuntimeTaskEntity extends BaseEntity implements Task {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_", nullable = false)
    private HistoricalTaskEntity historicalTask;

    public RuntimeTaskEntity(String id) {
        super(id);
    }

    @Override
    public Type getType() {
        return Type.RUNTIME;
    }

    @Override
    public Optional<Result> getResult() {
        return historicalTask.getResult();
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
    public Optional<Task> getOriginTask() {
        return historicalTask.getOriginTask();
    }

    @Override
    public HistoricalTaskEntity asHistorical() {
        return historicalTask;
    }

    @Override
    public Optional<RuntimeTaskEntity> asRuntime() {
        return Optional.of(this);
    }

    @Override
    public Optional<QueuedTaskEntity> asQueued() {
        return Optional.empty();
    }

    @Override
    public Instant getTimeCreated() {
        return historicalTask.getTimeCreated();
    }
}
