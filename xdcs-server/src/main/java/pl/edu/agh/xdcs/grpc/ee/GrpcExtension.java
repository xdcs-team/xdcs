package pl.edu.agh.xdcs.grpc.ee;

import pl.edu.agh.xdcs.grpc.context.AgentContext;
import pl.edu.agh.xdcs.grpc.context.RendezvousContext;
import pl.edu.agh.xdcs.grpc.context.SessionContext;
import pl.edu.agh.xdcs.grpc.scope.AgentScoped;
import pl.edu.agh.xdcs.grpc.scope.RendezvousScoped;
import pl.edu.agh.xdcs.grpc.scope.SessionScoped;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * @author Kamil Jarosz
 */
public class GrpcExtension implements Extension {
    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        bbd.addScope(AgentScoped.class, true, false);
        bbd.addScope(SessionScoped.class, true, false);
        bbd.addScope(RendezvousScoped.class, true, false);
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        abd.addContext(new AgentContext());
        abd.addContext(new SessionContext());
        abd.addContext(new RendezvousContext());
    }
}
