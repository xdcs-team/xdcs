package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Deployment")
@Table(name = "XDCS_DEPLOYMENT_DESC_")
public class DeploymentDescriptorEntity extends BaseEntity {
    @JoinColumn(name = "DEF_ID_")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private TaskDefinitionEntity definition;

    @Column(name = "DEPLOY_TIME_")
    private Instant timeDeployed = Instant.now();

    @Column(name = "DESCRIPTION_")
    private String description = "";

    @JoinColumn(name = "DEPLOYMENT_REF_")
    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    private ObjectRefEntity deploymentRef;
}
