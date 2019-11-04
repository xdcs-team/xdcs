package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.db.dao.TaskQueueDao;
import pl.edu.agh.xdcs.db.entity.QueuedTaskEntity;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class TaskQueueService {
    @Inject
    private TaskQueueDao taskQueueDao;

    public void enqueue(QueuedTaskEntity task) {
        taskQueueDao.persist(task);
    }
}
