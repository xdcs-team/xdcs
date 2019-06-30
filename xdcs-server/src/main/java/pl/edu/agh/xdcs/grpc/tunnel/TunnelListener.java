package pl.edu.agh.xdcs.grpc.tunnel;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;

/**
 * @author Kamil Jarosz
 */
public interface TunnelListener {
    void tunnelCreated(ManagedChannel channel);
}
