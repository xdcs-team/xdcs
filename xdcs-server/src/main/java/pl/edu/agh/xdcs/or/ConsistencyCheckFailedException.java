package pl.edu.agh.xdcs.or;

/**
 * @author Kamil Jarosz
 */
public class ConsistencyCheckFailedException extends RuntimeException {
    public ConsistencyCheckFailedException(String message) {
        super(message);
    }

    public ConsistencyCheckFailedException(Throwable cause) {
        super(cause);
    }
}
