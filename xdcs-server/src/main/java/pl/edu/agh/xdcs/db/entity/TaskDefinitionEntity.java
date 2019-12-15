package pl.edu.agh.xdcs.db.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@Entity(name = "TaskDefinition")
@Table(name = "XDCS_TASK_DEF_")
public class TaskDefinitionEntity extends BaseEntity {
    private static final KernelParameters.Converter KERN_PARAMS_CONVERTER = new KernelParameters.Converter();

    @Column(name = "NAME_")
    private String name;

    @Column(name = "TYPE_", length = 10)
    @Enumerated(value = EnumType.STRING)
    private TaskType type;

    @Column(name = "KERN_FILE_")
    private String kernelFile;

    @Column(name = "KERN_NAME_")
    private String kernelName;

    @Column(name = "KERN_PARAMS_")
    @Getter(AccessLevel.NONE)
    private byte[] kernelParams;

    @Column(name = "SCRIPT_PATH_", length = 511)
    private String scriptPath;

    @Column(name = "DOCKERFILE_", length = 511)
    private String dockerfile;

    @Column(name = "ALLOCATE_TTY_")
    private Boolean allocatePseudoTty;

    @Column(name = "ARTIFACTS_")
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> artifacts;

    public KernelParameters getKernelParams() {
        return KERN_PARAMS_CONVERTER.convertToEntityAttribute(kernelParams);
    }

    public void setKernelParams(KernelParameters kernelParameters) {
        kernelParams = KERN_PARAMS_CONVERTER.convertToDatabaseColumn(kernelParameters);
    }
}
