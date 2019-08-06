package pl.edu.agh.xdcs.or;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryException extends RuntimeException {
    public ObjectRepositoryException(String message) {
        super(message);
    }

    public ObjectRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
