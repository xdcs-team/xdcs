package pl.edu.agh.xdcs.or;

/**
 * An interface used to inject logic which retrieves root object IDs.
 * It may be used for example for consistency checks or housekeeping.
 *
 * @author Kamil Jarosz
 */
public interface RootProvider {
    /**
     * This method upon execution should visit all root object IDs.
     * Duplicate IDs are allowed.
     *
     * @param visitor a visitor used to visit object IDs
     */
    void provideRoots(RootVisitor visitor);

    interface RootVisitor {
        void visit(String rootId, Class<? extends ObjectBase> type);
    }
}
