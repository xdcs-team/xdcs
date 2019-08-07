package pl.edu.agh.xdcs.or;

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

/**
 * @author Kamil Jarosz
 */
public class ObjectRepository {
    private final Map<Class<?>, ObjectRepositoryTypeHandler<?>> handlers = new HashMap<>();
    private final Path root;
    private final ObjectIdentifierResolver resolver;

    private ObjectRepository(Path root) {
        this.root = root;
        this.resolver = new ObjectIdentifierResolver(getObjectsDir());

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
        return ObjectIdentifierResolver.validObjectId(objectId, allowPartial);
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

    public <T extends ObjectBase> T cat(String objectId, Class<T> type) {
        Path objectPath = resolver.resolve(objectId);
        ObjectRepositoryTypeHandler<T> handler = getHandler(type);
        try (InputStream is = Files.newInputStream(objectPath)) {
            return handler.read(is);
        } catch (NoSuchFileException e) {
            throw new ObjectRepositoryInconsistencyException(objectId, e);
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

    public enum ObjectLookupResult {
        EXISTS,
        AMBIGUOUS,
        ABSENT,
        ;
    }
}
