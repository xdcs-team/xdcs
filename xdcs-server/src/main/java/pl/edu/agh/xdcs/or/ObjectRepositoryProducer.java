package pl.edu.agh.xdcs.or;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class ObjectRepositoryProducer {
    private static final Path OR_PATH = Paths.get(System.getProperty("jboss.home.dir", "."))
            .resolve("xdcs/object-repository");

    private ObjectRepository globalObjectRepository;

    @PostConstruct
    public void initialize() {
        globalObjectRepository = ObjectRepository.forPath(OR_PATH);
    }

    @Produces
    public ObjectRepository getGlobalObjectRepository() {
        return globalObjectRepository;
    }
}
