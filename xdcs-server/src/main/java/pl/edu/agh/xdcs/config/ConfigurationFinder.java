package pl.edu.agh.xdcs.config;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Kamil Jarosz
 */
@ApplicationScoped
public class ConfigurationFinder {
    private static final Path[] CONFIG_FILE_LOCATIONS = new Path[]{
            Paths.get(System.getProperty("jboss.home.dir", ".") + "/xdcs-config.xml"),
            Paths.get("./xdcs-config.xml"),
            Paths.get("/etc/xdcs/xdcs-config.xml"),
    };

    @Inject
    private Logger logger;

    private Path configurationPath;

    @PostConstruct
    public void initialize() {
        for (Path configLocation : CONFIG_FILE_LOCATIONS) {
            logger.debug("Looking for configuration at " + configLocation);
            if (!Files.isRegularFile(configLocation)) {
                logger.debug("File " + configLocation + " doesn't exist or is not a regular file");
                continue;
            }

            if (!Files.isReadable(configLocation)) {
                String message = "Configuration file " + configLocation + " is not readable";
                logger.error(message);
                throw new RuntimeException(message);
            }

            logger.debug("Configuration found at " + configLocation);
            configurationPath = configLocation;
        }

        if (configurationPath == null) {
            String message = "Configuration not found, the following locations " +
                    "have been searched: " + Arrays.toString(CONFIG_FILE_LOCATIONS);
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    public Path getConfigurationPath() {
        return configurationPath;
    }
}
