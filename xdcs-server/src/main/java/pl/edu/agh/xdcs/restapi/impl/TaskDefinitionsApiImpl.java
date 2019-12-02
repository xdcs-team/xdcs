package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.db.dao.DeploymentDescriptorDao;
import pl.edu.agh.xdcs.db.entity.DeploymentDescriptorEntity;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.restapi.TaskDefinitionsApi;
import pl.edu.agh.xdcs.restapi.mapper.DeploymentConfigMapper;
import pl.edu.agh.xdcs.restapi.mapper.DeploymentMapper;
import pl.edu.agh.xdcs.restapi.mapper.FileDescriptionMapper;
import pl.edu.agh.xdcs.restapi.mapper.FileTypeMapper;
import pl.edu.agh.xdcs.restapi.mapper.TaskDefinitionMapper;
import pl.edu.agh.xdcs.restapi.model.DeploymentConfigDto;
import pl.edu.agh.xdcs.restapi.model.DeploymentDescriptorsDto;
import pl.edu.agh.xdcs.restapi.model.FileDto;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionDto;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionsDto;
import pl.edu.agh.xdcs.restapi.util.RestUtils;
import pl.edu.agh.xdcs.services.TaskDefinitionService;
import pl.edu.agh.xdcs.util.UriResolver;
import pl.edu.agh.xdcs.workspace.FileDescription;
import pl.edu.agh.xdcs.workspace.Workspace;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class TaskDefinitionsApiImpl implements TaskDefinitionsApi {
    @Inject
    private UriResolver resolver;

    @Inject
    private TaskDefinitionService taskDefinitionService;

    @Inject
    private TaskDefinitionMapper taskDefinitionMapper;

    @Inject
    private DeploymentConfigMapper deploymentConfigMapper;

    @Inject
    private FileDescriptionMapper fileDescriptionMapper;

    @Inject
    private DeploymentMapper deploymentMapper;

    @Inject
    private DeploymentDescriptorDao deploymentDescriptorDao;

    @Inject
    private FileTypeMapper fileTypeMapper;

    private TaskDefinitionEntity findTaskDefinition(String taskDefinitionId) {
        return taskDefinitionService.getTaskDefinition(taskDefinitionId)
                .orElseThrow(() -> new NotFoundException("Task definition not found: " + taskDefinitionId));
    }

    @Override
    public Response createTaskDefinition(TaskDefinitionDto taskDefinitionDto) {
        if (taskDefinitionDto.getId() != null) {
            return RestUtils.badRequest("ID must not be set");
        }

        if (taskDefinitionDto.getName() == null) {
            return RestUtils.badRequest("Name must not be null");
        }

        TaskDefinitionEntity definition = taskDefinitionService.newTaskDefinition(taskDefinitionDto.getName());
        return RestUtils.created(resolver.of(TaskDefinitionsApi::getTaskDefinition, definition.getId()));
    }

    @Override
    public Response getTaskDefinition(String taskDefinitionId) {
        TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
        return Response.ok(taskDefinitionMapper.toRestEntity(definition)).build();
    }

    @Override
    public Response getTaskDefinitionConfiguration(String taskDefinitionId) {
        TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
        return Response.ok(deploymentConfigMapper.toRestEntity(definition)).build();
    }

    @Override
    public Response getTaskDefinitionDeployments(String taskDefinitionId) {
        List<DeploymentDescriptorEntity> deployments = deploymentDescriptorDao.findByDefinitionId(taskDefinitionId);
        return Response.ok(new DeploymentDescriptorsDto()
                .from(0)
                .total(deployments.size())
                .items(deploymentMapper.toRestEntities(deployments))).build();
    }

    @Override
    public Response getTaskDefinitionWorkspaceFile(String taskDefinitionId, String path) {
        try {
            TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
            FileDescription description = taskDefinitionService.getWorkspace(definition)
                    .readFileDescription(path)
                    .orElseThrow(NotFoundException::new);
            return Response.ok(fileDescriptionMapper.toApiEntity(description)).build();
        } catch (NoSuchFileException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }
    }

    @Override
    public Response deleteTaskDefinitionWorkspaceFile(String taskDefinitionId, String path) {
        try {
            TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
            taskDefinitionService.getWorkspace(definition)
                    .deleteFile(path);
            return Response.ok().build();
        } catch (NoSuchFileException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }
    }

    @Override
    public Response getTaskDefinitionWorkspaceFileContent(String taskDefinitionId, String path) {
        try {
            TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
            Workspace ws = taskDefinitionService.getWorkspace(definition);
            if (ws.readFileDescription(path).orElseThrow(NotFoundException::new).getType() == FileDescription.FileType.DIRECTORY) {
                return RestUtils.unprocessableEntity("File is a directory");
            }

            InputStream file = ws.openFile(path)
                    .orElseThrow(NotFoundException::new);
            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).build();
        } catch (NoSuchFileException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }
    }

    @Override
    public Response getTaskDefinitions(Integer from, Integer limit) {
        List<TaskDefinitionEntity> definitions = taskDefinitionService.getTaskDefinitions(from, limit);

        TaskDefinitionsDto taskDefinitionsDto = new TaskDefinitionsDto();
        taskDefinitionsDto.from(from);
        taskDefinitionsDto.items(taskDefinitionMapper.toRestEntities(definitions));
        taskDefinitionsDto.total(Math.toIntExact(taskDefinitionService.countAllTaskDefinitions()));
        return Response.ok(taskDefinitionsDto).build();
    }

    @Override
    public Response setTaskDefinitionConfiguration(String taskDefinitionId, DeploymentConfigDto config) {
        RestUtils.checkNotNull(config, "No body");
        RestUtils.checkNotNull(taskDefinitionId, "No task definition");

        TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
        deploymentConfigMapper.updateModelEntity(config, definition);
        return Response.noContent().build();
    }

    @Override
    public Response setTaskDefinitionWorkspaceFile(String taskDefinitionId, String path, FileDto file) {
        FileDescription.FileType fileType = fileTypeMapper.toModelEntity(file.getType());
        Optional<Set<PosixFilePermission>> filePermissions = Optional.ofNullable(file.getPermissions())
                .map(PosixFilePermissions::fromString);
        if (file.getChildren() != null && !file.getChildren().isEmpty()) {
            return RestUtils.badRequest("Cannot change children");
        }

        TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
        Workspace workspace = taskDefinitionService.getWorkspace(definition);
        try {
            Optional<FileDescription> desc = workspace.readFileDescription(path);

            if (!desc.isPresent()) {
                workspace.createFile(path, FileDescription.builder()
                        .type(fileType)
                        .permissions(filePermissions.orElse(null))
                        .build());
            } else {
                if (fileType != desc.get().getType()) {
                    return RestUtils.badRequest("Cannot change file type");
                }

                if (filePermissions.isPresent()) {
                    workspace.setPermissions(path, filePermissions.get());
                }
            }
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }

        return Response.noContent().build();
    }

    @Override
    public Response setTaskDefinitionWorkspaceFileContent(String taskDefinitionId, String path, File body) {
        try (InputStream content = new FileInputStream(body)) {
            TaskDefinitionEntity definition = findTaskDefinition(taskDefinitionId);
            taskDefinitionService.getWorkspace(definition).saveFile(path, content);
        } catch (NoSuchFileException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }

        return Response.noContent().build();
    }
}
