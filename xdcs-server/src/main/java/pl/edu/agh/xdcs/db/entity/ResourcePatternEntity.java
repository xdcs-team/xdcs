package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.xdcs.db.conf.PatternConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.regex.Pattern;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Resource")
@Table(name = "XDCS_RES_PATTERN_")
public class ResourcePatternEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_")
    private QueuedTaskEntity requester;

    @Column(name = "TYPE_")
    private ResourceType type;

    @Column(name = "AGENT_NAME_PATTERN_")
    @Convert(converter = PatternConverter.class)
    private Pattern agentNamePattern;

    @Column(name = "QUANTITY_")
    private long quantity;
}
