package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Entity(name = "ArtifactTree")
@Table(name = "ARTIFACT_TREE_", indexes = {
        @Index(columnList = "TASK_ID_,UPLOADED_BY_", unique = true),
        @Index(columnList = "UPLOADED_BY_")
})
public class ArtifactTreeEntity extends BaseEntity {
    @JoinColumn(name = "TASK_ID_", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private HistoricalTaskEntity task;

    @Column(name = "TIME_", nullable = false)
    private Instant time = Instant.now();

    @JoinColumn(name = "UPLOADED_BY_", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private AgentEntity uploadedBy;

    @JoinColumn(name = "ROOT_")
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ObjectRefEntity root;
}
