package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.agents.AgentManager;
import pl.edu.agh.xdcs.db.dao.AgentDao;
import pl.edu.agh.xdcs.db.entity.AgentEntity;
import pl.edu.agh.xdcs.restapi.NodesApi;
import pl.edu.agh.xdcs.restapi.mapper.AgentDetailsMapper;
import pl.edu.agh.xdcs.restapi.mapper.NodeMapper;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
@Transactional
public class NodesApiImpl implements NodesApi {
    @Inject
    private AgentManager agentManager;

    @Inject
    private AgentDao agentDao;

    @Inject
    private NodeMapper nodeMapper;

    @Inject
    private AgentDetailsMapper agentDetailsMapper;

    @Override
    public Response getNodes() {
        List<AgentEntity> agents = agentDao.getAllAgents();
        return Response.ok(nodeMapper.toNodes(agents)).build();
    }

    @Override
    public Response getNode(String nodeId) {
        AgentEntity agent = agentDao.findByName(nodeId)
                .orElseThrow(NotFoundException::new);
        return Response.ok(nodeMapper.toNode(agent)).build();
    }

    @Override
    public Response getNodeDetails(String nodeId) {
        Agent agent = agentManager.getAgent(nodeId)
                .orElseThrow(NotFoundException::new);
        return Response.ok(agentDetailsMapper.toRestEntity(agent)).build();
    }
}
