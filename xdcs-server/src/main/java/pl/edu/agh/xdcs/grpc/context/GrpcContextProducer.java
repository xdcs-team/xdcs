package pl.edu.agh.xdcs.grpc.context;

import pl.edu.agh.xdcs.grpc.scope.AgentScoped;
import pl.edu.agh.xdcs.grpc.scope.RendezvousScoped;
import pl.edu.agh.xdcs.grpc.scope.SessionScoped;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @author Kamil Jarosz
 */
public class GrpcContextProducer {
    @Inject
    private BeanManager beanManager;

    @Produces
    public AgentContext getAgentContext() {
        return (AgentContext) beanManager.getContext(AgentScoped.class);
    }

    @Produces
    public SessionContext getSessionContext() {
        return (SessionContext) beanManager.getContext(SessionScoped.class);
    }

    @Produces
    public RendezvousContext getRendezvousContext() {
        return (RendezvousContext) beanManager.getContext(RendezvousScoped.class);
    }
}
