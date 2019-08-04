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

/**
 * @author Kamil Jarosz
 */
@Eager
@ApplicationScoped
public class ConfigurationLoader {
    @Inject
    private Logger logger;

    @Inject
    private ConfigurationFinder configurationFinder;

    @Inject
    private ConfigurationUnmarshaller unmarshaller;

    private Configuration configuration;

    @PostConstruct
    public void initialize() {
        try {
            configuration = unmarshaller.unmarshal(Files.newInputStream(configurationFinder.getConfigurationPath()));
        } catch (JAXBException e) {
            logger.error("Invalid configuration file", e);
            throw new RuntimeException("Invalid configuration file", e);
        } catch (IOException e) {
            String message = "IO error while reading configuration file";
            logger.error(message, e);
            throw new RuntimeException(message, e);
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

    @Produces
    @Configured
    public WebSecurityConfiguration getWebSecurityConfiguration() {
        return configuration.getWebSecurity();
    }
}
