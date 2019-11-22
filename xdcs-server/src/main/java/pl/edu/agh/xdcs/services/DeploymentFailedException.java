package pl.edu.agh.xdcs.services;

/**
 * @author Kamil Jarosz
 */
public class DeploymentFailedException extends RuntimeException {
    public DeploymentFailedException(String message) {
        super(message);
    }
}
