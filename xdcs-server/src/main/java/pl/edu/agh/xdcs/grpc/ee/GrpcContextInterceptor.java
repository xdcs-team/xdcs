package pl.edu.agh.xdcs.grpc.ee;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.grpc.DefinedHeaders;
import pl.edu.agh.xdcs.grpc.context.AgentContext;
import pl.edu.agh.xdcs.grpc.context.RendezvousContext;
import pl.edu.agh.xdcs.grpc.context.SessionContext;
import pl.edu.agh.xdcs.grpc.session.GrpcSession;
import pl.edu.agh.xdcs.grpc.session.GrpcSessionManager;
import pl.edu.agh.xdcs.security.Token;

import javax.inject.Inject;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@link ServerInterceptor} which provides GRPC-related contexts to
 * GRPC services.
 *
 * @author Kamil Jarosz
 */
public class GrpcContextInterceptor implements ServerInterceptor {
    @Inject
    private GrpcSessionManager sessionManager;

    @Inject
    private AgentContext agentContext;

    @Inject
    private SessionContext sessionContext;

    @Inject
    private RendezvousContext rendezvousContext;

    @Inject
    private DefinedHeaders definedHeaders;

    @Inject
    private Logger logger;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        GrpcSession session = authorizeAgent(call, headers);
        return runWithContext(call, session, () -> {
            ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
            return new ServerCall.Listener<ReqT>() {
                @Override
                public void onMessage(ReqT message) {
                    runWithContext(call, session, () -> delegate.onMessage(message));
                }

                @Override
                public void onHalfClose() {
                    runWithContext(call, session, delegate::onHalfClose);
                }

                @Override
                public void onCancel() {
                    runWithContext(call, session, delegate::onCancel);
                    rendezvousContext.evict(call);
                }

                @Override
                public void onComplete() {
                    runWithContext(call, session, delegate::onComplete);
                    rendezvousContext.evict(call);
                }

                @Override
                public void onReady() {
                    runWithContext(call, session, delegate::onReady);
                }
            };
        });
    }

    private void runWithContext(ServerCall call, GrpcSession session, Runnable r) {
        runWithContext(call, session, () -> {
            r.run();
            return null;
        });
    }

    private <R> R runWithContext(ServerCall call, GrpcSession session, Supplier<R> r) {
        SocketAddress clientAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);

        agentContext.enter(clientAddress);
        sessionContext.enter(session);
        rendezvousContext.enter(call);
        try {
            return r.get();
        } finally {
            rendezvousContext.exit();
            sessionContext.exit();
            agentContext.exit();
        }
    }

    private GrpcSession authorizeAgent(ServerCall call, Metadata headers) {
        Token token = headers.get(definedHeaders.authorization());
        if (token == null) {
            abortCall(call, "Agent tried to connect but no valid token was provided");
            return null;
        }

        GrpcSession session = sessionManager.getSession(token.getSubject())
                .orElseThrow(() -> new RuntimeException("Client has not started a session yet"));
        String sessionId = token.getClaim("session", String.class);
        if (!Objects.equals(sessionId, session.getSessionId())) {
            abortCall(call, "Agent tried to connect but token was meant for a different session");
            return null;
        }

        return session;
    }

    private void abortCall(ServerCall call, String message) {
        try {
            call.close(Status.UNAUTHENTICATED, new Metadata());
            logger.warn(message);
        } catch (IllegalStateException ignored) {

        }
    }
}
