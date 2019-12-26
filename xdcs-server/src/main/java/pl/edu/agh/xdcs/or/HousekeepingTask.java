package pl.edu.agh.xdcs.or;

import com.google.common.collect.Sets;
import pl.edu.agh.xdcs.or.ObjectRepository.ObjectLookupResult;
import pl.edu.agh.xdcs.util.DeletingFileVisitor;
import pl.edu.agh.xdcs.util.LockFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
class HousekeepingTask implements Callable<Set<String>>, AutoCloseable {
    private final ObjectRepository objectRepository;
    private final RootProvider rootProvider;
    private final ObjectResolver reachableObjectsResolver;
    private final Path reachableObjectsDir;

    HousekeepingTask(ObjectRepository objectRepository, RootProvider rootProvider) {
        this.objectRepository = objectRepository;
        this.rootProvider = rootProvider;
        this.reachableObjectsDir = objectRepository.root().resolve(".reachable");
        this.reachableObjectsResolver = new ObjectResolver(reachableObjectsDir);
    }

    @Override
    public Set<String> call() {
        Path lockFile = objectRepository.root().resolve(".housekeeping-lock");
        try (LockFile lf = LockFile.newLockFile(lockFile)) {
            Files.createDirectories(this.reachableObjectsDir);

            Set<String> allObjects = objectRepository.allObjects().collect(Collectors.toSet());
            rootProvider.provideRoots(HousekeepingTask.this::markReachable);

            Set<String> reachableObjects = reachableObjectsResolver.allObjects().collect(Collectors.toSet());
            return Sets.difference(allObjects, reachableObjects);
        } catch (LockFile.LockFailedException e) {
            throw new IllegalStateException("Housekeeping is already running", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void markReachable(String objectId, Class<? extends ObjectBase> type) {
        if (objectRepository.lookup(objectId) != ObjectLookupResult.EXISTS) {
            throw new ObjectRepositoryInconsistencyException("Missing object: " + objectId);
        }

        try {
            Path path = reachableObjectsResolver.resolveFullNoCheck(objectId);
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        objectRepository.dependenciesFor(objectId, type)
                .forEach(key -> markReachable(key.getObjectId(), key.getType()));
    }

    @Override
    public void close() {
        try {
            Files.walkFileTree(reachableObjectsDir, new DeletingFileVisitor());
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }
}
