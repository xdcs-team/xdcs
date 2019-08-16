package pl.edu.agh.xdcs.grpc;

/**
 * @author Kamil Jarosz
 */
public class GrpcServerException extends RuntimeException {
    public GrpcServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrpcServerException(Throwable cause) {
        super(cause);
    }
}
