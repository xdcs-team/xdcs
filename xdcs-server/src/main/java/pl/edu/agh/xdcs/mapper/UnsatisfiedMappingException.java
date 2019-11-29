package pl.edu.agh.xdcs.mapper;

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
