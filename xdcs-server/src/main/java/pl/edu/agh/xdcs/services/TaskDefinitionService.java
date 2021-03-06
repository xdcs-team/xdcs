package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.db.dao.TaskDefinitionDao;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.workspace.Workspace;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UncheckedIOException;
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
        return taskDefinitionDao.find(taskDefinitionId);
    }

    public List<TaskDefinitionEntity> getTaskDefinitions(int from, int limit) {
        return taskDefinitionDao.listTaskDefinitions(from, limit);
    }

    public Workspace getWorkspace(TaskDefinitionEntity definition) {
        return workspaceService.forDefinition(definition);
    }

    public TaskDefinitionEntity newTaskDefinition(String name) {
        TaskDefinitionEntity definition = new TaskDefinitionEntity();
        definition.setName(name);
        taskDefinitionDao.persist(definition);
        try {
            workspaceService.forDefinition(definition).setup();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return definition;
    }

    public long countAllTaskDefinitions() {
        return taskDefinitionDao.countAll();
    }
}
