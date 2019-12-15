package pl.edu.agh.xdcs.agentapi.utils;

/**
 * @author Kamil Jarosz
 */
public class GrpcServiceException extends RuntimeException {
    public GrpcServiceException(String message) {
        super(message);
    }
}
