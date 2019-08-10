package pl.edu.agh.xdcs.security.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Kamil Jarosz
 */
@Path("")
public interface AuthEndpoints {
    @GET
    @Path("auth")
    @Produces(MediaType.TEXT_HTML)
    Response authorize(
            @QueryParam("response_type") String responseType,
            @QueryParam("client_id") String clientId,
            @QueryParam("redirect_uri") String redirectUri);

    @POST
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response doAuthorize(
            @QueryParam("response_type") String responseType,
            @QueryParam("client_id") String clientId,
            @QueryParam("redirect_uri") String redirectUri,
            @FormParam("username") String username,
            @FormParam("password") String password);

    @POST
    @Path("token")
    @Produces(MediaType.APPLICATION_JSON)
    Response getToken(
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("refresh_token") String refreshToken);
}
