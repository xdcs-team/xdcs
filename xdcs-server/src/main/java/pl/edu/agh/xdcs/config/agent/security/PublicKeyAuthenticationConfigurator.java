package pl.edu.agh.xdcs.config.agent.security;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import pl.edu.agh.xdcs.config.AgentSecurityConfiguration;
import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.SecurityPublicKeyPolicy;
import pl.edu.agh.xdcs.config.util.ReferencedFileLoader;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.AnyPublicKeyAuthenticator;
import pl.edu.agh.xdcs.grpc.ssh.configurators.GrpcSshConfigurator;
import pl.edu.agh.xdcs.util.ObjectMatcher;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * @author Kamil Jarosz
 */
public class PublicKeyAuthenticationConfigurator implements GrpcSshConfigurator {
    private ObjectMatcher<PublickeyAuthenticator> publicKeyDirectiveMatcher = ObjectMatcher.<PublickeyAuthenticator>newMatcher()
            .match(SecurityPublicKeyPolicy.File.class, this::parseDirective)
            .match(SecurityPublicKeyPolicy.AllowAll.class, this::parseDirective)
            .other(directive -> {
                throw new RuntimeException("Unknown directive: " + directive);
            })
            .build();

    @Inject
    private ReferencedFileLoader fileLoader;

    @Inject
    @Configured
    private AgentSecurityConfiguration agentSecurityConfiguration;

    private PublickeyAuthenticator parseDirective(SecurityPublicKeyPolicy.AllowAll directive) {
        return AcceptAllPublickeyAuthenticator.INSTANCE;
    }

    private PublickeyAuthenticator parseDirective(SecurityPublicKeyPolicy.File directive) {
        Path path = fileLoader.toPath(directive.getPath());

        if (directive.isRequired() && !Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new RuntimeException("File does not exist: " + path);
        }

        return new AuthorizedKeysAuthenticator(path);
    }

    @Override
    public void configure(SshServer server) {
        SecurityPublicKeyPolicy publicKeyPolicy =
                agentSecurityConfiguration.getPoliciesConfig().getPublicKeyPolicy();
        if (publicKeyPolicy == null) {
            // pubkey authentication is disabled
            return;
        }

        PublickeyAuthenticator[] authenticators = publicKeyPolicy.getDirectives()
                .stream()
                .map(publicKeyDirectiveMatcher::match)
                .toArray(PublickeyAuthenticator[]::new);
        server.setPublickeyAuthenticator(new AnyPublicKeyAuthenticator(authenticators));
    }
}
