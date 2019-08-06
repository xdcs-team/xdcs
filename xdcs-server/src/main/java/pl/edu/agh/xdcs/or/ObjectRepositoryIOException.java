package pl.edu.agh.xdcs.or;

import java.io.IOException;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryIOException extends ObjectRepositoryException {
    public ObjectRepositoryIOException(String objectId, IOException e) {
        super("IO error occurred while reading " + objectId, e);
    }

    public ObjectRepositoryIOException(IOException e) {
        super("IO error occurred while dealing with the filesystem", e);
    }
}
