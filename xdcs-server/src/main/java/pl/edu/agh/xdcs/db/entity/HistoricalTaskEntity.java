package pl.edu.agh.xdcs.db.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Map;
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
    private static final EnvironmentVariables.Converter ENV_VARS_CONVERTER = new EnvironmentVariables.Converter();

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

    @Column(name = "ENV_VARIABLES_")
    @Getter(AccessLevel.NONE)
    private byte[] environmentVariables;

    @MapKeyColumn(name="POSITION_")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Map<Integer, ObjectRefEntity> kernelArguments;

    @Column(name = "GLOBAL_WORK_SHAPE_")
    @Convert(converter = WorkShape.Converter.class)
    private WorkShape globalWorkShape;

    @Column(name = "LOCAL_WORK_SHAPE_")
    @Convert(converter = WorkShape.Converter.class)
    private WorkShape localWorkShape;

    @Column(name = "MERGING_AGENT_")
    private String mergingAgent;

    @OneToOne
    @JoinColumn(name = "ORIGIN_TASK_")
    private HistoricalTaskEntity originTask;

    @Override
    public Type getType() {
        return Type.HISTORICAL;
    }

    @Override
    public Optional<Result> getResult() {
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Task> getOriginTask() {
        return Optional.ofNullable(originTask);
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

    public EnvironmentVariables getEnvironmentVariables() {
        return ENV_VARS_CONVERTER.convertToEntityAttribute(environmentVariables);
    }

    public void setEnvironmentVariables(EnvironmentVariables vars) {
        environmentVariables = ENV_VARS_CONVERTER.convertToDatabaseColumn(vars);
    }
}
