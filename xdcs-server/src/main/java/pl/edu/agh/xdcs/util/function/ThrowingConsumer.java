package pl.edu.agh.xdcs.util.function;

/**
 * @author Kamil Jarosz
 */
@FunctionalInterface
public interface ThrowingConsumer<A, T extends Throwable> {
    void accept(A t) throws T;
}
