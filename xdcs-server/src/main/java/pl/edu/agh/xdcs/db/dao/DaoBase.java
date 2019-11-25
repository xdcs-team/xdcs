package pl.edu.agh.xdcs.db.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Kamil Jarosz
 */
public abstract class DaoBase {
    @PersistenceContext(unitName = "xdcs")
    protected EntityManager entityManager;
}
