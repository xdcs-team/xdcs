package pl.edu.agh.xdcs.grpc.ssh;

import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.SessionFactory;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentDisconnectedEvent;
import pl.edu.agh.xdcs.util.Eager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;

/**
 * @author Kamil Jarosz
 */
@Eager
@ApplicationScoped
public class GrpcSshServer {
    @Inject
    private Logger logger;

    @Resource
    private ManagedScheduledExecutorService scheduledExecutorService;

    @Inject
    private Event<AgentConnectedEvent> agentConnectedEvent;

    @Inject
    private Event<AgentDisconnectedEvent> agentDisconnectedEvent;

    private SshServer server;

    private int port = Integer.parseInt(System.getProperty("xdcs.agent.port.ssh", "8082"));

    @PostConstruct
    public void init() {
        logger.info("Initializing GRPC server on port " + port);

        server = SshServer.setUpDefaultServer();
        server.setPort(port);
        server.setScheduledExecutorService(scheduledExecutorService);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("serverkey")));
        server.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
        server.setSessionFactory(new SessionFactory(server));
        server.setForwardingFilter(new GrpcSshForwardingFilter());
        server.addSessionListener(new SessionListener() {
            @Override
            public void sessionClosed(Session session) {
                agentDisconnectedEvent.fire(AgentDisconnectedEvent.builder()
                        .agentAddress(((InetSocketAddress) session.getRemoteAddress()).getAddress())
                        .build());
            }
        });

        server.addPortForwardingEventListener(new PortForwardingEventListener() {
            @Override
            public void establishedExplicitTunnel(
                    Session session,
                    SshdSocketAddress local,
                    SshdSocketAddress remote,
                    boolean localForwarding,
                    SshdSocketAddress boundAddress,
                    Throwable reason) {
                if (reason != null) return;

                agentConnectedEvent.fire(AgentConnectedEvent.builder()
                        .agentAddress(((InetSocketAddress) session.getRemoteAddress()).getAddress())
                        .tunnelEndpoint(boundAddress.toInetSocketAddress())
                        .build());
            }
        });

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start GRPC server", e);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        logger.info("Shutting down GRPC server");
        server.stop(true);
    }
}
