package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
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
@Entity(name = "LogLine")
@Table(name = "XDCS_LOG_", indexes = {
        @Index(columnList = "TASK_ID_"),
        @Index(columnList = "TYPE_"),
        @Index(columnList = "TIME_")
})
public class LogLineEntity extends BaseEntity {
    @JoinColumn(name = "TASK_ID_", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private HistoricalTaskEntity task;

    @Column(name = "TIME_", nullable = false)
    private Instant time;

    @Column(name = "TYPE_", nullable = false)
    @Enumerated(EnumType.STRING)
    private LogType type;

    @Column(name = "CONTENTS_", nullable = false)
    private byte[] contents;

    @JoinColumn(name = "LOGGED_BY_", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private AgentEntity loggedBy;

    public enum LogType {
        INTERNAL,
        STDOUT,
        STDERR,
        ;
    }
}
