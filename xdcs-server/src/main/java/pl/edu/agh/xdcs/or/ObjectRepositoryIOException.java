package pl.edu.agh.xdcs.or;

import java.io.IOException;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryIOException extends ObjectRepositoryException {
    private final IOException cause;

    public ObjectRepositoryIOException(String objectId, IOException cause) {
        super("IO error occurred while reading " + objectId, cause);
        this.cause = cause;
    }

    public ObjectRepositoryIOException(IOException cause) {
        super("IO error occurred while dealing with the filesystem", cause);
        this.cause = cause;
    }

    public IOException getIOException() {
        return cause;
    }
}
