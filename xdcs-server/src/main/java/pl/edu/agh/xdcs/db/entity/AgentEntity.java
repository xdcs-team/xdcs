package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Agent")
@Table(name = "XDCS_AGENT_")
public class AgentEntity extends BaseEntity {
    @Column(name = "NAME_")
    private String name;

    @Column(name = "DISPLAY_NAME_")
    private String displayName;
}
