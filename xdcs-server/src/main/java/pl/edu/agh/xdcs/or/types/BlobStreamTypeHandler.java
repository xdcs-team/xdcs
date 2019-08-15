package pl.edu.agh.xdcs.or.types;

import com.google.common.io.ByteStreams;
import pl.edu.agh.xdcs.or.ObjectRepositoryTypeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Kamil Jarosz
 */
@GlobalTypeHandler
public class BlobStreamTypeHandler implements ObjectRepositoryTypeHandler<BlobStream> {
    @Override
    public Class<BlobStream> getRepresentation() {
        return BlobStream.class;
    }

    @Override
    public BlobStream read(InputStream file) {
        return new BlobStream(file);
    }

    @Override
    public void write(BlobStream object, OutputStream file) throws IOException {
        ByteStreams.copy(object.getStream(), file);
    }

    @Override
    public boolean closeAfterRead() {
        return false;
    }
}
