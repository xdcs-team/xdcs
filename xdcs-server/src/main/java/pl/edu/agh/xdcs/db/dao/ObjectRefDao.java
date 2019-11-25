package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.ObjectRefEntity;

/**
 * @author Kamil Jarosz
 */
public class ObjectRefDao extends EntityDaoBase<ObjectRefEntity> {
    @Override
    protected Class<ObjectRefEntity> getEntityClass() {
        return ObjectRefEntity.class;
    }
}
