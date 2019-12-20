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
@Entity(name = "ResourcePattern")
@Table(name = "XDCS_RES_PATTERN_")
public class ResourcePatternEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_", nullable = false)
    private QueuedTaskEntity requester;

    @Column(name = "AGENT_NAME_PATTERN_", nullable = false)
    private String agentNameLike;

    @Column(name = "KEY_PATTERN_", nullable = false)
    private String resourceKeyLike;

    @Override
    public String toString() {
        return '[' + agentNameLike + " / " + resourceKeyLike + ']';
    }
}
