package pl.edu.agh.xdcs.restapi.impl;

import pl.edu.agh.xdcs.restapi.AgentsApi;
import pl.edu.agh.xdcs.restapi.model.AgentEntryDto;
import pl.edu.agh.xdcs.restapi.model.AgentInfoDto;
import pl.edu.agh.xdcs.security.web.UserContext;
import pl.edu.agh.xdcs.util.UriResolver;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;

/**
 * @author Kamil Jarosz
 */
public class AgentsApiImpl implements AgentsApi {
    @Context
    private UriInfo uriInfo;

    @Inject
    private UriResolver resolver;

    @Inject
    private UserContext userContext;

    @Override
    public Response getAgentInfo(String agentId) {
        return Response.ok(new AgentInfoDto()
                .displayName("asdf")
                .id(agentId)
                .href(uriInfo.getPath())).build();
    }

    @Override
    public Response getAgentList() {
        return Response.ok(Arrays.asList(
                new AgentEntryDto()
                        .displayName("asdf " + userContext.getUsername())
                        .id("1234")
                        .href(resolver.of(AgentsApi::getAgentInfo, "1234"))
        )).build();
    }
}
