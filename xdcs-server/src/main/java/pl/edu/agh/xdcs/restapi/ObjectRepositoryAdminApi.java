package pl.edu.agh.xdcs.restapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * @author Kamil Jarosz
 */
@Path("admin/or")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ObjectRepositoryAdminApi {
    @GET
    @Path("orphans")
    Set<String> getOrphans();

    @POST
    @Path("housekeeping")
    void runHousekeeping();
}
