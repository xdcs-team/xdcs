package pl.edu.agh.xdcs.workspace;

import com.google.common.io.ByteStreams;
import pl.edu.agh.xdcs.util.DeletingFileVisitor;
import pl.edu.agh.xdcs.util.FsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        PosixFileAttributes attributes;
        try {
            attributes = readPosixAttributes(resolved);
        } catch (IOException e) {
            return Optional.empty();
        }

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

    private PosixFileAttributes readPosixAttributes(Path resolved) throws IOException {
        PosixFileAttributeView view = Files.getFileAttributeView(resolved, PosixFileAttributeView.class);
        return view.readAttributes();
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
        try {
            PosixFileAttributes attributes = readPosixAttributes(path);
            return FileDescription.Entry.builder()
                    .name(path.getFileName().toString())
                    .type(readFileType(attributes).orElse(null))
                    .permissions(attributes.permissions())
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        if (Files.isDirectory(resolved)) {
            Files.walkFileTree(resolved, new DeletingFileVisitor());
        } else {
            Files.deleteIfExists(resolved);
        }
    }

    @Override
    public void setup() throws IOException {
        Files.createDirectories(root);
    }

    @Override
    public void createFile(String path, FileDescription fileDescription) throws IOException {
        Path resolved = resolveWorkspacePath(path);
        Files.createDirectories(resolved.getParent());
        if (fileDescription.getType() == FileDescription.FileType.DIRECTORY) {
            Files.createDirectory(resolved);
        } else {
            Files.createFile(resolved);
        }

        if (fileDescription.getPermissions() != null) {
            Files.setPosixFilePermissions(resolved, fileDescription.getPermissions());
        }
    }

    @Override
    public void setPermissions(String path, Set<PosixFilePermission> permissions) throws IOException {
        Path resolved = resolveWorkspacePath(path);
        Files.setPosixFilePermissions(resolved, permissions);
    }

    @Override
    public void moveFile(String from, String to) throws IOException {
        Path fromResolved = resolveWorkspacePath(from);
        Path toResolved = resolveWorkspacePath(to);
        Files.move(fromResolved, toResolved, StandardCopyOption.ATOMIC_MOVE);
    }
}
