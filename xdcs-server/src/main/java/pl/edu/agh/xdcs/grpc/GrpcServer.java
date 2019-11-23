package pl.edu.agh.xdcs.grpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.grpc.ee.GrpcContextInterceptor;
import pl.edu.agh.xdcs.grpc.security.GrpcSecurityGenerator;
import pl.edu.agh.xdcs.util.ApplicationStartedEvent;
import pl.edu.agh.xdcs.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class GrpcServer {
    private final int port = Integer.parseInt(System.getProperty("xdcs.agent.port.grpc", "8081"));

    @Inject
    private Logger logger;

    @Resource
    private ManagedExecutorService executorService;

    @Inject
    private GrpcContextInterceptor contextInterceptor;

    @Inject
    private GrpcSecurityGenerator securityGenerator;

    private Server server;
    private KeyPair keyPair;
    private X509Certificate certificate;

    @PostConstruct
    private void init() {
        Security.addProvider(new BouncyCastleProvider());

        logger.info("Initializing GRPC server on port " + port);

        keyPair = securityGenerator.generateKeyPair();
        certificate = securityGenerator.generateSelfSignedCertificate(keyPair);
        server = createServer();

        try {
            server.start();
        } catch (IOException e) {
            throw new GrpcServerException("Could not start GRPC server", e);
        }
    }

    private void wake(@Observes ApplicationStartedEvent event) {

    }

    public InputStream getCertificate() {
        return toPemCert(certificate);
    }

    private Server createServer() {
        ServerBuilder<?> builder = ServerBuilder.forPort(port)
                .executor(executorService)
                .intercept(contextInterceptor)
                .useTransportSecurity(
                        getCertificate(),
                        toPemKey(keyPair.getPrivate().getEncoded()));

        Set<Bean<?>> beans = CDI.current().getBeanManager()
                .getBeans(BindableService.class, Service.INSTANCE);
        for (Bean bean : beans) {
            builder.addService(createProxyFromBean(bean));
        }

        return builder.build();
    }

    private ByteArrayInputStream toPemCert(X509Certificate cert) {
        StringBuilder ret = new StringBuilder();

        try {
            String stringEncoded = Base64.getEncoder().encodeToString(cert.getEncoded());

            // Begin Certificate
            ret.append("-----BEGIN CERTIFICATE-----\n");

            StringUtil.breakByLength(stringEncoded, 65)
                    .forEach(line -> ret.append(line).append('\n'));

            // End Certificate
            ret.append("-----END CERTIFICATE-----\n\n");
        } catch (CertificateEncodingException e) {
            throw new AssertionError(e);
        }

        return new ByteArrayInputStream(ret.substring(0, ret.length() - 1)
                .getBytes(StandardCharsets.UTF_8));
    }

    private ByteArrayInputStream toPemKey(byte[] encoded) {
        String stringEncoded = Base64.getEncoder().encodeToString(encoded);
        String cert = "-----BEGIN PRIVATE KEY-----" + "\n" +
                StringUtil.breakByLength(stringEncoded, 65)
                        .collect(Collectors.joining("\n")) + "\n" +
                "-----END PRIVATE KEY-----";

        return new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8));
    }

    private BindableService createProxyFromBean(Bean<?> bean) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(bean.getBeanClass().getSuperclass());
        factory.setFilter(method -> {
            try {
                return !method.equals(BindableService.class.getMethod("bindService"));
            } catch (NoSuchMethodException e) {
                throw new GrpcServerException(e);
            }
        });

        MethodHandler handler = (self, method, proceed, args) -> {
            Object delegate = CDI.current().select(bean.getBeanClass(), Service.INSTANCE).get();

            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                logger.warn("Bindable service delegate threw an exception, rethrowing it", e);
                throw e.getTargetException();
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new GrpcServerException("Exception occurred while invoking delegate method", e);
            }
        };

        try {
            return (BindableService) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new GrpcServerException("Cannot create a proxy", e);
        }
    }

    @PreDestroy
    private void destroy() {
        logger.info("Shutting down GRPC server");
        server.shutdownNow();
    }
}
