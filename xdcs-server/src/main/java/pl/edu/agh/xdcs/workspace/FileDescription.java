package pl.edu.agh.xdcs.workspace;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class FileDescription {
    private FileType type;

    private List<String> children;

    private Set<PosixFilePermission> permissions;

    public enum FileType {
        REGULAR,
        DIRECTORY,
        SYMLINK,
    }
}
