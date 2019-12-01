package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.entity.LogLineEntity;

/**
 * @author Kamil Jarosz
 */
public class LogLineDao extends EntityDaoBase<LogLineEntity> {
    @Override
    protected Class<LogLineEntity> getEntityClass() {
        return LogLineEntity.class;
    }
}
