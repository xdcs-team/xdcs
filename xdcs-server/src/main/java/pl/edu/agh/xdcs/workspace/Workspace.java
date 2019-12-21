package pl.edu.agh.xdcs.workspace;

import pl.edu.agh.xdcs.or.ObjectRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;

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

    void deleteFile(String path) throws IOException;

    void setup() throws IOException;

    void createFile(String path, FileDescription fileDescription) throws IOException;

    void setPermissions(String path, Set<PosixFilePermission> permissions) throws IOException;

    void moveFile(String from, String to) throws IOException;
}
