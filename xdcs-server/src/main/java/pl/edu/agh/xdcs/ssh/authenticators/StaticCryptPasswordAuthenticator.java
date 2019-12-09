package pl.edu.agh.xdcs.ssh.authenticators;

import org.apache.commons.codec.digest.Crypt;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * @author Kamil Jarosz
 */
public class StaticCryptPasswordAuthenticator implements PasswordAuthenticator {
    private final Map<String, String> passwords;

    public StaticCryptPasswordAuthenticator(Map<String, String> passwords) {
        this.passwords = passwords;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session)
            throws PasswordChangeRequiredException, AsyncAuthException {
        if (!passwords.containsKey(username)) {
            return false;
        }

        String cryptedExpected = passwords.get(username);
        String crypted = Crypt.crypt(password.getBytes(StandardCharsets.UTF_8), cryptedExpected);
        return Objects.equals(cryptedExpected, crypted);
    }
}
