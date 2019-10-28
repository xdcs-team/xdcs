package pl.edu.agh.xdcs.test.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import pl.edu.agh.xdcs.or.ObjectRepository;
import pl.edu.agh.xdcs.or.types.BlobTypeHandler;
import pl.edu.agh.xdcs.or.types.TreeTypeHandler;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryTestBase {
    protected Path root;
    protected ObjectRepository repository;

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
}
