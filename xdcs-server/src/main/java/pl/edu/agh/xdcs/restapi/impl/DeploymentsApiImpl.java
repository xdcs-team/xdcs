package pl.edu.agh.xdcs.restapi.impl;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.db.entity.TaskDefinitionEntity;
import pl.edu.agh.xdcs.or.types.Deployment;
import pl.edu.agh.xdcs.restapi.DeploymentsApi;
import pl.edu.agh.xdcs.restapi.mapper.impl.DeploymentMapper;
import pl.edu.agh.xdcs.restapi.mapper.impl.FileDescriptionMapper;
import pl.edu.agh.xdcs.restapi.model.DeploymentDto;
import pl.edu.agh.xdcs.restapi.model.DeploymentRequestDto;
import pl.edu.agh.xdcs.restapi.model.FileDto;
import pl.edu.agh.xdcs.restapi.util.RestUtils;
import pl.edu.agh.xdcs.services.DeploymentFailedException;
import pl.edu.agh.xdcs.services.DeploymentService;
import pl.edu.agh.xdcs.services.TaskDefinitionService;
import pl.edu.agh.xdcs.util.UriResolver;
import pl.edu.agh.xdcs.workspace.FileDescription;
import pl.edu.agh.xdcs.workspace.Workspace;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class DeploymentsApiImpl implements DeploymentsApi {
    @Inject
    private Logger logger;

    @Inject
    private UriResolver resolver;

    @Inject
    private DeploymentService deploymentService;

    @Inject
    private TaskDefinitionService definitionService;

    @Inject
    private FileDescriptionMapper fileDescriptionMapper;

    @Inject
    private DeploymentMapper deploymentMapper;

    @Override
    public Response getDeployment(String deploymentId) {
        Deployment deployment = deploymentService.getDeployment(deploymentId);
        DeploymentDto deploymentDto = deploymentMapper.toRestEntity(deployment);
        return Response.ok(deploymentDto).build();
    }

    @Override
    public Response deployTaskDefinition(DeploymentRequestDto deploymentRequest) {
        TaskDefinitionEntity definition = definitionService.getTaskDefinition(deploymentRequest.getFrom())
                .orElseThrow(NotFoundException::new);
        try {
            String deploymentId = deploymentService.deploy(definition, deploymentRequest.getDescription());
            return RestUtils.created(resolver.of(DeploymentsApi::getDeployment, deploymentId));
        } catch (DeploymentFailedException e) {
            return RestUtils.badRequest(e.getMessage());
        }
    }

    @Override
    public Response getDeploymentFile(String deploymentId, String path) {
        try {
            Workspace workspace = deploymentService.getWorkspaceForDeployment(deploymentId);
            FileDescription desc = workspace.readFileDescription(path).orElseThrow(NotFoundException::new);
            FileDto file = fileDescriptionMapper.toRestEntity(desc);
            return Response.ok(file).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }
    }

    @Override
    public Response getDeploymentFileContent(String deploymentId, String path) {
        try {
            Workspace workspace = deploymentService.getWorkspaceForDeployment(deploymentId);
            InputStream file = workspace.openFile(path).orElseThrow(NotFoundException::new);
            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).build();
        } catch (IOException e) {
            return RestUtils.serverError(e);
        }
    }
}
