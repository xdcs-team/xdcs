package pl.edu.agh.xdcs.grpc.security;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.api.AgentSecurityGrpc;
import pl.edu.agh.xdcs.api.AgentTokenGrant;
import pl.edu.agh.xdcs.api.SecurityInformation;
import pl.edu.agh.xdcs.api.ServerCertificate;
import pl.edu.agh.xdcs.grpc.GrpcServer;
import pl.edu.agh.xdcs.grpc.events.GrpcSessionCreatedEvent;
import pl.edu.agh.xdcs.security.TokenIssuer;

import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kamil Jarosz
 */
public class AgentSecurityInformationProvider {
    @Inject
    private Logger logger;

    @Inject
    private TokenIssuer tokenIssuer;

    @Inject
    private GrpcServer server;

    public void sendSecurityInformation(@ObservesAsync GrpcSessionCreatedEvent event) {
        ManagedChannel channel = event.getSession().getChannel();
        AgentSecurityGrpc.AgentSecurityBlockingStub agentSecurity = AgentSecurityGrpc.newBlockingStub(channel);

        String sessionId = event.getSession().getSessionId();

        Map<String, Object> claims = new HashMap<>();
        claims.put("session", sessionId);
        String token = tokenIssuer.issueToken(
                event.getSession().getAgentName(),
                null,
                TokenIssuer.TokenType.AGENT,
                claims);
        AgentTokenGrant tokenGrant = AgentTokenGrant.newBuilder()
                .setToken(token)
                .build();

        ServerCertificate certificate = createServerCertificate();

        logger.debug("Sending security info to " + event.getSession().getAgentName());
        agentSecurity.acceptSecurityInformation(SecurityInformation.newBuilder()
                .setTokenGrant(tokenGrant)
                .setServerCertificate(certificate)
                .build());
        logger.debug("Security info sent successfully to " + event.getSession().getAgentName());
    }

    private ServerCertificate createServerCertificate() {
        try {
            return ServerCertificate.newBuilder()
                    .setBytes(ByteString.readFrom(server.getCertificate()))
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
