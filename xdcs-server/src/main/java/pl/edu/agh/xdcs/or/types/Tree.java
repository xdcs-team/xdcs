package pl.edu.agh.xdcs.or.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.edu.agh.xdcs.or.ObjectBase;
import pl.edu.agh.xdcs.or.ObjectRepository;

import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kamil Jarosz
 */
@EqualsAndHashCode
public class Tree implements ObjectBase {
    private List<Entry> entries;

    private Tree(List<Entry> entries) {
        this.entries = entries;
    }

    public static Tree ofEntries(Entry... entries) {
        return ofEntries(Arrays.asList(entries));
    }

    @JsonCreator
    public static Tree ofEntries(List<Entry> entries) {
        ArrayList<Entry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing(Entry::getName));
        return new Tree(sorted);
    }

    @JsonValue
    public List<Entry> getEntries() {
        return entries;
    }

    public enum FileType {
        S_IFLNK("12"),
        S_IFREG("10"),
        S_IFDIR("04"),
        ;

        private final String val;

        FileType(String val) {
            this.val = val;
        }

        public static Optional<FileType> fromValue(String value) {
            for (FileType type : values()) {
                if (type.getValue().equals(value)) {
                    return Optional.of(type);
                }
            }

            return Optional.empty();
        }

        public String getValue() {
            return val;
        }
    }

    @SuppressWarnings("OctalInteger")
    @EqualsAndHashCode
    public static class FilePermissions {
        private final int value;

        FilePermissions(int value) {
            if (value < 0 || value > 0777) {
                throw new IllegalArgumentException("Invalid permissions: " + value);
            }

            this.value = value;
        }

        public static FilePermissions fromValue(int value) {
            return new FilePermissions(value);
        }

        public static FilePermissions fromPosixPermissions(Set<PosixFilePermission> perms) {
            int value = 0;
            if (perms.contains(PosixFilePermission.OWNER_READ)) value |= 0400;
            if (perms.contains(PosixFilePermission.OWNER_WRITE)) value |= 0200;
            if (perms.contains(PosixFilePermission.OWNER_EXECUTE)) value |= 0100;
            if (perms.contains(PosixFilePermission.GROUP_READ)) value |= 040;
            if (perms.contains(PosixFilePermission.GROUP_WRITE)) value |= 020;
            if (perms.contains(PosixFilePermission.GROUP_EXECUTE)) value |= 010;
            if (perms.contains(PosixFilePermission.OTHERS_READ)) value |= 04;
            if (perms.contains(PosixFilePermission.OTHERS_WRITE)) value |= 02;
            if (perms.contains(PosixFilePermission.OTHERS_EXECUTE)) value |= 01;
            return fromValue(value);
        }

        public boolean canOwnerRead() {
            return (value & 0400) != 0;
        }

        public boolean canOwnerWrite() {
            return (value & 0200) != 0;
        }

        public boolean canOwnerExecute() {
            return (value & 0100) != 0;
        }

        public boolean canGroupRead() {
            return (value & 040) != 0;
        }

        public boolean canGroupWrite() {
            return (value & 020) != 0;
        }

        public boolean canGroupExecute() {
            return (value & 010) != 0;
        }

        public boolean canOthersRead() {
            return (value & 04) != 0;
        }

        public boolean canOthersWrite() {
            return (value & 02) != 0;
        }

        public boolean canOthersExecute() {
            return (value & 01) != 0;
        }

        public Set<PosixFilePermission> toPosixPermissions() {
            Set<PosixFilePermission> ret = EnumSet.noneOf(PosixFilePermission.class);
            if (canOwnerRead()) ret.add(PosixFilePermission.OWNER_READ);
            if (canOwnerWrite()) ret.add(PosixFilePermission.OWNER_WRITE);
            if (canOwnerExecute()) ret.add(PosixFilePermission.OWNER_EXECUTE);
            if (canGroupRead()) ret.add(PosixFilePermission.GROUP_READ);
            if (canGroupWrite()) ret.add(PosixFilePermission.GROUP_WRITE);
            if (canGroupExecute()) ret.add(PosixFilePermission.GROUP_EXECUTE);
            if (canOthersRead()) ret.add(PosixFilePermission.OTHERS_READ);
            if (canOthersWrite()) ret.add(PosixFilePermission.OTHERS_WRITE);
            if (canOthersExecute()) ret.add(PosixFilePermission.OTHERS_EXECUTE);
            return ret;
        }

        @Override
        public String toString() {
            return String.format("%04o", value);
        }
    }

    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor
    @JsonPropertyOrder({"mode", "name", "id"})
    public static class Entry {
        @JsonProperty("mode")
        private EntryMode mode;

        @JsonProperty("name")
        private String name;

        @JsonProperty("id")
        private String objectId;

        private Entry(EntryMode mode, String name, String objectId) {
            if (!ObjectRepository.validObjectId(objectId, false)) {
                throw new IllegalArgumentException("Object ID is not valid");
            }

            this.mode = Objects.requireNonNull(mode);
            this.name = Objects.requireNonNull(name);
            this.objectId = objectId;
        }

        public static Entry of(EntryMode mode, String name, String objectId) {
            return new Entry(mode, name, objectId);
        }
    }

    @Getter
    @EqualsAndHashCode
    public static class EntryMode {
        private static final Pattern MODE_PATTERN = Pattern.compile("(12|10|04)([0-7]{4})");

        private FileType type;
        private FilePermissions permissions;

        private EntryMode(FileType type, FilePermissions permissions) {
            this.type = Objects.requireNonNull(type);
            this.permissions = Objects.requireNonNull(permissions);
        }

        public static EntryMode of(FileType type, FilePermissions permissions) {
            return new EntryMode(type, permissions);
        }

        @JsonCreator
        public static EntryMode fromString(String mode) {
            Matcher matcher = MODE_PATTERN.matcher(mode);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid file mode: " + mode);
            }

            FileType type = FileType.fromValue(matcher.group(1))
                    .orElseThrow(IllegalArgumentException::new);
            FilePermissions permissions = new FilePermissions(Integer.parseInt(matcher.group(2), 8));
            return of(type, permissions);
        }

        @JsonValue
        @Override
        public String toString() {
            return type.getValue() + permissions.toString();
        }
    }
}
