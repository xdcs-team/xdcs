package pl.edu.agh.xdcs.config.agent.security;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import pl.edu.agh.xdcs.config.AgentSecurityConfiguration;
import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.PasswordFileFormat;
import pl.edu.agh.xdcs.config.SecurityPasswordPolicy;
import pl.edu.agh.xdcs.config.util.ReferencedFileLoader;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.AnyPasswordAuthenticator;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.BarePasswordAuthenticator;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.ShadowFilePasswordAuthenticator;
import pl.edu.agh.xdcs.grpc.ssh.configurators.GrpcSshConfigurator;
import pl.edu.agh.xdcs.util.ObjectMatcher;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Kamil Jarosz
 */
public class PasswordAuthenticationConfigurator implements GrpcSshConfigurator {
    @Inject
    private ReferencedFileLoader fileLoader;

    private final ObjectMatcher<PasswordAuthenticator> passwordDirectiveMatcher = ObjectMatcher.<PasswordAuthenticator>newMatcher()
            .match(SecurityPasswordPolicy.InlinePass.class, this::parseDirective)
            .match(SecurityPasswordPolicy.AllowAll.class, this::parseDirective)
            .match(SecurityPasswordPolicy.File.class, this::parseDirective)
            .other(directive -> {
                throw new RuntimeException("Unknown directive: " + directive);
            })
            .build();

    @Inject
    @Configured
    private AgentSecurityConfiguration agentSecurityConfiguration;

    private PasswordAuthenticator parseDirective(SecurityPasswordPolicy.File directive) {
        if (directive.getFormat() == PasswordFileFormat.SHADOW) {
            try (InputStream is = fileLoader.loadFile(directive.getPath())) {
                return new ShadowFilePasswordAuthenticator(is);
            } catch (IOException e) {
                throw new RuntimeException("IO error while reading shadow file", e);
            }
        }

        throw new RuntimeException("Unknown password file format: " + directive.getFormat());
    }

    private AcceptAllPasswordAuthenticator parseDirective(SecurityPasswordPolicy.AllowAll directive) {
        return AcceptAllPasswordAuthenticator.INSTANCE;
    }

    private BarePasswordAuthenticator parseDirective(SecurityPasswordPolicy.InlinePass directive) {
        return new BarePasswordAuthenticator(
                directive.getUsername(),
                directive.getPassword().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void configure(SshServer server) {
        SecurityPasswordPolicy passwordPolicy =
                agentSecurityConfiguration.getPoliciesConfig().getPasswordPolicy();
        if (passwordPolicy == null) {
            // password authentication is disabled
            return;
        }

        PasswordAuthenticator[] authenticators = passwordPolicy.getDirectives()
                .stream()
                .map(passwordDirectiveMatcher::match)
                .toArray(PasswordAuthenticator[]::new);
        server.setPasswordAuthenticator(new AnyPasswordAuthenticator(authenticators));
    }
}
