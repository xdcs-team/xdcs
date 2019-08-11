package pl.edu.agh.xdcs.util;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
public class FsUtils {
    public static Path resolveWithoutTraversal(Path original, String other) {
        Path resolved = original.resolve(other).normalize();
        if (!resolved.startsWith(original)) {
            throw new PathTraversalDetectedException(original, resolved);
        }
        return resolved;
    }

    public static String permissionsToString(Set<PosixFilePermission> permissions) {
        StringBuilder ret = new StringBuilder(9);
        ret.append(permissions.contains(PosixFilePermission.OWNER_READ) ? 'r' : '-');
        ret.append(permissions.contains(PosixFilePermission.OWNER_WRITE) ? 'w' : '-');
        ret.append(permissions.contains(PosixFilePermission.OWNER_EXECUTE) ? 'x' : '-');
        ret.append(permissions.contains(PosixFilePermission.GROUP_READ) ? 'r' : '-');
        ret.append(permissions.contains(PosixFilePermission.GROUP_WRITE) ? 'w' : '-');
        ret.append(permissions.contains(PosixFilePermission.GROUP_EXECUTE) ? 'x' : '-');
        ret.append(permissions.contains(PosixFilePermission.OTHERS_READ) ? 'r' : '-');
        ret.append(permissions.contains(PosixFilePermission.OTHERS_WRITE) ? 'w' : '-');
        ret.append(permissions.contains(PosixFilePermission.OTHERS_EXECUTE) ? 'x' : '-');
        return ret.toString();
    }
}
