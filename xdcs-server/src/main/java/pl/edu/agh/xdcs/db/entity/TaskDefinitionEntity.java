package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "TaskDefinition")
@Table(name = "XDCS_TASK_DEF_")
public class TaskDefinitionEntity extends BaseEntity {
    @Column(name = "NAME_")
    private String name;

    @Column(name = "TYPE_")
    @Enumerated(value = EnumType.STRING)
    private TaskType type;

    @Column(name = "KERN_FILE_")
    private String kernelFile;

    @Column(name = "KERN_NAME_")
    private String kernelName;

    @Column(name = "KERN_PARAMS_")
    @Convert(converter = KernelParameters.Converter.class)
    private KernelParameters kernelParams;

    @Column(name = "SCRIPT_PATH_")
    private String scriptPath;

    @Column(name = "DOCKERFILE_")
    private String dockerfile;
}
