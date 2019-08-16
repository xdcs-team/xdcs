package pl.edu.agh.xdcs.workspace;

import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.ObjectRepositoryIOException;
import pl.edu.agh.xdcs.or.types.BlobStream;
import pl.edu.agh.xdcs.or.types.Tree;

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
            List<String> rootChildren = ws.readFileDescription("/")
                    .orElseThrow(RuntimeException::new)
                    .getChildren();
            return writeDirectory(ws, "/", rootChildren);
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    private String writeDirectory(Workspace ws, String path, List<String> children) throws IOException {
        List<Tree.Entry> entries = new ArrayList<>();
        for (String child : children) {
            String childPath = path + "/" + child;
            FileDescription desc = ws.readFileDescription(childPath)
                    .orElseThrow(RuntimeException::new);

            Tree.FileType type;
            switch (desc.getType()) {
                case REGULAR:
                    type = Tree.FileType.S_IFREG;
                    break;
                case DIRECTORY:
                    type = Tree.FileType.S_IFDIR;
                    break;
                case SYMLINK:
                    type = Tree.FileType.S_IFLNK;
                    break;
                default:
                    throw new RuntimeException();
            }

            String id = writeFile(ws, childPath);
            Tree.FilePermissions permissions = Tree.FilePermissions.fromPosixPermissions(desc.getPermissions());
            entries.add(Tree.Entry.of(Tree.EntryMode.of(type, permissions), child, id));
        }

        return or.store(Tree.ofEntries(entries));
    }

    private String writeFile(Workspace ws, String path) throws IOException {
        return or.store(BlobStream.from(ws.openFile(path).orElseThrow(RuntimeException::new)));
    }
}
