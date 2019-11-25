package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.BaseEntity;

import javax.persistence.Entity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public abstract class EntityDaoBase<T extends BaseEntity> extends DaoBase {
    private final String entityName = getEntityClass().getAnnotation(Entity.class).name();

    protected abstract Class<T> getEntityClass();

    public void persist(T object) {
        entityManager.persist(object);
    }

    public void bulkPersist(Collection<T> objects) {
        objects.forEach(this::persist);
    }

    public Optional<T> find(String id) {
        return Optional.ofNullable(entityManager.find(getEntityClass(), id));
    }

    public void remove(T object) {
        entityManager.remove(object);
    }

    public long countAll() {
        return (long) entityManager.createQuery("select count(*) from " + entityName).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<T> selectAll() {
        return entityManager.createQuery("select e from " + entityName + " e").getResultList();
    }
}
