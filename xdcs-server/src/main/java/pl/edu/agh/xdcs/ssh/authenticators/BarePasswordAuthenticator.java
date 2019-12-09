package pl.edu.agh.xdcs.ssh.authenticators;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Kamil Jarosz
 */
public class BarePasswordAuthenticator implements PasswordAuthenticator {
    private final String username;
    private final byte[] password;

    public BarePasswordAuthenticator(String username, byte[] password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session)
            throws PasswordChangeRequiredException, AsyncAuthException {
        return this.username.equals(username) &&
                Arrays.equals(this.password, password.getBytes(StandardCharsets.UTF_8));
    }
}
