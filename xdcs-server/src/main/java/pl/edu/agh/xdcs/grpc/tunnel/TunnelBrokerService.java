package pl.edu.agh.xdcs.grpc.tunnel;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.xdcs.api.TunnelBrokerGrpc;
import pl.edu.agh.xdcs.api.TunneledMessage;

import javax.enterprise.concurrent.ManagedExecutorService;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Kamil Jarosz
 */
public class TunnelBrokerService extends TunnelBrokerGrpc.TunnelBrokerImplBase {
    private final static Logger logger = LoggerFactory.getLogger(TunnelBrokerService.class);

    private final ManagedExecutorService executor;
    private final TunnelListener tunnelListener;

    public TunnelBrokerService(ManagedExecutorService executor, TunnelListener tunnelListener) {
        this.executor = executor;
        this.tunnelListener = tunnelListener;
    }

    @Override
    public StreamObserver<TunneledMessage> tunnel(StreamObserver<TunneledMessage> responseObserver) {
        try {
            return tunnel0(responseObserver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private StreamObserver<TunneledMessage> tunnel0(StreamObserver<TunneledMessage> responseObserver) throws IOException {
        ProxyServer proxyServer = new ProxyServer(executor);
        proxyServer.setChunkListener(chunk -> {
            if (logger.isTraceEnabled()) {
                logger.trace("Tunneling server->client: " + Arrays.toString(chunk));
            }

            responseObserver.onNext(TunneledMessage.newBuilder()
                    .setData(ByteString.copyFrom(chunk))
                    .build());
        });
        proxyServer.setErrorListener(t -> {
            logger.error("Error tunneling server->client", t);
            responseObserver.onError(t);
        });
        proxyServer.setCompletionListener(() -> {
            logger.trace("No more data to tunnel server->client");
            responseObserver.onCompleted();
        });
        int port = proxyServer.getLocalPort();

        ManagedChannel tunneledChannel = ManagedChannelBuilder.forAddress("localhost", port)
                .executor(executor)
                .usePlaintext()
                .build();
        tunneledChannel.getState(true);

        logger.debug("Accepting connection to proxy server");
        proxyServer.waitForConnection();
        logger.debug("Accepted");

        StreamObserver<TunneledMessage> observer = new StreamObserver<TunneledMessage>() {
            @Override
            public void onNext(TunneledMessage value) {
                byte[] bytes = value.getData().toByteArray();

                if (logger.isTraceEnabled()) {
                    logger.trace("Tunneling client->server: " + Arrays.toString(bytes));
                }

                try {
                    proxyServer.sendChunk(bytes);
                } catch (IOException e) {
                    logger.error("IO Exception while writing to proxy connection", e);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Error while tunneling client->server", t);
                done();
            }

            @Override
            public void onCompleted() {
                done();
            }

            private void done() {
                logger.debug("No more data to tunnel client->server");

                tunneledChannel.shutdown();
                try {
                    proxyServer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        executor.execute(() -> tunnelListener.tunnelCreated(tunneledChannel));
        return observer;
    }
}
