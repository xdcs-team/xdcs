package pl.edu.agh.xdcs.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Kamil Jarosz
 */
class LockFileTest {
    @Test
    void lockInLock() {
        Path lockFile = Paths.get(".testLock");
        try (LockFile lf = LockFile.newLockFile(lockFile)) {
            assertThatThrownBy(() -> LockFile.newLockFile(lockFile))
                    .isInstanceOf(LockFile.LockFailedException.class);
        } catch (LockFile.LockFailedException e) {
            fail(e);
        }

        assertThat(Files.exists(lockFile))
                .isFalse();
    }

    @Test
    void twoConsecutiveLocks() {
        Path lockFile = Paths.get(".testLock");
        try (LockFile lf = LockFile.newLockFile(lockFile)) {
            assertThat(Files.exists(lockFile))
                    .isTrue();
        } catch (LockFile.LockFailedException e) {
            fail(e);
        }

        assertThat(Files.exists(lockFile))
                .isFalse();

        try (LockFile lf = LockFile.newLockFile(lockFile)) {
            assertThat(Files.exists(lockFile))
                    .isTrue();
        } catch (LockFile.LockFailedException e) {
            fail(e);
        }

        assertThat(Files.exists(lockFile))
                .isFalse();
    }
}
