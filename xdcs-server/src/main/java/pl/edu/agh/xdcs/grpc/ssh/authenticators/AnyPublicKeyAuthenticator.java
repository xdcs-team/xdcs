package pl.edu.agh.xdcs.grpc.ssh.authenticators;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import java.security.PublicKey;
import java.util.Arrays;

/**
 * @author Kamil Jarosz
 */
public class AnyPublicKeyAuthenticator implements PublickeyAuthenticator {
    private final PublickeyAuthenticator[] authenticators;

    public AnyPublicKeyAuthenticator(PublickeyAuthenticator... authenticators) {
        this.authenticators = authenticators;
    }

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {
        return Arrays.stream(authenticators).anyMatch(a -> a.authenticate(username, key, session));
    }
}
