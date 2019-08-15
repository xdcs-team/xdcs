package pl.edu.agh.xdcs.fsrepo;

import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.util.FsUtils;

import java.nio.file.Path;

/**
 * @author Kamil Jarosz
 */
public class FilesystemRepository {
    private final Path root;
    private final ObjectRepository objectRepository;

    private FilesystemRepository(Path root) {
        this.root = root;
        this.objectRepository = ObjectRepository.forPath(root);
    }

    public static FilesystemRepository forPath(Path root) {
        return new FilesystemRepository(root);
    }

    public Path getRoot() {
        return root;
    }

    public ObjectRepository getObjectRepository() {
        return objectRepository;
    }

    public Path getWorkspacePath(String taskDefinitionId) {
        return FsUtils.resolveWithoutTraversal(root.resolve("workspaces"), taskDefinitionId);
    }
}
