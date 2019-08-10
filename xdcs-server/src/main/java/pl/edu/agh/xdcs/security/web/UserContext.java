package pl.edu.agh.xdcs.security.web;

import javax.enterprise.context.RequestScoped;

/**
 * @author Kamil Jarosz
 */
@RequestScoped
public class UserContext {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
