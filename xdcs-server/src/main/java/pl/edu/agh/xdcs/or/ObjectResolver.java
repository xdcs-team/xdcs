package pl.edu.agh.xdcs.or;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used for resolving objects in the given directory. This class enforces
 * the objects file structure.
 *
 * @author Kamil Jarosz
 */
class ObjectResolver {
    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("[0-9a-f]{3,40}");
    private static final Pattern OBJECT_FILE_PATTERN = Pattern.compile("[0-9a-f]{38}");
    private static final Pattern OBJECT_BUCKET_PATTERN = Pattern.compile("[0-9a-f]{2}");

    private final Path objectsDir;

    ObjectResolver(Path objectsDir) {
        this.objectsDir = objectsDir;
    }

    static boolean validObjectId(String objectId, boolean allowPartial) {
        Objects.requireNonNull(objectId);
        if (!OBJECT_ID_PATTERN.matcher(objectId).matches()) {
            return false;
        }

        return allowPartial || objectId.length() == 40;
    }

    /**
     * Resolve object path with the given object ID. This will check whether the object exists.
     * Partial IDs may be passed as it will automatically resolve to an existing object.
     *
     * @throws AmbiguousObjectIdentifierException     when the given object ID is ambiguous
     * @throws InvalidObjectIdentifierException       when the object ID is ill-formed
     * @throws ObjectRepositoryInconsistencyException when the object doesn't exist
     */
    Path resolve(String objectId) {
        if (!validObjectId(objectId, true)) {
            throw new InvalidObjectIdentifierException(objectId);
        }

        int len = objectId.length();
        String hi = objectId.substring(0, 2);
        String low = objectId.substring(2);

        if (len == 40) {
            // full ID
            Path resolved = objectsDir.resolve(hi)
                    .resolve(low);

            if (!Files.isRegularFile(resolved)) {
                throw new ObjectRepositoryInconsistencyException(objectId);
            }

            return resolved;
        }

        Path parent = objectsDir.resolve(hi);
        if (!Files.isDirectory(parent)) {
            throw new ObjectRepositoryInconsistencyException(objectId);
        }

        try {
            List<Path> possibilities = Files.list(parent)
                    .filter(f -> f.getFileName().toString().length() == 38)
                    .filter(f -> f.getFileName().toString().startsWith(low))
                    .collect(Collectors.toList());

            if (possibilities.size() > 1) {
                List<String> ambiguousIdentifiers = possibilities.stream()
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.toList());
                throw new AmbiguousObjectIdentifierException(objectId, ambiguousIdentifiers);
            }

            if (possibilities.isEmpty()) {
                throw new ObjectRepositoryInconsistencyException(objectId);
            }

            return parent.resolve(possibilities.get(0));
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    /**
     * Resolve object path with the given object ID. This will not check whether the object exists,
     * so a full ID needs to be passed.
     *
     * @throws InvalidObjectIdentifierException when the object ID is ill-formed or partial
     */
    Path resolveFullNoCheck(String objectId) {
        if (!validObjectId(objectId, false)) {
            throw new InvalidObjectIdentifierException(objectId);
        }

        String hi = objectId.substring(0, 2);
        String low = objectId.substring(2);
        return objectsDir.resolve(hi)
                .resolve(low);
    }

    Stream<String> allObjects() {
        try {
            return Files.walk(objectsDir, 2)
                    .filter(path -> path.getParent().getParent().equals(objectsDir))
                    .filter(path -> OBJECT_FILE_PATTERN.matcher(path.getFileName().toString()).matches())
                    .filter(path -> OBJECT_BUCKET_PATTERN.matcher(path.getParent().getFileName().toString()).matches())
                    .map(path -> path.getParent().getFileName().toString() + path.getFileName().toString());
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }
}
