package pl.edu.agh.xdcs.or;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepositoryValidationException extends ObjectRepositoryException {
    public ObjectRepositoryValidationException(
            String objectId, Class<? extends ObjectBase> type, Throwable cause) {
        super("Invalid object type: " + objectId + " is not of type " + type.getName(), cause);
    }
}
