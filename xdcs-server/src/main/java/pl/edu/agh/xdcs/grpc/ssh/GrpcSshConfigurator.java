package pl.edu.agh.xdcs.grpc.ssh;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import pl.edu.agh.xdcs.config.AgentSecurityConfiguration;
import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.PasswordFileFormat;
import pl.edu.agh.xdcs.config.SecurityPasswordPolicy;
import pl.edu.agh.xdcs.config.SecurityPublicKeyPolicy;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.AnyPasswordAuthenticator;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.AnyPublicKeyAuthenticator;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.BarePasswordAuthenticator;
import pl.edu.agh.xdcs.grpc.ssh.authenticators.ShadowFilePasswordAuthenticator;
import pl.edu.agh.xdcs.util.ObjectMatcher;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamil Jarosz
 */
public class GrpcSshConfigurator {
    private ObjectMatcher<PasswordAuthenticator> passwordDirectiveMatcher = ObjectMatcher.<PasswordAuthenticator>newMatcher()
            .match(SecurityPasswordPolicy.InlinePass.class, directive ->
                    new BarePasswordAuthenticator(directive.getUsername(), directive.getPassword()))
            .match(SecurityPasswordPolicy.AllowAll.class, directive ->
                    AcceptAllPasswordAuthenticator.INSTANCE)
            .match(SecurityPasswordPolicy.File.class, directive -> {
                if (directive.getFormat() == PasswordFileFormat.SHADOW) {
                    try (InputStream is = directive.getPath().toURL().openConnection().getInputStream()) {
                        return new ShadowFilePasswordAuthenticator(is);
                    } catch (IOException e) {
                        throw new RuntimeException("IO error while reading shadow file", e);
                    }
                }

                throw new RuntimeException("Unknown password file format: " + directive.getFormat());
            })
            .other(directive -> {
                throw new RuntimeException("Unknown directive: " + directive);
            })
            .build();

    private ObjectMatcher<PublickeyAuthenticator> publicKeyDirectiveMatcher = ObjectMatcher.<PublickeyAuthenticator>newMatcher()
            .other(directive -> {
                throw new RuntimeException("Unknown directive: " + directive);
            })
            .build();

    @Inject
    @Configured
    private AgentSecurityConfiguration agentSecurityConfiguration;

    public void configure(SshServer server) {
        configurePasswordAuthentication(server);
        configurePublicKeyAuthentication(server);
    }

    private void configurePasswordAuthentication(SshServer server) {
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

    private void configurePublicKeyAuthentication(SshServer server) {
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
