package pl.edu.agh.xdcs.restapi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * @author Kamil Jarosz
 */
public class RestUtils {
    private static final Logger logger = LoggerFactory.getLogger(RestUtils.class);

    public static Response created(String resourceLocation) {
        return Response.created(URI.create(resourceLocation)).build();
    }

    public static Response badRequest(String reason) {
        RestErrorResponse entity = RestErrorResponse.builder()
                .error(reason)
                .build();
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(entity)
                .build();
    }

    public static Response serverError(Throwable cause) {
        logger.error("Server error occurred", cause);
        RestErrorResponse entity = RestErrorResponse.builder()
                .error(cause.getClass().getName() + ": " + cause.getMessage())
                .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(entity)
                .build();
    }
}
