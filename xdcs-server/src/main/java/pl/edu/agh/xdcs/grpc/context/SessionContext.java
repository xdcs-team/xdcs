package pl.edu.agh.xdcs.grpc.context;

import pl.edu.agh.xdcs.grpc.session.GrpcSession;
import pl.edu.agh.xdcs.grpc.scope.GrpcSessionScoped;

import javax.enterprise.inject.Alternative;

/**
 * @author Kamil Jarosz
 */
@Alternative
public class SessionContext extends StackableKeyedContext<GrpcSession> {
    public SessionContext() {
        super(GrpcSessionScoped.class, false);
    }
}
