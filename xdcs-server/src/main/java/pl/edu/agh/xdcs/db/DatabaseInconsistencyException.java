package pl.edu.agh.xdcs.db;

/**
 * @author Kamil Jarosz
 */
public class DatabaseInconsistencyException extends RuntimeException {
    public DatabaseInconsistencyException(String message) {
        super(message);
    }
}
