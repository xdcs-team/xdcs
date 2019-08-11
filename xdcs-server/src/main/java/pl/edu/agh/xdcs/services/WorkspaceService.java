package pl.edu.agh.xdcs.services;

import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.fsrepo.FilesystemRepository;
import pl.edu.agh.xdcs.workspace.Workspace;

import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class WorkspaceService {
    @Inject
    private FilesystemRepository filesystemRepository;

    public Workspace forDefinition(TaskDefinitionEntity definition) {
        return Workspace.forPath(filesystemRepository.getWorkspacePath(definition.getId()));
    }

    public Workspace forObject(String objectId) {
        return Workspace.forObject(filesystemRepository.getObjectRepository(), objectId);
    }
}
