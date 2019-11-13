package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "TaskQueue")
@Table(name = "XDCS_TASK_QUEUE_", indexes = {
        @Index(columnList = "LAST_CHECK_"),
        @Index(columnList = "CREATED_")
})
public class QueuedTaskEntity extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_", nullable = false)
    private HistoricalTaskEntity historicalTask;

    @Column(name = "LAST_CHECK_")
    private Instant lastCheck = Instant.MIN;

    @Column(name = "CREATED_")
    private Instant created = Instant.now();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "requester")
    private Set<ResourcePatternEntity> resourcePatterns;
}
