package pl.edu.agh.xdcs.or;

import org.apache.commons.io.IOUtils;
import pl.edu.agh.xdcs.util.ConcurrentUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
class FSObjectRepository implements ObjectRepository {
    private final Map<Class<?>, ObjectRepositoryTypeHandler<?>> handlers = new HashMap<>();
    private final Path root;
    private final ObjectResolver resolver;

    FSObjectRepository(Path root) {
        this.root = root;
        this.resolver = new ObjectResolver(getObjectsDir());

        try {
            Files.createDirectories(getObjectsDir());
            Files.createDirectories(getTempDir());
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    private Path getObjectsDir() {
        return root;
    }

    private Path getTempDir() {
        return root.resolve("temp");
    }

    @Override
    public void register(ObjectRepositoryTypeHandler<?> handler) {
        handlers.put(handler.getRepresentation(), handler);
    }

    @Override
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

    @Override
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

    @Override
    public <T extends ObjectBase> T cat(String objectId, Class<T> type) {
        return cat0(objectId, type, false);
    }

    private <T extends ObjectBase> T cat0(String objectId, Class<T> type, boolean forceClose) {
        ObjectRepositoryTypeHandler<T> handler = getHandler(type);
        InputStream is = cat(objectId);
        try {
            try {
                return handler.read(is);
            } finally {
                if (handler.closeAfterRead() || forceClose) {
                    is.close();
                }
            }
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(objectId, e);
        }
    }

    @Override
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

            return store(temporaryPath);
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    @Override
    public String store(InputStream object) {
        try {
            String now = LocalDateTime.now().toString();
            Path temporaryPath = Files.createTempFile(getTempDir(), now + "_", "");

            try (OutputStream os = Files.newOutputStream(temporaryPath)) {
                IOUtils.copy(object, os);
            }

            return store(temporaryPath);
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    @Override
    public void validate(String objectId, Class<? extends ObjectBase> type) throws ObjectRepositoryValidationException {
        try {
            cat0(objectId, type, true);
        } catch (Exception e) {
            throw new ObjectRepositoryValidationException(objectId, type, e);
        }

        dependenciesFor(objectId, type).forEach(key -> {
            validate(key.getObjectId(), key.getType());
        });
    }

    private String store(Path temporaryPath) throws IOException {
        String objectId;
        try (InputStream is = Files.newInputStream(temporaryPath)) {
            objectId = DigestUtils.digest(is);
        }

        Path targetPath = resolver.resolveFullNoCheck(objectId);
        Files.createDirectories(targetPath.getParent());
        Files.move(temporaryPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
        return objectId;
    }

    @SuppressWarnings("unchecked")
    private <T extends ObjectBase> ObjectRepositoryTypeHandler<T> getHandler(Class<T> type) {
        ObjectRepositoryTypeHandler<?> handler = handlers.get(type);
        if (handler == null) {
            throw new MissingHandlerException(type);
        }

        return (ObjectRepositoryTypeHandler<T>) handler;
    }

    private boolean verifyChecksum(String objectId) {
        try (InputStream cat = cat(objectId)) {
            return DigestUtils.digest(cat).equals(objectId);
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    @Override
    public void verifyChecksums() throws InterruptedException, ChecksumVerificationException {
        List<Future<String>> futures = new ArrayList<>();

        allObjects().forEach(objectId -> {
            Future<String> future = ConcurrentUtils.applicationExecutorService()
                    .submit(() -> verifyChecksum(objectId) ? null : objectId);
            futures.add(future);
        });

        Set<String> invalidObjects = new HashSet<>();
        try {
            for (Future<String> future : futures) {
                String invalidObject = future.get();
                if (invalidObject != null) {
                    invalidObjects.add(invalidObject);
                }
            }
        } catch (ExecutionException e) {
            throw new ChecksumVerificationException(e);
        }

        if (!invalidObjects.isEmpty()) {
            throw new ChecksumVerificationException("Invalid objects: " + invalidObjects);
        }
    }

    @Override
    public Future<Set<String>> runConsistencyCheck(RootProvider rootProvider) {
        try (ConsistencyCheckTask task = new ConsistencyCheckTask(this, rootProvider)) {
            return ConcurrentUtils.applicationExecutorService()
                    .submit(task);
        }
    }

    @Override
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

    @Override
    public Future<Set<String>> runHousekeeping(RootProvider rootProvider) {
        try (HousekeepingTask task = new HousekeepingTask(this, rootProvider)) {
            return ConcurrentUtils.applicationExecutorService()
                    .submit(task);
        }
    }

    @Override
    public Stream<String> allObjects() {
        return resolver.allObjects();
    }

    @Override
    public Path root() {
        return root;
    }

    @Override
    public <T extends ObjectBase> Stream<ObjectKey> dependenciesFor(String objectId, Class<T> type) {
        ObjectRepositoryTypeHandler<T> handler = getHandler(type);
        return handler.dependencies(cat(objectId, type));
    }
}
