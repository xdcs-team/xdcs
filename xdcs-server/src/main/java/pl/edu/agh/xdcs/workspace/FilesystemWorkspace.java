package pl.edu.agh.xdcs.workspace;

import com.google.common.io.ByteStreams;
import pl.edu.agh.xdcs.util.FsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
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
    private final Path root;

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
        PosixFileAttributes attributes = readPosixAttributes(resolved);

        FileDescription.FileType type = readFileType(attributes).orElse(null);
        List<FileDescription.Entry> children;
        if (attributes.isRegularFile()) {
            children = null;
        } else if (attributes.isDirectory()) {
            children = Files.list(resolved)
                    .map(this::readEntry)
                    .collect(Collectors.toList());
        } else if (attributes.isSymbolicLink()) {
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

    private PosixFileAttributes readPosixAttributes(Path resolved) {
        PosixFileAttributeView view = Files.getFileAttributeView(resolved, PosixFileAttributeView.class);
        try {
            return view.readAttributes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<FileDescription.FileType> readFileType(PosixFileAttributes attributes) {
        if (attributes.isRegularFile()) {
            return Optional.of(FileDescription.FileType.REGULAR);
        } else if (attributes.isDirectory()) {
            return Optional.of(FileDescription.FileType.DIRECTORY);
        } else if (attributes.isSymbolicLink()) {
            return Optional.of(FileDescription.FileType.SYMLINK);
        } else {
            return Optional.empty();
        }
    }

    private FileDescription.Entry readEntry(Path path) {
        PosixFileAttributes attributes = readPosixAttributes(path);
        return FileDescription.Entry.builder()
                .name(path.getFileName().toString())
                .type(readFileType(attributes).orElse(null))
                .permissions(attributes.permissions())
                .build();
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

    @Override
    public void deleteFile(String path) throws IOException {
        Path resolved = resolveWorkspacePath(path);
        Files.deleteIfExists(resolved);
    }

    @Override
    public void setup() throws IOException {
        Files.createDirectories(root);
    }
}
