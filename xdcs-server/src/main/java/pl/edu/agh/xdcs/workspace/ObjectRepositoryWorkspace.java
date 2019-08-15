package pl.edu.agh.xdcs.workspace;

import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.ObjectRepositoryException;
import pl.edu.agh.xdcs.or.ObjectRepositoryIOException;
import pl.edu.agh.xdcs.or.types.BlobStream;
import pl.edu.agh.xdcs.or.types.Tree;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
class ObjectRepositoryWorkspace implements Workspace {
    private final ObjectRepository or;
    private final String rootId;

    ObjectRepositoryWorkspace(ObjectRepository or, String rootId) {
        this.or = or;
        this.rootId = rootId;
    }

    private Optional<Tree.Entry> findEntry(String path, String parentId) {
        while (path.startsWith("/")) path = path.substring(1);

        String name;
        String rest;
        boolean directoryExpected;
        if (path.contains("/")) {
            int index = path.indexOf('/');
            name = path.substring(0, index);
            rest = path.substring(index + 1);
            directoryExpected = true;
        } else {
            name = path;
            rest = null;
            directoryExpected = false;
        }

        Tree parent = or.cat(parentId, Tree.class);
        Optional<Tree.Entry> entry = parent.getEntries().stream()
                .filter(e -> directoryExpected == (e.getMode().getType() == Tree.FileType.S_IFDIR))
                .filter(e -> e.getName().equals(name))
                .findAny();

        if (!directoryExpected) {
            return entry;
        }

        return entry.flatMap(e -> findEntry(rest, e.getObjectId()));
    }

    @Override
    public Optional<InputStream> openFile(String path) throws IOException {
        try {
            return findEntry(path, rootId)
                    .map(Tree.Entry::getObjectId)
                    .map(id -> or.cat(id, BlobStream.class))
                    .map(BlobStream::getStream);
        } catch (ObjectRepositoryIOException e) {
            throw e.getIOException();
        } catch (ObjectRepositoryException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileDescription> readFileDescription(String path) throws IOException {
        try {
            if (path.equals("/")) {
                return Optional.ofNullable(FileDescription.builder()
                        .type(FileDescription.FileType.DIRECTORY)
                        .permissions(EnumSet.allOf(PosixFilePermission.class))
                        .children(list(rootId))
                        .build());
            }

            return findEntry(path, rootId).map(this::mapFileDescription);
        } catch (ObjectRepositoryIOException e) {
            throw e.getIOException();
        } catch (ObjectRepositoryException e) {
            return Optional.empty();
        }
    }

    private FileDescription mapFileDescription(Tree.Entry entry) {
        List<String> children;
        if (entry.getMode().getType() == Tree.FileType.S_IFDIR) {
            children = list(entry.getObjectId());
        } else {
            children = null;
        }

        return FileDescription.builder()
                .type(mapType(entry.getMode().getType()))
                .permissions(entry.getMode().getPermissions().toPosixPermissions())
                .children(children)
                .build();
    }

    private List<String> list(String objectId) {
        return or.cat(objectId, Tree.class)
                .getEntries()
                .stream()
                .map(Tree.Entry::getName)
                .collect(Collectors.toList());
    }

    private FileDescription.FileType mapType(Tree.FileType type) {
        switch (type) {
            case S_IFLNK:
                return FileDescription.FileType.SYMLINK;
            case S_IFREG:
                return FileDescription.FileType.REGULAR;
            case S_IFDIR:
                return FileDescription.FileType.DIRECTORY;
        }

        throw new RuntimeException("Unknown type: " + type);
    }

    @Override
    public void saveFile(String path, InputStream content) {
        throw new UnsupportedOperationException();
    }
}
