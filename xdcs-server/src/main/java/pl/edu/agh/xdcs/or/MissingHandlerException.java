package pl.edu.agh.xdcs.or;

/**
 * @author Kamil Jarosz
 */
public class MissingHandlerException extends ObjectRepositoryException {
    public MissingHandlerException(Class<? extends ObjectBase> type) {
        super("Handler for type " + type + " is missing");
    }
}
