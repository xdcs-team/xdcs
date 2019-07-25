package pl.edu.agh.xdcs.grpc.context;

import io.grpc.ServerCall;
import pl.edu.agh.xdcs.grpc.scope.RendezvousScoped;

import javax.enterprise.inject.Alternative;

/**
 * @author Kamil Jarosz
 */
@Alternative
public class RendezvousContext extends StackableKeyedContext<ServerCall> {
    public RendezvousContext() {
        super(RendezvousScoped.class, false);
    }
}
