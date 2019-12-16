package pl.edu.agh.xdcs.agents;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.edu.agh.xdcs.db.entity.AgentEntity;

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
    private AgentEntity.Status status;
    @Setter
    private InetSocketAddress tunnelEndpoint;

    @Override
    public String toString() {
        return name + "/" + address;
    }

    public boolean isReady() {
        return this.status == AgentEntity.Status.READY;
    }
}
