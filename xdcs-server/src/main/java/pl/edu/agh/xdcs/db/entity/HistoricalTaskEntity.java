package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "HisTask")
@Table(name = "XDCS_HIS_TASK_")
public class HistoricalTaskEntity extends BaseEntity {
    @JoinColumn(name = "DEPLOYMENT_DESC_")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DeploymentDescriptorEntity deploymentDescriptor;

    public HistoricalTaskEntity(String id) {
        super(id);
    }
}
