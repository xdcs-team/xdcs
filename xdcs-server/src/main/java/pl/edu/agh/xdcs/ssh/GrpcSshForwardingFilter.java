package pl.edu.agh.xdcs.ssh;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.forward.ForwardingFilter;

/**
 * @author Kamil Jarosz
 */
public class GrpcSshForwardingFilter implements ForwardingFilter {
    private static final String ALLOWED_HOST = "127.0.0.1";
    private static final int ALLOWED_PORT = 0;

    @Override
    public boolean canForwardAgent(Session session, String requestType) {
        return false;
    }

    @Override
    public boolean canListen(SshdSocketAddress address, Session session) {
        return ALLOWED_HOST.equals(address.getHostName()) && address.getPort() == ALLOWED_PORT;
    }

    @Override
    public boolean canConnect(Type type, SshdSocketAddress address, Session session) {
        return false;
    }

    @Override
    public boolean canForwardX11(Session session, String requestType) {
        return false;
    }
}
