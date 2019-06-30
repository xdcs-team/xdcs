package pl.edu.agh.xdcs.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.grpc.tunnel.TunnelBrokerService;
import pl.edu.agh.xdcs.api.AgentGrpc;
import pl.edu.agh.xdcs.api.Task;
import pl.edu.agh.xdcs.api.TaskExecutionResult;
import pl.edu.agh.xdcs.api.TaskType;
import pl.edu.agh.xdcs.util.Eager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
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

    @Resource
    private ManagedExecutorService executorService;

    private Server server;

    private int port = Integer.parseInt(System.getProperty("xdcs.agent.port", "8081"));

    @PostConstruct
    public void init() {
        logger.info("Initializing GRPC server on port " + port);

        ServerBuilder<?> builder = ServerBuilder.forPort(port)
                .executor(executorService)
                .addService(new TunnelBrokerService(executorService, channel -> {
                    logger.info("YEEEEEEEEY, it works");

                    AgentGrpc.AgentStub agent = AgentGrpc.newStub(channel);
                    logger.info("stub created");

                    for(int i = 0; i < 4; ++i) agent.executeTask(Task.newBuilder().setType(TaskType.OPEN_CL).build(), new StreamObserver<TaskExecutionResult>() {
                        @Override
                        public void onNext(TaskExecutionResult value) {
                            logger.info("onNext: " + value);
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.info("onError: " + t);
                        }

                        @Override
                        public void onCompleted() {
                            logger.info("onCompleted");
                        }
                    });
                    logger.info("task executed");
                }));

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
