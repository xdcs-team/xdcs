package pl.edu.agh.xdcs.workspace;

import com.google.common.io.ByteStreams;
import pl.edu.agh.xdcs.util.FsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
class FilesystemWorkspace implements Workspace {
    private Path root;

    FilesystemWorkspace(Path root) {
        this.root = root;
    }

    @Override
    public Optional<InputStream> openFile(String path) throws IOException {
        try {
            return Optional.of(Files.newInputStream(resolveWorkspacePath(path)));
        } catch (NoSuchFileException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileDescription> readFileDescription(String path) throws IOException {
        Path resolved = resolveWorkspacePath(path);
        PosixFileAttributeView view = Files.getFileAttributeView(resolved, PosixFileAttributeView.class);
        PosixFileAttributes attributes = view.readAttributes();

        FileDescription.FileType type;
        List<String> children;
        if (attributes.isRegularFile()) {
            type = FileDescription.FileType.REGULAR;
            children = null;
        } else if (attributes.isDirectory()) {
            type = FileDescription.FileType.DIRECTORY;
            children = Files.list(resolved)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } else if (attributes.isSymbolicLink()) {
            type = FileDescription.FileType.SYMLINK;
            children = null;
        } else {
            return Optional.empty();
        }

        return Optional.ofNullable(FileDescription.builder()
                .type(type)
                .children(children)
                .permissions(attributes.permissions())
                .build());
    }

    @Override
    public void saveFile(String path, InputStream content) throws IOException {
        Path resolved = resolveWorkspacePath(path);
        Files.createDirectories(resolved.getParent());
        try (OutputStream os = Files.newOutputStream(resolved)) {
            ByteStreams.copy(content, os);
        }
    }

    private Path resolveWorkspacePath(String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }

        return FsUtils.resolveWithoutTraversal(root, path);
    }
}
