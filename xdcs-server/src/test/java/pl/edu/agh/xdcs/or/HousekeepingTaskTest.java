package pl.edu.agh.xdcs.or;

import org.junit.jupiter.api.Test;
import pl.edu.agh.xdcs.or.types.Blob;
import pl.edu.agh.xdcs.or.types.Tree;
import pl.edu.agh.xdcs.test.utils.ObjectRepositoryTestBase;

import java.nio.file.Files;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil Jarosz
 */
class HousekeepingTaskTest extends ObjectRepositoryTestBase {
    @Test
    void noUnreachableObjects() {
        Blob blob1 = Blob.fromBytes("test blob".getBytes());
        Blob blob2 = Blob.fromBytes("test blob 2".getBytes());

        Tree rootTree = Tree.ofEntries(
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob1",
                        repository.store(blob1)),
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob2",
                        repository.store(blob2)));

        String rootId = repository.store(rootTree);

        try (HousekeepingTask task = new HousekeepingTask(repository, visitor -> {
            visitor.visit(rootId, Tree.class);
            assertThat(Files.exists(root.resolve(".reachable")))
                    .isTrue();
        })) {
            Set<String> unreachableObjects = task.call();

            assertThat(unreachableObjects).isEmpty();
        }

        assertThat(Files.exists(root.resolve(".reachable")))
                .isFalse();
    }

    @Test
    void unreachableObjects() {
        Blob blob1 = Blob.fromBytes("test blob".getBytes());
        Blob blob2 = Blob.fromBytes("test blob 2".getBytes());
        Blob blob3 = Blob.fromBytes("test blob 3".getBytes());

        Blob blob4 = Blob.fromBytes("test blob 4".getBytes());
        String blob4Id = repository.store(blob4);

        Blob blob5 = Blob.fromBytes("test blob 5".getBytes());

        String blob5Id = repository.store(blob5);
        Tree unreachableTree = Tree.ofEntries(
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob1",
                        repository.store(blob1)),
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob2",
                        blob5Id));
        String unreachableTreeId = repository.store(unreachableTree);

        Tree rootTree = Tree.ofEntries(
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob1",
                        repository.store(blob1)),
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob2",
                        repository.store(blob2)));

        String rootId1 = repository.store(rootTree);
        String rootId2 = repository.store(blob3);

        try (HousekeepingTask task = new HousekeepingTask(repository, visitor -> {
            visitor.visit(rootId1, Tree.class);
            visitor.visit(rootId2, Blob.class);
        })) {
            Set<String> unreachableObjects = task.call();

            assertThat(unreachableObjects).containsExactlyInAnyOrder(
                    unreachableTreeId,
                    blob5Id,
                    blob4Id);
        }
    }
}
