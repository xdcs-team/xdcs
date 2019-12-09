package pl.edu.agh.xdcs.or;

import javax.enterprise.inject.Vetoed;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
@Vetoed
public class ObjectRepositoryMock implements ObjectRepository {
    private final Map<String, Object> objects = new HashMap<>();

    @Override
    public void register(ObjectRepositoryTypeHandler<?> handler) {
        throw new AssertionError();
    }

    @Override
    public ObjectLookupResult lookup(String objectId) {
        throw new AssertionError();
    }

    @Override
    public InputStream cat(String objectId) {
        throw new AssertionError();
    }

    @Override
    public <T extends ObjectBase> T cat(String objectId, Class<T> type) {
        Object obj = objects.get(objectId);
        if (!type.isInstance(obj)) {
            throw new AssertionError();
        }

        return type.cast(obj);
    }

    @Override
    public String store(ObjectBase object) {
        String id = DigestUtils.digest(UUID.randomUUID().toString().getBytes());
        objects.put(id, object);
        return id;
    }

    @Override
    public String store(InputStream object) {
        throw new AssertionError();
    }

    @Override
    public void validate(String objectId, Class<? extends ObjectBase> type) throws ObjectRepositoryValidationException {
        throw new AssertionError();
    }

    @Override
    public void verifyChecksums() {
        throw new AssertionError();
    }

    @Override
    public Future<Set<String>> runConsistencyCheck(RootProvider rootProvider) {
        throw new AssertionError();
    }

    @Override
    public void checkConsistency(RootProvider rootProvider) {
        throw new AssertionError();
    }

    @Override
    public Future<Set<String>> runHousekeeping(RootProvider rootProvider) {
        throw new AssertionError();
    }

    @Override
    public Stream<String> allObjects() {
        return objects.keySet().stream();
    }

    @Override
    public Path root() {
        throw new AssertionError();
    }

    @Override
    public <T extends ObjectBase> Stream<ObjectKey> dependenciesFor(String objectId, Class<T> type) {
        throw new AssertionError();
    }
}
