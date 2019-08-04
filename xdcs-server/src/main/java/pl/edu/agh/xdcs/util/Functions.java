package pl.edu.agh.xdcs.util;

/**
 * @author Kamil Jarosz
 */
public class Functions {
    @FunctionalInterface
    public interface Function1<A, R> {
        R apply(A arg1);
    }

    @FunctionalInterface
    public interface Function2<A, B, R> {
        R apply(A arg1, B arg2);
    }

    @FunctionalInterface
    public interface Function3<A, B, C, R> {
        R apply(A arg1, B arg2, C arg3);
    }

    @FunctionalInterface
    public interface Function4<A, B, C, D, R> {
        R apply(A arg1, B arg2, C arg3, D arg4);
    }

    @FunctionalInterface
    public interface Function5<A, B, C, D, E, R> {
        R apply(A arg1, B arg2, C arg3, D arg4, E arg5);
    }

    @FunctionalInterface
    public interface Function6<A, B, C, D, E, F, R> {
        R apply(A arg1, B arg2, C arg3, D arg4, E arg5, F arg6);
    }
}
