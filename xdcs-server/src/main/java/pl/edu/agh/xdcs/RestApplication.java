package pl.edu.agh.xdcs;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author Kamil Jarosz
 */
@ApplicationPath("/rest")
public class RestApplication extends Application {
    public static final String CONTEXT_ROOT = "/xdcs";
}
