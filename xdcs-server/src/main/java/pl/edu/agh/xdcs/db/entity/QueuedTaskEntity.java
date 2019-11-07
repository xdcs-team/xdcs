package pl.edu.agh.xdcs.db.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "TaskQueue")
@Table(name = "XDCS_TASK_QUEUE_", indexes = {
        @Index(columnList = "LAST_CHECK_"),
        @Index(columnList = "CREATED_")
})
public class QueuedTaskEntity extends BaseEntity {
    @Column(name = "DEPLOYMENT_DESC_")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DeploymentDescriptorEntity deploymentDescriptor;

    @Column(name = "LAST_CHECK_")
    private Instant lastCheck = Instant.MIN;

    @Column(name = "CREATED_")
    private Instant created = Instant.now();
}
