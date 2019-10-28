package pl.edu.agh.xdcs.or.types;

import pl.edu.agh.xdcs.or.ObjectBase;
import pl.edu.agh.xdcs.or.ObjectRepositoryIOException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamil Jarosz
 */
public class BlobStream implements ObjectBase, AutoCloseable {
    private final InputStream is;

    BlobStream(InputStream is) {
        this.is = is;
    }

    public static BlobStream from(InputStream is) {
        return new BlobStream(is);
    }

    public InputStream getStream() {
        return is;
    }

    @Override
    public void close() {
        try {
            is.close();
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }
}
