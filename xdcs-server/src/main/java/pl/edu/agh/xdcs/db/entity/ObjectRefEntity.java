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
@Entity
@Table(name = "XDCS_OR_REF")
public class ObjectRefEntity extends BaseEntity {
    @Column(name = "REF_OBJ_ID_", length = 40)
    private String referencedObjectId;
}
