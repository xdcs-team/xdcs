package pl.edu.agh.xdcs.or;

import pl.edu.agh.xdcs.or.ObjectRepository.ObjectLookupResult;
import pl.edu.agh.xdcs.util.DeletingFileVisitor;
import pl.edu.agh.xdcs.util.LockFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author Kamil Jarosz
 */
class ConsistencyCheckTask implements Callable<Set<String>>, AutoCloseable {
    private final ObjectRepository objectRepository;
    private final RootProvider rootProvider;
    private final ObjectResolver checkedObjectsResolver;
    private final Path checkedObjectsDir;

    ConsistencyCheckTask(ObjectRepository objectRepository, RootProvider rootProvider) {
        this.objectRepository = objectRepository;
        this.rootProvider = rootProvider;
        this.checkedObjectsDir = objectRepository.root().resolve(".consistency-checked");
        this.checkedObjectsResolver = new ObjectResolver(checkedObjectsDir);
    }

    @Override
    public Set<String> call() {
        Path lockFile = objectRepository.root().resolve(".consistency-check-lock");
        try (LockFile lf = LockFile.newLockFile(lockFile)) {
            Set<String> missingObjects = new HashSet<>();
            rootProvider.provideRoots((rootId, type) ->
                    ConsistencyCheckTask.this.checkConsistency(rootId, type)
                            .forEach(missingObjects::add));
            return missingObjects;
        } catch (LockFile.LockFailedException e) {
            throw new IllegalStateException("Consistency check is already running", e);
        }
    }

    private <T extends ObjectBase> Stream<String> checkConsistency(String objectId, Class<T> type) {
        if (consistencyChecked(objectId)) {
            return Stream.empty();
        }
        markChecked(objectId);

        if (objectRepository.lookup(objectId) != ObjectLookupResult.EXISTS) {
            return Stream.of(objectId);
        }

        return objectRepository.dependenciesFor(objectId, type)
                .flatMap(key -> checkConsistency(key.getObjectId(), key.getType()));
    }

    private boolean consistencyChecked(String objectId) {
        return Files.exists(checkedObjectsResolver.resolveFullNoCheck(objectId));
    }

    private void markChecked(String objectId) {
        try {
            Path path = checkedObjectsResolver.resolveFullNoCheck(objectId);
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            Files.walkFileTree(checkedObjectsDir, new DeletingFileVisitor());
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }
}
