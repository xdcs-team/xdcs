package pl.edu.agh.xdcs.workspace;

import pl.edu.agh.xdcs.or.ObjectRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public interface Workspace {
    static Workspace forPath(Path root) {
        return new FilesystemWorkspace(root);
    }

    static Workspace forObject(ObjectRepository or, String objectId) {
        return new ObjectRepositoryWorkspace(or, objectId);
    }

    Optional<InputStream> openFile(String path) throws IOException;

    Optional<FileDescription> readFileDescription(String path) throws IOException;

    void saveFile(String path, InputStream content) throws IOException;
}
