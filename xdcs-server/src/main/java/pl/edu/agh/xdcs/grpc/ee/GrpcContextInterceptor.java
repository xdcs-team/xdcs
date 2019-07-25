package pl.edu.agh.xdcs.grpc.ee;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import pl.edu.agh.xdcs.grpc.context.AgentContext;
import pl.edu.agh.xdcs.grpc.context.RendezvousContext;
import pl.edu.agh.xdcs.grpc.context.SessionContext;

import javax.inject.Inject;
import java.net.SocketAddress;

/**
 * A {@link ServerInterceptor} which provides GRPC-related contexts to
 * GRPC services.
 *
 * @author Kamil Jarosz
 */
public class GrpcContextInterceptor implements ServerInterceptor {
    @Inject
    private SessionManager sessionManager;

    @Inject
    private AgentContext agentContext;

    @Inject
    private SessionContext sessionContext;

    @Inject
    private RendezvousContext rendezvousContext;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
        return new ServerCall.Listener<ReqT>() {
            @Override
            public void onMessage(ReqT message) {
                intercept(call, () -> delegate.onMessage(message));
            }

            @Override
            public void onHalfClose() {
                intercept(call, delegate::onHalfClose);
            }

            @Override
            public void onCancel() {
                intercept(call, delegate::onCancel);
                rendezvousContext.evict(call);
            }

            @Override
            public void onComplete() {
                intercept(call, delegate::onComplete);
                rendezvousContext.evict(call);
            }

            @Override
            public void onReady() {
                intercept(call, delegate::onReady);
            }
        };
    }

    private void intercept(ServerCall call, Runnable r) {
        SocketAddress clientAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        ManagedGrpcSession session = sessionManager.getSession(clientAddress)
                .orElseThrow(() -> new RuntimeException("Client has not started a session yet"));

        agentContext.enter(clientAddress);
        sessionContext.enter(session);
        rendezvousContext.enter(call);
        try {
            r.run();
        } finally {
            rendezvousContext.exit();
            sessionContext.exit();
            agentContext.exit();
        }
    }
}
