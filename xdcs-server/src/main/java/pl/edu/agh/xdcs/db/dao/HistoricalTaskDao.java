package pl.edu.agh.xdcs.db.dao;

import pl.edu.agh.xdcs.db.DatabaseInconsistencyException;
import pl.edu.agh.xdcs.db.entity.HistoricalTaskEntity;
import pl.edu.agh.xdcs.db.entity.Task.Result;

/**
 * @author Kamil Jarosz
 */
public class HistoricalTaskDao extends EntityDaoBase<HistoricalTaskEntity> {
    @Override
    protected Class<HistoricalTaskEntity> getEntityClass() {
        return HistoricalTaskEntity.class;
    }
}
