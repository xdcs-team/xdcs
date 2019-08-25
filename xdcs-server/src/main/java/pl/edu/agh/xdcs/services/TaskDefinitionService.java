package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.db.dao.TaskDefinitionDao;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.workspace.Workspace;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class TaskDefinitionService {
    @Inject
    private TaskDefinitionDao taskDefinitionDao;

    @Inject
    private WorkspaceService workspaceService;

    public Optional<TaskDefinitionEntity> getTaskDefinition(String taskDefinitionId) {
        return Optional.ofNullable(taskDefinitionDao.find(taskDefinitionId));
    }

    public List<TaskDefinitionEntity> getTaskDefinitions(int from, int limit) {
        return taskDefinitionDao.listTaskDefinitions(from, limit);
    }

    public Workspace getWorkspace(TaskDefinitionEntity definition) {
        return workspaceService.forDefinition(definition);
    }

    public TaskDefinitionEntity newTaskDefinition(String name) {
        TaskDefinitionEntity definition = TaskDefinitionEntity.builder()
                .name(name)
                .build();
        taskDefinitionDao.persist(definition);
        return definition;
    }

    public long countAllTaskDefinitions() {
        return taskDefinitionDao.countAll();
    }
}
