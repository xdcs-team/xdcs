package pl.edu.agh.xdcs.workspace;

import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.ObjectRepositoryIOException;
import pl.edu.agh.xdcs.or.types.BlobStream;
import pl.edu.agh.xdcs.or.types.Tree;
import pl.edu.agh.xdcs.restapi.mapper.UnsatisfiedMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryWorkspaceWriter {
    private final ObjectRepository or;

    private ObjectRepositoryWorkspaceWriter(ObjectRepository or) {
        this.or = or;
    }

    public static ObjectRepositoryWorkspaceWriter forObjectRepository(ObjectRepository or) {
        return new ObjectRepositoryWorkspaceWriter(or);
    }

    public String write(Workspace ws) {
        try {
            return writeDirectory(ws, "/");
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    private String writeDirectory(Workspace ws, String path) throws IOException {
        List<FileDescription.Entry> children = ws.readFileDescription(path)
                .orElseThrow(RuntimeException::new)
                .getChildren();

        List<Tree.Entry> entries = new ArrayList<>();
        for (FileDescription.Entry child : children) {
            String childPath = path + "/" + child.getName();
            Tree.FileType type = mapFileType(child.getType());
            String id;
            if (type == Tree.FileType.S_IFDIR) {
                id = writeDirectory(ws, childPath);
            } else {
                id = writeFile(ws, childPath);
            }
            Tree.FilePermissions permissions = Tree.FilePermissions.fromPosixPermissions(child.getPermissions());
            entries.add(Tree.Entry.of(Tree.EntryMode.of(type, permissions), child.getName(), id));
        }

        return or.store(Tree.ofEntries(entries));
    }

    private Tree.FileType mapFileType(FileDescription.FileType type) {
        switch (type) {
            case REGULAR:
                return Tree.FileType.S_IFREG;
            case DIRECTORY:
                return Tree.FileType.S_IFDIR;
            case SYMLINK:
                return Tree.FileType.S_IFLNK;
            default:
                throw new UnsatisfiedMappingException();
        }
    }

    private String writeFile(Workspace ws, String path) throws IOException {
        return or.store(BlobStream.from(ws.openFile(path).orElseThrow(RuntimeException::new)));
    }
}
