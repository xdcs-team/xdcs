package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.agents.AgentManager;
import pl.edu.agh.xdcs.restapi.NodesApi;
import pl.edu.agh.xdcs.restapi.mapper.AgentDetailsMapper;
import pl.edu.agh.xdcs.restapi.mapper.NodeMapper;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class NodesApiImpl implements NodesApi {
    @Inject
    private AgentManager agentManager;

    @Inject
    private NodeMapper nodeMapper;

    @Inject
    private AgentDetailsMapper agentDetailsMapper;

    @Override
    public Response getNodes() {
        Collection<Agent> agents = agentManager.getAllAgents();
        return Response.ok(nodeMapper.toNodes(agents)).build();
    }

    @Override
    public Response getNode(String nodeId) {
        Agent agent = getAgent(nodeId);
        return Response.ok(nodeMapper.toNode(agent)).build();
    }

    @Override
    public Response getNodeDetails(String nodeId) {
        Agent agent = getAgent(nodeId);
        return Response.ok(agentDetailsMapper.toRestEntity(agent)).build();
    }

    private Agent getAgent(String nodeId) {
        return agentManager.getAgent(nodeId).orElseThrow(NotFoundException::new);
    }
}
