package pl.edu.agh.xdcs.or;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryInconsistencyException extends ObjectRepositoryException {
    public ObjectRepositoryInconsistencyException(String objectId) {
        super("Inconsistency: object " + objectId + " doesn't exist");
    }

    public ObjectRepositoryInconsistencyException(String objectId, Throwable cause) {
        super("Inconsistency: object " + objectId + " doesn't exist", cause);
    }
}
