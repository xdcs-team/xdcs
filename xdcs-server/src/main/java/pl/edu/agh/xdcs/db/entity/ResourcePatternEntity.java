package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.xdcs.db.conf.WildcardPatternConverter;
import pl.edu.agh.xdcs.util.WildcardPattern;

import javax.persistence.Column;
import javax.persistence.Convert;
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

    @Column(name = "TYPE_", nullable = false)
    private ResourceType type;

    @Column(name = "AGENT_NAME_PATTERN_", nullable = false)
    @Convert(converter = WildcardPatternConverter.class)
    private WildcardPattern agentNamePattern;

    @Column(name = "KEY_PATTERN_", nullable = false)
    @Convert(converter = WildcardPatternConverter.class)
    private WildcardPattern resourceKeyPattern;

    @Column(name = "QUANTITY_")
    private int quantity;
}
