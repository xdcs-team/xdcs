package pl.edu.agh.xdcs.grpc.context;

import pl.edu.agh.xdcs.grpc.ee.ManagedGrpcSession;
import pl.edu.agh.xdcs.grpc.scope.SessionScoped;

import javax.enterprise.inject.Alternative;

/**
 * @author Kamil Jarosz
 */
@Alternative
public class SessionContext extends StackableKeyedContext<ManagedGrpcSession> {
    public SessionContext() {
        super(SessionScoped.class, false);
    }
}
