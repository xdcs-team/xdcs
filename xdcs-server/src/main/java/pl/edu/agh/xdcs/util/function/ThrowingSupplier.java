package pl.edu.agh.xdcs.util.function;

/**
 * @author Kamil Jarosz
 */
@FunctionalInterface
public interface ThrowingSupplier<R, T extends Throwable> {
    R get() throws T;
}
