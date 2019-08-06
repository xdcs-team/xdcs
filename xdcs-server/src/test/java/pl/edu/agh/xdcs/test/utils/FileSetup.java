package pl.edu.agh.xdcs.test.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * @author Kamil Jarosz
 */
public class FileSetup {
    public static Path setUpDirectory(Object requester) throws IOException {
        return Files.createTempDirectory("xdcs_" + requester.getClass().getName() + "_");
    }

    public static void tearDownDirectory(Path dir) throws IOException {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static Path setUpFile(Object requester) throws IOException {
        return Files.createTempFile("xdcs_" + requester.getClass().getName() + "_", "");
    }

    public static void tearDownFile(Path file) throws IOException {
        Files.delete(file);
    }
}
