package pl.edu.agh.xdcs.or.util;

import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.types.Tree;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryUtils {
    @Inject
    private ObjectRepository objectRepository;

    public void walkTree(String treeId, OrFileVisitor visitor) {
        walkTree0(treeId, visitor, "/");
    }

    private void walkTree0(String treeId, OrFileVisitor visitor, String path) {
        Tree tree = objectRepository.cat(treeId, Tree.class);
        tree.getEntries().forEach(entry -> {
            String name = entry.getName();
            switch (entry.getMode().getType()) {
                case S_IFLNK:
                case S_IFREG:
                    visitor.visitEntry(path + name, entry);
                    break;
                case S_IFDIR:
                    visitor.beforeVisitDirectory(path + name, entry);
                    walkTree0(entry.getObjectId(), visitor, path + name + "/");
                    visitor.afterVisitDirectory(path + name, entry);
            }
        });
    }

    public Optional<Tree.Entry> getChildEntry(String rootId, String path) {
        List<String> parts = Arrays.stream(path.split("/"))
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toList());

        String parentId = rootId;
        for (String part : parts.subList(0, parts.size() - 1)) {
            String childId = objectRepository.cat(parentId, Tree.class)
                    .getEntries()
                    .stream()
                    .filter(entry -> entry.getMode().getType() == Tree.FileType.S_IFDIR)
                    .filter(entry -> entry.getName().equals(part))
                    .map(Tree.Entry::getObjectId)
                    .findAny()
                    .orElse(null);

            if (childId == null) {
                return Optional.empty();
            }

            parentId = childId;
        }

        String filename = parts.get(parts.size() - 1);
        return objectRepository.cat(parentId, Tree.class)
                .getEntries()
                .stream()
                .filter(entry -> entry.getMode().getType() != Tree.FileType.S_IFDIR)
                .filter(entry -> entry.getName().equals(filename))
                .findAny();
    }
}
