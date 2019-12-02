package pl.edu.agh.xdcs.db;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @author Kamil Jarosz
 */
public class DbUtils {
    /**
     * Minimum valid instant for PostgreSQL.
     */
    public static final Instant MIN_INSTANT = LocalDate.of(-4712, 1, 1)
            .atStartOfDay().toInstant(ZoneOffset.UTC);

    /**
     * Maximum valid instant for PostgreSQL.
     */
    public static final Instant MAX_INSTANT = LocalDate.of(294276, 1, 1)
            .atStartOfDay().toInstant(ZoneOffset.UTC);
}
