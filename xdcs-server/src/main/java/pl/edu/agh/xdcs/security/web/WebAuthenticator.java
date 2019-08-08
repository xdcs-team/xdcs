package pl.edu.agh.xdcs.security.web;

/**
 * @author Kamil Jarosz
 */
public interface WebAuthenticator {
    boolean authenticate(String username, String password);
}
