package pl.edu.agh.xdcs.ssh;

import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.SessionFactory;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.config.Configuration;
import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.KeyPathConfiguration;
import pl.edu.agh.xdcs.config.util.ReferencedFileLoader;
import pl.edu.agh.xdcs.grpc.events.AgentConnectedEvent;
import pl.edu.agh.xdcs.grpc.events.AgentDisconnectedEvent;
import pl.edu.agh.xdcs.ssh.configurators.GrpcSshConfigurator;
import pl.edu.agh.xdcs.util.Eager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
@Eager
@ApplicationScoped
public class XdcsSshServer {
    private final int port = Integer.parseInt(System.getProperty("xdcs.agent.port.ssh", "8082"));

    @Inject
    private Logger logger;

    @Inject
    @Configured
    private Configuration config;

    @Inject
    private ReferencedFileLoader fileLoader;

    @Resource
    private ManagedScheduledExecutorService scheduledExecutorService;

    @Inject
    private Event<AgentConnectedEvent> agentConnectedEvent;

    @Inject
    private Event<AgentDisconnectedEvent> agentDisconnectedEvent;

    @Inject
    private Instance<GrpcSshConfigurator> configurators;

    private SshServer server;

    @PostConstruct
    public void init() {
        logger.info("Initializing SSH server on port " + port);

        server = SshServer.setUpDefaultServer();
        server.getProperties().put(SshServer.IDLE_TIMEOUT, 0);
        server.getProperties().put(SshServer.NIO2_READ_TIMEOUT, 0);
        server.setPort(port);
        server.setHost(config.getBindHost());
        server.setScheduledExecutorService(scheduledExecutorService);
        setUpKeys();
        configurators.forEach(configurator -> configurator.configure(server));
        server.setSessionFactory(new SessionFactory(server));
        server.setForwardingFilter(new GrpcSshForwardingFilter());
        server.addSessionListener(new SessionListener() {
            @Override
            public void sessionClosed(Session session) {
                InetAddress agentAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress();
                String agentName = session.getUsername();
                logger.info("Agent '" + agentName + "' disconnected from " + agentAddress);
                agentDisconnectedEvent.fire(AgentDisconnectedEvent.builder()
                        .agentName(agentName)
                        .agentAddress(agentAddress)
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

                String agentName = session.getUsername();
                InetAddress agentAddress = ((InetSocketAddress) session.getRemoteAddress()).getAddress();

                logger.info("Agent '" + agentName + "' connected from " + agentAddress);
                agentConnectedEvent.fire(AgentConnectedEvent.builder()
                        .agentAddress(agentAddress)
                        .agentName(agentName)
                        .tunnelEndpoint(boundAddress.toInetSocketAddress())
                        .build());
            }
        });

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start SSH server", e);
        }
    }

    private void setUpKeys() {
        Path keyPath = Optional.ofNullable(config.getAgentSecurity().getSshKey())
                .map(KeyPathConfiguration::getPath)
                .map(fileLoader::toPath)
                .orElse(null);
        if (keyPath != null) {
            server.setKeyPairProvider(new FileKeyPairProvider(keyPath));
        } else {
            server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down SSH server");
        try {
            server.stop(true);
        } catch (IOException e) {
            String message = "IO error occurred when stopping SSH server";
            logger.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
