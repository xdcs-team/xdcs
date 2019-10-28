package pl.edu.agh.xdcs.or;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public interface ObjectRepositoryTypeHandler<T> {
    Class<T> getRepresentation();

    T read(InputStream file) throws IOException;

    void write(T object, OutputStream file) throws IOException;

    default boolean closeAfterRead() {
        return true;
    }

    Stream<ObjectKey> dependencies(T object);
}
