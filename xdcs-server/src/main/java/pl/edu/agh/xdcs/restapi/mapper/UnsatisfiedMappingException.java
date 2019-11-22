package pl.edu.agh.xdcs.restapi.mapper;

/**
 * @author Kamil Jarosz
 */
public class UnsatisfiedMappingException extends RuntimeException {
    public UnsatisfiedMappingException() {
    }

    public UnsatisfiedMappingException(String message) {
        super(message);
    }
}
