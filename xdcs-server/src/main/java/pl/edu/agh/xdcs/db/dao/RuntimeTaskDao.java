package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.RuntimeTaskEntity;

/**
 * @author Kamil Jarosz
 */
public class RuntimeTaskDao extends EntityDaoBase<RuntimeTaskEntity> {
    @Override
    protected Class<RuntimeTaskEntity> getEntityClass() {
        return RuntimeTaskEntity.class;
    }
}
