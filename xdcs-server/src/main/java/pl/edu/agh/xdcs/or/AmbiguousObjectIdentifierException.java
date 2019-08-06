package pl.edu.agh.xdcs.or;

import java.util.List;

/**
 * @author Kamil Jarosz
 */
public class AmbiguousObjectIdentifierException extends ObjectRepositoryException {
    public AmbiguousObjectIdentifierException(String objectId, List<String> ambiguousIdentifiers) {
        super("Object ID " + objectId + " is ambiguous, possibilities: " + String.join(", ", ambiguousIdentifiers));
    }
}
