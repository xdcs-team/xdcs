package pl.edu.agh.xdcs.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.util.Eager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Kamil Jarosz
 */
@Eager
@ApplicationScoped
public class GrpcServer {
    @Inject
    private Logger logger;

    @Inject
    private Instance<BindableService> services;

    private Server server;

    private int port = Integer.parseInt(System.getProperty("xdcs.agent.port", "8081"));

    @PostConstruct
    public void init() {
        logger.info("Initializing GRPC server on port " + port);

        ServerBuilder<?> builder = ServerBuilder.forPort(port);
        for (BindableService service : services) {
            builder.addService(service);
        }

        server = builder.build();
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start GRPC server", e);
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down GRPC server");
        server.shutdownNow();
    }
}
