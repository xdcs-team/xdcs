package pl.edu.agh.xdcs.fsrepo;

import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.ObjectRepositoryTypeHandler;
import pl.edu.agh.xdcs.or.types.GlobalTypeHandler;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class FilesystemRepositoryProducer {
    private static final Path OR_PATH = Paths.get(System.getProperty("jboss.home.dir", ".")).resolve("xdcs");

    private FilesystemRepository globalFilesystemRepository;

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
