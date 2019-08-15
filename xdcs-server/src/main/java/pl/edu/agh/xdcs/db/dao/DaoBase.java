package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Kamil Jarosz
 */
public abstract class DaoBase<T extends BaseEntity> {
    @PersistenceContext(unitName = "xdcs")
    protected EntityManager entityManager;

    private final String entityName = getEntityClass().getAnnotation(Entity.class).name();

    protected abstract Class<T> getEntityClass();

    public void persist(T object) {
        entityManager.persist(object);
    }

    public T find(String id) {
        return entityManager.find(getEntityClass(), id);
    }

    public void remove(T object) {
        entityManager.remove(object);
    }

    public long countAll() {
        return (long) entityManager.createQuery("select count(*) from " + entityName).getSingleResult();
    }
}
