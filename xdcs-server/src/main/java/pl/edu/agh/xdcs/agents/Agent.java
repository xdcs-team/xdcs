package pl.edu.agh.xdcs.agents;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Kamil Jarosz
 */
@Getter
@RequiredArgsConstructor
public class Agent {
    private final String name;

    @Setter
    private String displayName;
    @Setter
    private InetAddress address;
    @Setter
    private Status status;
    @Setter
    private InetSocketAddress tunnelEndpoint;

    @Override
    public String toString() {
        return name + "/" + address;
    }

    public enum Status {
        UNAVAILABLE,
        READY,
        BUSY,
        OFFLINE,
    }
}
