package pl.edu.agh.xdcs.agents;

import lombok.Builder;

import java.net.InetAddress;

/**
 * @author Kamil Jarosz
 */
@Builder
public class Agent {
    private final String name;
    private final String label;
    private final InetAddress address;
    private final Status status;

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        ONLINE,
        OFFLINE,
    }
}
