package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Agent")
@Table(name = "XDCS_AGENT_", indexes = {
        @Index(columnList = "NAME_", unique = true)
})
public class AgentEntity extends BaseEntity {
    @Column(name = "NAME_")
    private String name;

    @Column(name = "DISPLAY_NAME_")
    private String displayName;

    @Column(name = "STATUS_")
    private Status status;

    public enum Status {
        UNAVAILABLE,
        READY,
        BUSY,
        OFFLINE,
    }
}
