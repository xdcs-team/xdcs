package pl.edu.agh.xdcs.ssh.authenticators;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import java.util.Arrays;

/**
 * @author Kamil Jarosz
 */
public class AnyPasswordAuthenticator implements PasswordAuthenticator {
    private final PasswordAuthenticator[] authenticators;

    public AnyPasswordAuthenticator(PasswordAuthenticator... authenticators) {
        this.authenticators = authenticators;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session)
            throws PasswordChangeRequiredException, AsyncAuthException {
        return Arrays.stream(authenticators).anyMatch(a -> a.authenticate(username, password, session));
    }
}
