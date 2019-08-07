package pl.edu.agh.xdcs.db.conf;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamil Jarosz
 */
class UUIDConverterTest {
    private static final UUID JAVA = UUID.fromString("bc1edd34-b950-11e9-a2a3-2a2ae2dbcce4");
    private static final BigInteger DATABASE = new BigInteger("250055118703058612168239732454787370212");

    @Test
    void convertDbToJava() {
        UUIDConverter converter = new UUIDConverter();

        assertThat(converter.convertToEntityAttribute(DATABASE))
                .isEqualTo(JAVA);
    }

    @Test
    void convertJavaToDb() {
        UUIDConverter converter = new UUIDConverter();

        assertThat(converter.convertToDatabaseColumn(JAVA))
                .isEqualTo(DATABASE);
    }
}
