package pl.edu.agh.xdcs.or;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Kamil Jarosz
 */
class ObjectIdentifierResolver {
    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("[0-9a-f]{3,40}");
    private final Path objectsDir;

    ObjectIdentifierResolver(Path objectsDir) {
        this.objectsDir = objectsDir;
    }

    static boolean validObjectId(String objectId, boolean allowPartial) {
        Objects.requireNonNull(objectId);
        if (!OBJECT_ID_PATTERN.matcher(objectId).matches()) {
            return false;
        }

        return allowPartial || objectId.length() == 40;
    }

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

            if (possibilities.size() == 0) {
                throw new ObjectRepositoryInconsistencyException(objectId);
            }

            return parent.resolve(possibilities.get(0));
        } catch (IOException e) {
            throw new ObjectRepositoryIOException(e);
        }
    }

    Path resolveFullNoCheck(String objectId) {
        if (!validObjectId(objectId, false)) {
            throw new InvalidObjectIdentifierException(objectId);
        }

        String hi = objectId.substring(0, 2);
        String low = objectId.substring(2);
        return objectsDir.resolve(hi)
                .resolve(low);
    }
}
