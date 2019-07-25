package pl.edu.agh.xdcs.grpc.context;

import pl.edu.agh.xdcs.grpc.scope.AgentScoped;

import javax.enterprise.inject.Alternative;
import java.net.SocketAddress;

/**
 * @author Kamil Jarosz
 */
@Alternative
public class AgentContext extends StackableKeyedContext<SocketAddress> {
    public AgentContext() {
        super(AgentScoped.class, false);
    }
}
