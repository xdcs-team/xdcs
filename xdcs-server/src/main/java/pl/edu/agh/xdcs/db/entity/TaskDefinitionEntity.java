package pl.edu.agh.xdcs.db.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "XDCS_TASK_DEF_")
public class TaskDefinitionEntity extends BaseEntity {
    @Column(name = "NAME_")
    private String name;

    @JoinColumn(name = "TREE_ROOT_")
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private ObjectRefEntity objectRepositoryRoot;
}
