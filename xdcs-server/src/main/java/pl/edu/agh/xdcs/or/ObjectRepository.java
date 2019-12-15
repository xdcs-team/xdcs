package pl.edu.agh.xdcs.or;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public interface ObjectRepository {
    static ObjectRepository forPath(Path root) {
        return new FSObjectRepository(root);
    }

    static boolean validObjectId(String objectId, boolean allowPartial) {
        return ObjectResolver.validObjectId(objectId, allowPartial);
    }

    void register(ObjectRepositoryTypeHandler<?> handler);

    ObjectLookupResult lookup(String objectId);

    InputStream cat(String objectId);

    <T extends ObjectBase> T cat(String objectId, Class<T> type);

    String store(ObjectBase object);

    String store(InputStream object);

    void validate(String objectId, Class<? extends ObjectBase> type)
            throws ObjectRepositoryValidationException;

    /**
     * @throws InterruptedException          when verification has been interrupted
     * @throws ChecksumVerificationException when verification failed
     */
    void verifyChecksums() throws InterruptedException, ChecksumVerificationException;

    /**
     * Run consistency check. The returned future will provide a set of objects
     * that are required but missing from the object repository.
     *
     * @param rootProvider provider for object roots
     * @return future returning a set of missing objects
     */
    Future<Set<String>> runConsistencyCheck(RootProvider rootProvider);

    /**
     * @param rootProvider provider for object roots
     * @throws InterruptedException            when consistency check has been interrupted
     * @throws ConsistencyCheckFailedException when consistency check failed
     */
    void checkConsistency(RootProvider rootProvider) throws InterruptedException, ConsistencyCheckFailedException;

    /**
     * Run housekeeping. The returned future will provide a set of objects
     * that are unreachable by the given roots and may be safely deleted.
     *
     * @param rootProvider provider for object roots
     * @return future returning a set of unreachable objects
     */
    Future<Set<String>> runHousekeeping(RootProvider rootProvider);

    Stream<String> allObjects();

    /**
     * @return root path for this object repository
     */
    Path root();

    /**
     * @param objectId object to get the dependencies from
     * @return set of objects that the given object depends on
     */
    <T extends ObjectBase> Stream<ObjectKey> dependenciesFor(String objectId, Class<T> type);

    enum ObjectLookupResult {
        EXISTS,
        AMBIGUOUS,
        ABSENT,
        ;
    }
}
