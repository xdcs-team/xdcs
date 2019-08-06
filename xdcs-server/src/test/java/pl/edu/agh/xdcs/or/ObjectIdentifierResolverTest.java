package pl.edu.agh.xdcs.or;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.agh.xdcs.test.utils.FileSetup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Kamil Jarosz
 */
class ObjectIdentifierResolverTest {
    private ObjectIdentifierResolver resolver;
    private Path objectsDir;

    @BeforeEach
    void setUp() throws IOException {
        objectsDir = FileSetup.setUpDirectory(this);
        resolver = new ObjectIdentifierResolver(objectsDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSetup.tearDownDirectory(objectsDir);
    }

    @Test
    void testFullResolve() throws IOException {
        Files.createDirectories(objectsDir.resolve("12"));
        Path path = objectsDir.resolve("12/34567890abcdef1234567890abcdef12345678");
        Files.write(path, "a".getBytes());

        assertThat(resolver.resolve("1234567890abcdef1234567890abcdef12345678"))
                .isEqualTo(path);
    }

    @Test
    void testInvalidInput() {
        assertThatThrownBy(() -> resolver.resolve("ab"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolve("abcg"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolve("ABCDE"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolve("1234567890abcdef1234567890abcdef123456789"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
    }

    @Test
    void testAmbiguousObjects() throws IOException {
        Files.createDirectories(objectsDir.resolve("12"));
        Files.write(objectsDir.resolve("12/34567890abcdef1234567890abcdef12345678"), "a".getBytes());
        Files.write(objectsDir.resolve("12/34567890abcdef1234567890abcdef12345679"), "b".getBytes());

        assertThatThrownBy(() -> resolver.resolve("123"))
                .isInstanceOf(AmbiguousObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolve("1234567890abcdef123"))
                .isInstanceOf(AmbiguousObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolve("1234567890abcdef1234567890abcdef1234567"))
                .isInstanceOf(AmbiguousObjectIdentifierException.class);
    }

    @Test
    void testInvalidObjects() throws IOException {
        Files.createDirectories(objectsDir.resolve("12"));
        Files.write(objectsDir.resolve("12/34567890abcdef1234567890abcdef123456789abc"), "a".getBytes());

        assertThatThrownBy(() -> resolver.resolve("123"))
                .isInstanceOf(ObjectRepositoryInconsistencyException.class);
        assertThatThrownBy(() -> resolver.resolve("1234567890abcdef123"))
                .isInstanceOf(ObjectRepositoryInconsistencyException.class);
        assertThatThrownBy(() -> resolver.resolve("1234567890abcdef1234567890abcdef12"))
                .isInstanceOf(ObjectRepositoryInconsistencyException.class);
    }

    @Test
    void testNotFound() {
        assertThatThrownBy(() -> resolver.resolve("123"))
                .isInstanceOf(ObjectRepositoryInconsistencyException.class);
        assertThatThrownBy(() -> resolver.resolve("1234567890abcdef123"))
                .isInstanceOf(ObjectRepositoryInconsistencyException.class);
        assertThatThrownBy(() -> resolver.resolve("1234567890abcdef1234567890abcdef12345678"))
                .isInstanceOf(ObjectRepositoryInconsistencyException.class);
    }

    @Test
    void testPartialResolve() throws IOException {
        Files.createDirectories(objectsDir.resolve("12"));
        Path path = objectsDir.resolve("12/34567890abcdef1234567890abcdef12345678");
        Files.write(path, "a".getBytes());

        assertThat(resolver.resolve("123"))
                .isEqualTo(path);
        assertThat(resolver.resolve("1234567890abcdef12345"))
                .isEqualTo(path);
        assertThat(resolver.resolve("1234567890abcdef1234567890abcdef1234567"))
                .isEqualTo(path);
    }

    @Test
    void testFullResolveNoCheck() {
        Path path = objectsDir.resolve("12/34567890abcdef1234567890abcdef12345678");
        assertThat(resolver.resolveFullNoCheck("1234567890abcdef1234567890abcdef12345678"))
                .isEqualTo(path);
    }

    @Test
    void testPartialResolveNoCheck() {
        assertThatThrownBy(() -> resolver.resolveFullNoCheck("1234567890abcdef1234567890abcdef1234567"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolveFullNoCheck("1234"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolveFullNoCheck("aer"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
        assertThatThrownBy(() -> resolver.resolveFullNoCheck("1234567890abcdef1234567890abcdef1234567890"))
                .isInstanceOf(InvalidObjectIdentifierException.class);
    }
}
