package pl.edu.agh.xdcs.or;

/**
 * @author Kamil Jarosz
 */
public class InvalidObjectIdentifierException extends ObjectRepositoryException {
    public InvalidObjectIdentifierException(String objectId) {
        super("Object ID " + objectId + " is invalid");
    }
}
