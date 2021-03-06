package pl.edu.agh.xdcs.db.entity;

import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.xdcs.db.conf.UUIDConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Kamil Jarosz
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @Column(name = "ID_", nullable = false)
    @Convert(converter = UUIDConverter.class)
    private String id;

    @Version
    @Column(name = "REV_")
    private Long revision;

    protected BaseEntity() {
        this.id = UUID.randomUUID().toString();
    }

    protected BaseEntity(String id) {
        UUID.fromString(id);
        this.id = id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
