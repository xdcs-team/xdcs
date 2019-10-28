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
class ConsistencyCheckTaskTest extends ObjectRepositoryTestBase {
    @Test
    void happyPath() {
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

        try (ConsistencyCheckTask task = new ConsistencyCheckTask(repository, visitor -> {
            visitor.visit(rootId, Tree.class);
            assertThat(Files.exists(root.resolve(".consistency-checked")))
                    .isTrue();
        })) {
            Set<String> missingObjects = task.call();

            assertThat(missingObjects).isEmpty();
        }

        assertThat(Files.exists(root.resolve(".consistency-checked")))
                .isFalse();
    }

    @Test
    void missingObjects() {
        Blob blob1 = Blob.fromBytes("test blob".getBytes());

        Tree root = Tree.ofEntries(
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob1",
                        repository.store(blob1)),
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob2",
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));

        String rootId = repository.store(root);

        try (ConsistencyCheckTask task = new ConsistencyCheckTask(repository, visitor -> {
            visitor.visit(rootId, Tree.class);
        })) {
            Set<String> missingObjects = task.call();

            assertThat(missingObjects)
                    .containsExactlyInAnyOrder("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }
    }

    @Test
    void missingNotReachableObjects() {
        Blob blob1 = Blob.fromBytes("test blob".getBytes());

        Tree root = Tree.ofEntries(
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob1",
                        repository.store(blob1)));

        Tree notReachableTree = Tree.ofEntries(
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "blob2",
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));

        repository.store(notReachableTree);
        String rootId = repository.store(root);
        try (ConsistencyCheckTask task = new ConsistencyCheckTask(repository, visitor -> {
            visitor.visit(rootId, Tree.class);
        })) {
            Set<String> missingObjects = task.call();

            assertThat(missingObjects).isEmpty();
        }
    }
}
