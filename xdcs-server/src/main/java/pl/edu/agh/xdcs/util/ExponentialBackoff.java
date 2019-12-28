package pl.edu.agh.xdcs.util;

import javax.persistence.PersistenceException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Kamil Jarosz
 */
public interface ExponentialBackoff<R> {
    static <R> ExponentialBackoff<R> of(Subject<R> subject) {
        return timeout -> {
            Instant deadline = Instant.now().plus(timeout);
            long waitMillis = 2;
            while (true) {
                Optional<R> result = subject.tryExecute();
                if (result.isPresent()) {
                    return result;
                }

                if (Instant.now().isAfter(deadline)) {
                    return Optional.empty();
                }

                Thread.sleep(waitMillis);
                waitMillis *= 2;
            }
        };
    }

    static void fromPersistenceException(Runnable runnable, Duration timeout) {
        List<RuntimeException> suppressed = new ArrayList<>();
        Optional<Boolean> result = ExponentialBackoff.of(() -> {
            try {
                runnable.run();
                return Optional.of(true);
            } catch (PersistenceException e) {
                suppressed.add(e);
                return Optional.empty();
            }
        }).runWithoutInterruption(timeout);

        if (!result.isPresent()) {
            RuntimeException ex = suppressed.get(0);
            suppressed.subList(1, suppressed.size())
                    .forEach(ex::addSuppressed);
            throw ex;
        }
    }

    Optional<R> run(Duration timeout) throws InterruptedException;

    default Optional<R> runWithoutInterruption(Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        while (deadline.isAfter(Instant.now())) {
            try {
                return run(Duration.between(Instant.now(), deadline));
            } catch (InterruptedException ignored) {

            }
        }

        return Optional.empty();
    }

    @FunctionalInterface
    interface Subject<R> {
        Optional<R> tryExecute();
    }
}
