package pl.edu.agh.xdcs.or;

import pl.edu.agh.xdcs.util.ConcurrentUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
public class ObjectRepository {
    private final Map<Class<?>, ObjectRepositoryTypeHandler<?>> handlers = new HashMap<>();
    private final Path root;
    private final ObjectResolver resolver;

    private ObjectRepository(Path root) {
        this.root = root;
        this.resolver = new ObjectResolver(getObjectsDir());

        try {
            Files.createDirectories(getObjectsDir());
            Files.createDirectories(getTempDir());
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    public static ObjectRepository forPath(Path root) {
        return new ObjectRepository(root);
    }

    public static boolean validObjectId(String objectId, boolean allowPartial) {
        return ObjectResolver.validObjectId(objectId, allowPartial);
    }

    private Path getObjectsDir() {
        return root;
    }

    private Path getTempDir() {
        return root.resolve("temp");
    }

    public void register(ObjectRepositoryTypeHandler<?> handler) {
        handlers.put(handler.getRepresentation(), handler);
    }

    public ObjectLookupResult lookup(String objectId) {
        try {
            resolver.resolve(objectId);
            return ObjectLookupResult.EXISTS;
        } catch (ObjectRepositoryInconsistencyException e) {
            return ObjectLookupResult.ABSENT;
        } catch (AmbiguousObjectIdentifierException e) {
            return ObjectLookupResult.AMBIGUOUS;
        }
    }

    public InputStream cat(String objectId) {
        Path objectPath = resolver.resolve(objectId);
        try {
            return Files.newInputStream(objectPath);
        } catch (NoSuchFileException e) {
            throw new ObjectRepositoryInconsistencyException(objectId, e);
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(objectId, e);
        }
    }

    public <T extends ObjectBase> T cat(String objectId, Class<T> type) {
        ObjectRepositoryTypeHandler<T> handler = getHandler(type);
        InputStream is = cat(objectId);
        try {
            try {
                return handler.read(is);
            } finally {
                if (handler.closeAfterRead()) {
                    is.close();
                }
            }
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(objectId, e);
        }
    }

    public String store(ObjectBase object) {
        return store0(object);
    }

    @SuppressWarnings("unchecked")
    private <T extends ObjectBase> String store0(T object) {
        ObjectRepositoryTypeHandler<T> handler = getHandler((Class<T>) object.getClass());

        try {
            String now = LocalDateTime.now().toString();
            Path temporaryPath = Files.createTempFile(getTempDir(), now + "_", "");

            try (OutputStream os = Files.newOutputStream(temporaryPath)) {
                handler.write(object, os);
            }

            String objectId;
            try (InputStream is = Files.newInputStream(temporaryPath)) {
                objectId = DigestUtils.digest(is);
            }

            Path targetPath = resolver.resolveFullNoCheck(objectId);
            Files.createDirectories(targetPath.getParent());
            Files.move(temporaryPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            return objectId;
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ObjectBase> ObjectRepositoryTypeHandler<T> getHandler(Class<T> type) {
        ObjectRepositoryTypeHandler<?> handler = handlers.get(type);
        if (handler == null) {
            throw new MissingHandlerException(type);
        }

        return (ObjectRepositoryTypeHandler<T>) handler;
    }

    /**
     * Run consistency check. The returned future will provide a set of objects
     * that are required but missing from the object repository.
     *
     * @param rootProvider provider for object roots
     * @return future returning a set of missing objects
     */
    public Future<Set<String>> runConsistencyCheck(RootProvider rootProvider) {
        try (ConsistencyCheckTask task = new ConsistencyCheckTask(this, rootProvider)) {
            return ConcurrentUtils.applicationExecutorService()
                    .submit(task);
        }
    }

    /**
     * @param rootProvider provider for object roots
     * @throws InterruptedException            when consistency check has been interrupted
     * @throws ConsistencyCheckFailedException when consistency check failed
     */
    public void checkConsistency(RootProvider rootProvider) throws InterruptedException, ConsistencyCheckFailedException {
        try {
            Set<String> missingObjects = runConsistencyCheck(rootProvider).get();
            if (!missingObjects.isEmpty()) {
                throw new ConsistencyCheckFailedException("Missing objects: " + missingObjects);
            }
        } catch (ExecutionException e) {
            throw new ConsistencyCheckFailedException(e);
        }
    }

    /**
     * Run housekeeping. The returned future will provide a set of objects
     * that are unreachable by the given roots and may be safely deleted.
     *
     * @param rootProvider provider for object roots
     * @return future returning a set of unreachable objects
     */
    public Future<Set<String>> runHousekeeping(RootProvider rootProvider) {
        try (HousekeepingTask task = new HousekeepingTask(this, rootProvider)) {
            return ConcurrentUtils.applicationExecutorService()
                    .submit(task);
        }
    }

    public Stream<String> allObjects() {
        return resolver.allObjects();
    }

    /**
     * @return root path for this object repository
     */
    public Path root() {
        return root;
    }

    /**
     * @param objectId object to get the dependencies from
     * @return set of objects that the given object depends on
     */
    public <T extends ObjectBase> Stream<ObjectKey> dependenciesFor(String objectId, Class<T> type) {
        ObjectRepositoryTypeHandler<T> handler = getHandler(type);
        return handler.dependencies(cat(objectId, type));
    }

    public enum ObjectLookupResult {
        EXISTS,
        AMBIGUOUS,
        ABSENT,
        ;
    }
}
