package pl.edu.agh.xdcs.grpc.session;

import pl.edu.agh.xdcs.agents.Agent;
import pl.edu.agh.xdcs.agents.AgentManager;
import pl.edu.agh.xdcs.grpc.context.SessionContext;
import pl.edu.agh.xdcs.grpc.scope.SessionScoped;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
@SessionScoped
public class GrpcSessionProducer {
    @Inject
    private BeanManager beanManager;

    @Produces
    public GrpcSession getCurrentSession() {
        SessionContext context = (SessionContext) beanManager.getContext(SessionScoped.class);
        return context.getCurrentKey()
                .orElseThrow(Error::new);
    }

    @Produces
    public Agent getCurrentAgent(AgentManager agentManager) {
        return agentManager.getAgent(getCurrentSession().getAgentName())
                .orElseThrow(Error::new);
    }
}
