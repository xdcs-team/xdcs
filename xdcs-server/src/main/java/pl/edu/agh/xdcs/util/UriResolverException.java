package pl.edu.agh.xdcs.util;

/**
 * @author Kamil Jarosz
 */
public class UriResolverException extends RuntimeException {
    public UriResolverException(String message) {
        super(message);
    }

    public UriResolverException(Throwable cause) {
        super(cause);
    }
}
