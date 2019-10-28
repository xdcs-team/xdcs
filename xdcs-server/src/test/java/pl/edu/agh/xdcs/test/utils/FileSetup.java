package pl.edu.agh.xdcs.test.utils;

import pl.edu.agh.xdcs.util.DeletingFileVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Kamil Jarosz
 */
public class FileSetup {
    public static Path setUpDirectory(Object requester) throws IOException {
        return Files.createTempDirectory("xdcs_" + requester.getClass().getName() + "_");
    }

    public static void tearDownDirectory(Path dir) throws IOException {
        Files.walkFileTree(dir, new DeletingFileVisitor());
    }

    public static Path setUpFile(Object requester) throws IOException {
        return Files.createTempFile("xdcs_" + requester.getClass().getName() + "_", "");
    }

    public static void tearDownFile(Path file) throws IOException {
        Files.delete(file);
    }
}
