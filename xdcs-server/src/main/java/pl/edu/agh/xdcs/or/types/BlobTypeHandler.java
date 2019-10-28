package pl.edu.agh.xdcs.or.types;

import com.google.common.io.ByteStreams;
import pl.edu.agh.xdcs.or.ObjectKey;
import pl.edu.agh.xdcs.or.ObjectRepositoryTypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
@GlobalTypeHandler
public class BlobTypeHandler implements ObjectRepositoryTypeHandler<Blob> {
    @Override
    public Class<Blob> getRepresentation() {
        return Blob.class;
    }

    @Override
    public Blob read(InputStream file) throws IOException {
        return new Blob(ByteStreams.toByteArray(file));
    }

    @Override
    public void write(Blob object, OutputStream file) throws IOException {
        file.write(object.getData());
    }

    @Override
    public Stream<ObjectKey> dependencies(Blob object) {
        return Stream.empty();
    }
}
