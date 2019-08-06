package pl.edu.agh.xdcs.or;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.xdcs.test.utils.FileSetup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil Jarosz
 */
class DigestUtilsTest {
    private Path file;

    @BeforeEach
    void setUp() throws IOException {
        file = FileSetup.setUpFile(this);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSetup.tearDownFile(file);
    }

    @Test
    void testDigest1() throws IOException {
        Files.write(file, "".getBytes());

        assertThat(DigestUtils.digest(Files.newInputStream(file)))
                .isEqualTo("da39a3ee5e6b4b0d3255bfef95601890afd80709");
    }

    @Test
    void testDigest2() throws IOException {
        Files.write(file, "kamil".getBytes());

        assertThat(DigestUtils.digest(Files.newInputStream(file)))
                .isEqualTo("ab239c5a26a103f02214f1ae199f6dad0108e000");
    }
}
