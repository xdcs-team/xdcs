package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.agents.AgentManager;
import pl.edu.agh.xdcs.api.DeploymentId;
import pl.edu.agh.xdcs.api.TaskRunnerGrpc.TaskRunnerBlockingStub;
import pl.edu.agh.xdcs.grpc.session.GrpcSessionManager;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * @author Kamil Jarosz
 */
@Path("/tmp")
public class TemporaryApi {
    @Inject
    private AgentManager agentManager;

    @Inject
    private GrpcSessionManager sessionManager;

    @POST
    @Path("run-deployment/{deploymentId}")
    @Produces({"application/json"})
    public void runDeployment(@PathParam("deploymentId") String deploymentId) {
        Agent agent = agentManager.getAllAgents().iterator().next();
        TaskRunnerBlockingStub taskRunner = sessionManager.getStubProducer(agent)
                .getTaskRunnerBlockingStub();
        taskRunner.submit(DeploymentId.newBuilder()
                .setDeploymentId(deploymentId)
                .build());
    }
}
