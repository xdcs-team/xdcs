package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "HisTask")
@Table(name = "XDCS_HIS_TASK_")
public class HistoricalTaskEntity extends BaseEntity implements Task {
    @JoinColumn(name = "DEPLOYMENT_DESC_", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DeploymentDescriptorEntity deploymentDescriptor;

    @Column(name = "NAME_")
    private String name;

    @Column(name = "CREATED_", nullable = false)
    private Instant timeCreated = Instant.now();

    @Column(name = "RESULT_")
    @Enumerated(EnumType.STRING)
    private Result result;

    @JoinColumn(name = "ARTIFACT_TREE_")
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ObjectRefEntity artifactTree;

    @Override
    public Type getType() {
        return Type.HISTORICAL;
    }

    @Override
    public Optional<Result> getResult() {
        return Optional.ofNullable(result);
    }

    @Override
    public HistoricalTaskEntity asHistorical() {
        return this;
    }

    @Override
    public Optional<RuntimeTaskEntity> asRuntime() {
        return Optional.empty();
    }

    @Override
    public Optional<QueuedTaskEntity> asQueued() {
        return Optional.empty();
    }
}
