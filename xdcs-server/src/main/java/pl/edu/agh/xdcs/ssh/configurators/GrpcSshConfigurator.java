package pl.edu.agh.xdcs.ssh.configurators;

import org.apache.sshd.server.SshServer;

/**
 * @author Kamil Jarosz
 */
public interface GrpcSshConfigurator {
    void configure(SshServer server);
}
