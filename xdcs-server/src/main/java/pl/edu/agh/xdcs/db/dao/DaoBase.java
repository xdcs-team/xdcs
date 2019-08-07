package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.BaseEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;

/**
 * @author Kamil Jarosz
 */
public abstract class DaoBase<T extends BaseEntity> {
    @PersistenceContext(unitName = "xdcs")
    protected EntityManager entityManager;

    protected abstract Class<T> getEntityClass();

    public void persist(T object) {
        entityManager.persist(object);
    }

    public T find(UUID id) {
        return entityManager.find(getEntityClass(), id);
    }

    public void remove(T object) {
        entityManager.remove(object);
    }
}
