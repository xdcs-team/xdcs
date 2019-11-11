package pl.edu.agh.xdcs.restapi.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Kamil Jarosz
 */
@Provider
public class ErrorResponseExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        Response response = exception.getResponse();
        if (response.hasEntity() || response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            return response;
        }

        return Response.fromResponse(response)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(RestErrorResponse.builder()
                        .error(exception.getMessage())
                        .build())
                .build();
    }
}
