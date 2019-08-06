package pl.edu.agh.xdcs.or;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.xdcs.or.types.Blob;
import pl.edu.agh.xdcs.or.types.BlobTypeHandler;
import pl.edu.agh.xdcs.or.types.Tree;
import pl.edu.agh.xdcs.or.types.TreeTypeHandler;
import pl.edu.agh.xdcs.test.utils.FileSetup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil Jarosz
 */
class ObjectRepositoryTest {
    private ObjectRepository repository;
    private Path root;

    @BeforeEach
    void setUp() throws IOException {
        root = FileSetup.setUpDirectory(this);
        repository = ObjectRepository.forPath(root);
        repository.register(new BlobTypeHandler());
        repository.register(new TreeTypeHandler());
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSetup.tearDownDirectory(root);
    }

    @Test
    void testStoreRead() throws IOException {
        Blob object = Blob.fromBytes("test content".getBytes());
        String expectedObjectId = "1eebdf4fdc9fc7bf283031b93f9aef3338de9052";

        assertThat(repository.lookup(expectedObjectId))
                .isEqualTo(ObjectRepository.ObjectLookupResult.ABSENT);

        String objectId = repository.store(object);

        assertThat(objectId)
                .isEqualTo(expectedObjectId);

        assertThat(Files.isRegularFile(root.resolve("1e/ebdf4fdc9fc7bf283031b93f9aef3338de9052")))
                .isTrue();

        // tmp directory should be empty
        assertThat(Files.list(root.resolve("temp")))
                .isEmpty();

        assertThat(repository.cat(objectId, Blob.class))
                .isEqualTo(object);

        assertThat(repository.lookup(expectedObjectId))
                .isEqualTo(ObjectRepository.ObjectLookupResult.EXISTS);
        assertThat(repository.lookup(expectedObjectId.substring(0, 10)))
                .isEqualTo(ObjectRepository.ObjectLookupResult.EXISTS);
        assertThat(repository.lookup(expectedObjectId.substring(0, 3)))
                .isEqualTo(ObjectRepository.ObjectLookupResult.EXISTS);
    }

    @Test
    void testAmbiguous() {
        Blob objectA = Blob.fromBytes("test content".getBytes());
        Blob objectB = Blob.fromBytes("561577762909847591".getBytes());
        String commonPrefix = "1ee";

        String objectAId = repository.store(objectA);
        String objectBId = repository.store(objectB);

        assertThat(objectAId)
                .isEqualTo("1eebdf4fdc9fc7bf283031b93f9aef3338de9052");
        assertThat(objectBId)
                .isEqualTo("1eeaa95e13003e62994ccc03af5f700e3548446f");

        assertThat(repository.lookup(commonPrefix))
                .isEqualTo(ObjectRepository.ObjectLookupResult.AMBIGUOUS);
    }

    @Test
    void testWriteReadTree() {
        Tree tree = Tree.ofEntries(
                Tree.Entry.of(
                        Tree.EntryMode.fromString("040777"), "test2",
                        "1eebdf4fdc9fc7bf283031b93f9aef3338de9052"),
                Tree.Entry.of(
                        Tree.EntryMode.fromString("100777"), "test",
                        "1eebdf4fdc9fc7bf283031b93f9aef3338de9052"));

        String treeId = repository.store(tree);
        String expectedJson = "[" +
                "{\"mode\":\"100777\",\"name\":\"test\",\"id\":\"1eebdf4fdc9fc7bf283031b93f9aef3338de9052\"}," +
                "{\"mode\":\"040777\",\"name\":\"test2\",\"id\":\"1eebdf4fdc9fc7bf283031b93f9aef3338de9052\"}" +
                "]";
        assertThat(treeId).isEqualTo(DigestUtils.digest(expectedJson.getBytes()));

        assertThat(repository.lookup(treeId)).isEqualTo(ObjectRepository.ObjectLookupResult.EXISTS);
        assertThat(repository.cat(treeId, Tree.class)).isEqualTo(tree);
    }
}
