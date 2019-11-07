package pl.edu.agh.xdcs.db.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.xdcs.db.conf.ClassConverter;
import pl.edu.agh.xdcs.or.ObjectBase;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ObjectRef")
@Table(name = "XDCS_OR_REF")
public class ObjectRefEntity extends BaseEntity {
    @Column(name = "REF_OBJ_ID_", length = 40)
    private String referencedObjectId;

    @Column(name = "REF_OBJ_TYPE_", length = 256)
    @Convert(converter = ClassConverter.class)
    private Class<? extends ObjectBase> referencedObjectType;
}
