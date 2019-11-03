package pl.edu.agh.xdcs.workspace;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.attribute.PosixFilePermission;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
@Getter
@Builder
public class FileDescription {
    private FileType type;

    private List<Entry> children;

    private Set<PosixFilePermission> permissions;

    public enum FileType {
        REGULAR,
        DIRECTORY,
        SYMLINK,
    }

    @Getter
    @Builder
    public static class Entry {
        public static final Comparator<Entry> DIRECTORIES_FIRST = Comparator.comparing(
                entry -> entry.type == FileType.DIRECTORY ? 0 : 1);

        private String name;
        private FileType type;
        private Set<PosixFilePermission> permissions;
    }
}
