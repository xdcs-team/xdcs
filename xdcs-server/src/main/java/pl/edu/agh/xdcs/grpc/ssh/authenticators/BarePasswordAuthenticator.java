package pl.edu.agh.xdcs.grpc.ssh.authenticators;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

/**
 * @author Kamil Jarosz
 */
public class BarePasswordAuthenticator implements PasswordAuthenticator {
    private final String username;
    private final String password;

    public BarePasswordAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
        return this.username.equals(username) &&
                this.password.equals(password);
    }
}
