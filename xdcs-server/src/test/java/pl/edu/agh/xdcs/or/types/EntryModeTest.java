package pl.edu.agh.xdcs.or.types;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.edu.agh.xdcs.or.types.Tree.FileType.S_IFLNK;

/**
 * @author Kamil Jarosz
 */
class EntryModeTest {
    @Test
    void testDeserialization() {
        Tree.EntryMode entryMode = Tree.EntryMode.fromString("120067");

        assertThat(entryMode.getType()).isEqualTo(S_IFLNK);
        assertThat(entryMode.getPermissions().toString()).isEqualTo("0067");
    }

    @Test
    void testInvalidDeserialization() {
        assertThatThrownBy(() -> Tree.EntryMode.fromString("170067"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tree.EntryMode.fromString("120867"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tree.EntryMode.fromString("12036"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Tree.EntryMode.fromString("121661"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSerialization() {
        Tree.EntryMode entryMode = Tree.EntryMode.of(S_IFLNK, new Tree.FilePermissions(0776));

        assertThat(entryMode.toString())
                .isEqualTo("120776");
    }
}
