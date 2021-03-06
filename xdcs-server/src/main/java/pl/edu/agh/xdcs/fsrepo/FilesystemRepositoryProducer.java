package pl.edu.agh.xdcs.fsrepo;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import pl.edu.agh.xdcs.or.ChecksumVerificationException;
import pl.edu.agh.xdcs.or.ConsistencyCheckFailedException;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.ObjectRepositoryTypeHandler;
import pl.edu.agh.xdcs.or.types.GlobalTypeHandler;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class FilesystemRepositoryProducer {
    private static final Path OR_PATH = Paths.get(System.getProperty("jboss.home.dir", ".")).resolve("xdcs");

    private FilesystemRepository globalFilesystemRepository;

    @Inject
    private Logger logger;

    @Inject
    private DatabaseRootProvider databaseRootProvider;

    private void wake(@Observes @Initialized(ApplicationScoped.class) Object event) {
        // initialize this bean on startup
    }

    @PostConstruct
    public void initialize() {
        globalFilesystemRepository = FilesystemRepository.forPath(OR_PATH);
        ObjectRepository or = globalFilesystemRepository.getObjectRepository();

        Instance<Object> globalHandlers = CDI.current()
                .select(GlobalTypeHandler.Literal.instance());
        for (Object handler : globalHandlers) {
            if (handler instanceof ObjectRepositoryTypeHandler) {
                or.register((ObjectRepositoryTypeHandler<?>) handler);
            }
        }

        try {
            verifyChecksums(or);
            checkConsistency(or);
        } catch (ChecksumVerificationException e) {
            logger.error("Verifying checksums failed", e);
            throw e;
        } catch (ConsistencyCheckFailedException e) {
            logger.error("Consistency check failed", e);
            throw e;
        } catch (InterruptedException e) {
            logger.error("Filesystem repository initialization has been interrupted", e);
            throw new RuntimeException(e);
        }
    }

    private void checkConsistency(ObjectRepository or) throws InterruptedException {
        logger.info("Checking object repository consistency");
        Stopwatch sw2 = Stopwatch.createStarted();
        or.checkConsistency(databaseRootProvider);
        logger.info("Consistency check succeeded (" + sw2 + ")");
    }

    private void verifyChecksums(ObjectRepository or) throws InterruptedException {
        logger.info("Verifying object repository checksums");
        Stopwatch sw = Stopwatch.createStarted();
        or.verifyChecksums();
        logger.info("Verification succeeded (" + sw + ")");
    }

    @Produces
    public FilesystemRepository getGlobalFilesystemRepository() {
        return globalFilesystemRepository;
    }

    @Produces
    public ObjectRepository getGlobalObjectRepository() {
        return globalFilesystemRepository.getObjectRepository();
    }
}
