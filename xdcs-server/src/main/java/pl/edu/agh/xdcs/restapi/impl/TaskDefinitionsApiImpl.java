package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.restapi.TaskDefinitionsApi;
import pl.edu.agh.xdcs.restapi.mapper.impl.FileDescriptionMapper;
import pl.edu.agh.xdcs.restapi.mapper.impl.TaskDefinitionConfigMapper;
import pl.edu.agh.xdcs.restapi.mapper.impl.TaskDefinitionMapper;
import pl.edu.agh.xdcs.restapi.model.FileDto;
import pl.edu.agh.xdcs.restapi.model.TaskDefinitionConfigDto;
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
import java.util.List;

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
    private TaskDefinitionConfigMapper taskDefinitionConfigMapper;

    @Inject
    private FileDescriptionMapper fileDescriptionMapper;

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
        TaskDefinitionEntity definition = taskDefinitionService.getTaskDefinition(taskDefinitionId)
                .orElseThrow(NotFoundException::new);
        return Response.ok(taskDefinitionMapper.toRestEntity(definition)).build();
    }

    @Override
    public Response getTaskDefinitionConfiguration(String taskDefinitionId) {
        TaskDefinitionEntity definition = taskDefinitionService.getTaskDefinition(taskDefinitionId)
                .orElseThrow(NotFoundException::new);
        return Response.ok(taskDefinitionConfigMapper.toRestEntity(definition)).build();
    }

    @Override
    public Response getTaskDefinitionWorkspaceFile(String taskDefinitionId, String path) {
        try {
            TaskDefinitionEntity definition = taskDefinitionService.getTaskDefinition(taskDefinitionId)
                    .orElseThrow(NotFoundException::new);
            FileDescription description = taskDefinitionService.getWorkspace(definition)
                    .readFileDescription(path)
                    .orElseThrow(NotFoundException::new);
            return Response.ok(fileDescriptionMapper.toRestEntity(description)).build();
        } catch (NoSuchFileException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }
    }

    @Override
    public Response deleteTaskDefinitionWorkspaceFile(String taskDefinitionId, String path) {
        try {
            TaskDefinitionEntity definition = taskDefinitionService.getTaskDefinition(taskDefinitionId)
                    .orElseThrow(NotFoundException::new);
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
            TaskDefinitionEntity definition = taskDefinitionService.getTaskDefinition(taskDefinitionId)
                    .orElseThrow(NotFoundException::new);
            Workspace ws = taskDefinitionService.getWorkspace(definition);
            if (ws.readFileDescription(path).orElseThrow(NotFoundException::new).getType() == FileDescription.FileType.DIRECTORY) {
                return Response.status(422).build();
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
    public Response setTaskDefinitionConfiguration(String taskDefinitionId, TaskDefinitionConfigDto config) {
        TaskDefinitionEntity definition = taskDefinitionService.getTaskDefinition(taskDefinitionId)
                .orElseThrow(NotFoundException::new);
        taskDefinitionConfigMapper.updateModelEntity(config, definition);
        return Response.noContent().build();
    }

    @Override
    public Response setTaskDefinitionWorkspaceFile(String taskDefinitionId, String path, FileDto file) {
        throw new RuntimeException();
    }

    @Override
    public Response setTaskDefinitionWorkspaceFileContent(String taskDefinitionId, String path, File body) {
        try (InputStream content = new FileInputStream(body)) {
            TaskDefinitionEntity definition = taskDefinitionService.getTaskDefinition(taskDefinitionId)
                    .orElseThrow(NotFoundException::new);
            taskDefinitionService.getWorkspace(definition).saveFile(path, content);
        } catch (NoSuchFileException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }

        return Response.noContent().build();
    }
}
