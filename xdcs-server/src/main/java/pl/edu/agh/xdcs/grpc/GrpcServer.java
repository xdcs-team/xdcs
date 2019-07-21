package pl.edu.agh.xdcs.grpc;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.api.AgentGrpc;
import pl.edu.agh.xdcs.api.HeartbeatGrpc;
import pl.edu.agh.xdcs.api.HeartbeatRequest;
import pl.edu.agh.xdcs.api.Task;
import pl.edu.agh.xdcs.api.TaskExecutionResult;
import pl.edu.agh.xdcs.api.TaskType;
import pl.edu.agh.xdcs.grpc.ee.GrpcContextInterceptor;
import pl.edu.agh.xdcs.util.Eager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
@Eager
@ApplicationScoped
public class GrpcServer {
    @Inject
    private Logger logger;

    @Resource
    private ManagedExecutorService executorService;

    @Inject
    private GrpcContextInterceptor contextInterceptor;

    private Server server;

    private int port = Integer.parseInt(System.getProperty("xdcs.agent.port.grpc", "8081"));

    @PostConstruct
    public void init() {
        logger.info("Initializing GRPC server on port " + port);

        server = createServer();

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start GRPC server", e);
        }
    }

    private Server createServer() {
        ServerBuilder<?> builder = ServerBuilder.forPort(port)
                .executor(executorService)
                .intercept(contextInterceptor);

        Set<Bean<?>> beans = CDI.current().getBeanManager().getBeans(BindableService.class);
        for (Bean bean : beans) {
            builder.addService(createProxyFromBean(bean));
        }

        return builder.build();
    }

    private BindableService createProxyFromBean(Bean<?> bean) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(bean.getBeanClass().getSuperclass());
        factory.setFilter(method -> {
            try {
                return !method.equals(BindableService.class.getMethod("bindService"));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        MethodHandler handler = (self, method, proceed, args) -> {
            Object delegate = CDI.current().select(bean.getBeanClass()).get();

            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                logger.warn("Bindable service delegate threw an exception, rethrowing it", e);
                throw e.getTargetException();
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new RuntimeException("Exception occurred while invoking delegate method", e);
            }
        };

        try {
            return (BindableService) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Cannot create a proxy", e);
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down GRPC server");
        server.shutdownNow();
    }
}
