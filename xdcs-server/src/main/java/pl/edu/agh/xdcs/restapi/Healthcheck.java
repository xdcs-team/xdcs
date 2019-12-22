package pl.edu.agh.xdcs.restapi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Kamil Jarosz
 */
@Path("healthcheck")
public class Healthcheck {
    @GET
    @Path("")
    public void check() {
        // do nothing, 204 will be returned
    }
}
