package pl.edu.agh.xdcs.config;

import org.slf4j.Logger;
import pl.edu.agh.xdcs.util.Eager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Kamil Jarosz
 */
@Eager
@ApplicationScoped
public class ConfigurationLoader {
    private static final Path[] CONFIG_FILE_LOCATIONS = new Path[]{
            Paths.get(System.getProperty("jboss.home.dir", ".") + "/xdcs-config.xml"),
            Paths.get("./xdcs-config.xml"),
            Paths.get("/etc/xdcs/xdcs-config.xml"),
    };

    @Inject
    private Logger logger;

    @Inject
    private ConfigurationUnmarshaller unmarshaller;

    private Configuration configuration;

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

            try {
                configuration = unmarshaller.unmarshal(Files.newInputStream(configLocation));
            } catch (JAXBException e) {
                logger.error("Invalid configuration file", e);
                throw new RuntimeException("Invalid configuration file", e);
            } catch (IOException e) {
                String message = "IO error while reading configuration file";
                logger.error(message, e);
                throw new RuntimeException(message, e);
            }
        }

        if (configuration == null) {
            String message = "Configuration not found, the following locations " +
                    "have been searched: " + Arrays.toString(CONFIG_FILE_LOCATIONS);
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    @Produces
    @Configured
    public Configuration getConfiguration() {
        return configuration;
    }

    @Produces
    @Configured
    public AgentSecurityConfiguration getAgentSecurityConfiguration() {
        return configuration.getAgentSecurity();
    }
}
