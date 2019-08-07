package pl.edu.agh.xdcs.or.types;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Kamil Jarosz
 */
class FilePermissionsTest {
    @Test
    void testToString() {
        assertThat(new Tree.FilePermissions(0).toString())
                .isEqualTo("0000");
        assertThat(new Tree.FilePermissions(0234).toString())
                .isEqualTo("0234");
        assertThat(new Tree.FilePermissions(0777).toString())
                .isEqualTo("0777");
        assertThat(new Tree.FilePermissions(0720).toString())
                .isEqualTo("0720");
    }

    @Test
    void testInvalid() {
        assertThatThrownBy(() -> new Tree.FilePermissions(01720))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tree.FilePermissions(03000))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
