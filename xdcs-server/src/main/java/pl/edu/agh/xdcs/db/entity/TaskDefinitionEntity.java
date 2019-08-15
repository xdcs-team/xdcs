package pl.edu.agh.xdcs.db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "TaskDefinition")
@Table(name = "XDCS_TASK_DEF_")
public class TaskDefinitionEntity extends BaseEntity {
    @Column(name = "NAME_")
    private String name;
}
