package pl.edu.agh.xdcs.util.function;

/**
 * @author Kamil Jarosz
 */
@FunctionalInterface
public interface ThrowingFunction<A, R, T extends Throwable> {
    R apply(A t) throws T;
}
