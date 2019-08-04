package pl.edu.agh.xdcs.config.util;

import pl.edu.agh.xdcs.config.ConfigurationFinder;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kamil Jarosz
 */
public class ReferencedFileLoader {
    @Inject
    private ConfigurationFinder finder;

    public Path toPath(URI uri) {
        Path dir = finder.getConfigurationPath().getParent();
        return dir.resolve(uri.toString());
    }

    public InputStream loadFile(URI uri) throws IOException {
        Path dir = finder.getConfigurationPath().getParent();
        return Files.newInputStream(dir.resolve(uri.toString()));
    }
}
