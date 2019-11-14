package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
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
@Entity(name = "Resource")
@Table(name = "XDCS_RESOURCE_")
public class ResourceEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID_")
    private AgentEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOCKED_BY_")
    private RuntimeTaskEntity lockedBy;

    @Column(name = "TYPE_")
    private ResourceType type;

    @Column(name = "PATH_")
    private String resourcePath;
}
