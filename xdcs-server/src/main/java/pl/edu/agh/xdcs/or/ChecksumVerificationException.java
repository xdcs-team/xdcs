package pl.edu.agh.xdcs.or;

/**
 * @author Kamil Jarosz
 */
public class ChecksumVerificationException extends RuntimeException {
    public ChecksumVerificationException(String message) {
        super(message);
    }

    public ChecksumVerificationException(Throwable cause) {
        super(cause);
    }
}
