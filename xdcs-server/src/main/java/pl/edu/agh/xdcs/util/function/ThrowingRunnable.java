package pl.edu.agh.xdcs.util.function;

/**
 * @author Kamil Jarosz
 */
@FunctionalInterface
public interface ThrowingRunnable<T extends Throwable> {
    void run() throws T;
}
