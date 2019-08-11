package pl.edu.agh.xdcs.or.types;

import pl.edu.agh.xdcs.or.ObjectBase;

import java.io.InputStream;

/**
 * @author Kamil Jarosz
 */
public class BlobStream implements ObjectBase {
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
}
