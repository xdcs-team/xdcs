package pl.edu.agh.xdcs.config.web.security;

import pl.edu.agh.xdcs.config.Configured;
import pl.edu.agh.xdcs.config.PasswordFileFormat;
import pl.edu.agh.xdcs.config.SecurityPasswordPolicy;
import pl.edu.agh.xdcs.config.WebSecurityConfiguration;
import pl.edu.agh.xdcs.config.util.ReferencedFileLoader;
import pl.edu.agh.xdcs.security.web.WebAuthenticator;
import pl.edu.agh.xdcs.security.web.authenticators.LdapWebAuthenticator;
import pl.edu.agh.xdcs.security.web.authenticators.ShadowFileWebAuthenticator;
import pl.edu.agh.xdcs.util.Eager;
import pl.edu.agh.xdcs.util.Enabled;
import pl.edu.agh.xdcs.util.ObjectMatcher;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
@Eager
@Enabled
public class PasswordPolicyWebAuthenticator implements WebAuthenticator {
    @Inject
    private ReferencedFileLoader fileLoader;

    private ObjectMatcher<WebAuthenticator> passwordDirectiveMatcher = ObjectMatcher.<WebAuthenticator>newMatcher()
            .match(SecurityPasswordPolicy.InlinePass.class, this::parseDirective)
            .match(SecurityPasswordPolicy.AllowAll.class, this::parseDirective)
            .match(SecurityPasswordPolicy.File.class, this::parseDirective)
            .match(SecurityPasswordPolicy.Ldap.class, LdapWebAuthenticator::new)
            .other(directive -> {
                throw new RuntimeException("Unknown directive: " + directive);
            })
            .build();

    @Inject
    @Configured
    private WebSecurityConfiguration webSecurityConfiguration;

    private List<WebAuthenticator> authenticators = new ArrayList<>();

    private WebAuthenticator parseDirective(SecurityPasswordPolicy.File directive) {
        if (directive.getFormat() == PasswordFileFormat.SHADOW) {
            try (InputStream is = fileLoader.loadFile(directive.getPath())) {
                return new ShadowFileWebAuthenticator(is);
            } catch (IOException e) {
                throw new RuntimeException("IO error while reading shadow file", e);
            }
        }

        throw new RuntimeException("Unknown password file format: " + directive.getFormat());
    }

    private WebAuthenticator parseDirective(SecurityPasswordPolicy.AllowAll directive) {
        return (username, password) -> true;
    }

    private WebAuthenticator parseDirective(SecurityPasswordPolicy.InlinePass directive) {
        return (username, password) -> directive.getUsername().equals(username) && directive.getPassword().equals(password);
    }

    @PostConstruct
    public void init() {
        SecurityPasswordPolicy passwordPolicy = webSecurityConfiguration.getPasswordPolicy();
        if (passwordPolicy == null) return;

        for (Object directive : passwordPolicy.getDirectives()) {
            authenticators.add(passwordDirectiveMatcher.match(directive));
        }
    }

    @Override
    public boolean authenticate(String username, String password) {
        return authenticators.stream()
                .anyMatch(a -> a.authenticate(username, password));
    }
}
