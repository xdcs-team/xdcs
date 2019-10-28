package pl.edu.agh.xdcs.db.conf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Kamil Jarosz
 */
class ClassConverterTest {
    @Test
    void convertToDatabaseColumn() {
        assertThat(new ClassConverter().convertToDatabaseColumn(String.class))
                .isEqualTo("java.lang.String");
        assertThatThrownBy(() -> new ClassConverter().convertToDatabaseColumn(int.class))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void convertToEntityAttribute() {
        assertThat(new ClassConverter().convertToEntityAttribute("java.lang.String"))
                .isEqualTo(String.class);
    }
}
