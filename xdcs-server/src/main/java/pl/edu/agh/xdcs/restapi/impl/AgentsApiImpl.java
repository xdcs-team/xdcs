package pl.edu.agh.xdcs.restapi.impl;

import org.apache.commons.codec.digest.Crypt;
import pl.edu.agh.xdcs.restapi.AgentsApi;
import pl.edu.agh.xdcs.restapi.model.AgentEntryDto;
import pl.edu.agh.xdcs.restapi.model.AgentInfoDto;
import pl.edu.agh.xdcs.security.UserContext;
import pl.edu.agh.xdcs.util.UriResolver;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

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
    public AgentInfoDto getAgentInfo(String agentId) {
        return new AgentInfoDto()
                .displayName("asdf")
                .id(agentId)
                .href(uriInfo.getPath());
    }

    @Override
    public List<AgentEntryDto> getAgentList() {
        return Arrays.asList(
                new AgentEntryDto()
                        .displayName("asdf " + userContext.getUsername())
                        .id("1234")
                        .href(resolver.of(AgentsApi::getAgentInfo, "1234"))
        );
    }
}
