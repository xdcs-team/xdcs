package pl.edu.agh.xdcs.or.types;

import com.google.common.io.ByteStreams;
import pl.edu.agh.xdcs.or.ObjectRepositoryTypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Kamil Jarosz
 */
public class BlobTypeHandler implements ObjectRepositoryTypeHandler<Blob> {
    @Override
    public String getTypeName() {
        return Blob.TYPE_NAME;
    }

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
}
